/*
 * Copyright 2018.  Mihaela Madalina Mladin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package website.madalina.bakingapp.mvp.recipe;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import website.madalina.bakingapp.data.models.Recipe;
import website.madalina.bakingapp.data.models.Thumbnail;
import website.madalina.bakingapp.data.source.local.LocalDataObserver;
import website.madalina.bakingapp.data.source.local.LocalRecipeDataSource;
import website.madalina.bakingapp.data.source.local.LocalRecipeDataSourceImpl;
import website.madalina.bakingapp.data.source.remote.RemoteRecipeDataSource;
import website.madalina.bakingapp.data.source.remote.RemoteRecipeDataSourceImpl;
import website.madalina.bakingapp.data.source.remote.RemoteResponseListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;

public class RecipeViewModel extends AndroidViewModel implements RecipeContract.ViewModel {

    private static final int RECIPE_COUNT = 4;
    private final RecipeContract.View mView;
    private final RemoteRecipeDataSource remoteRecipeDataSource;
    private final LocalRecipeDataSource localRecipeDataSource;
    private ArrayList<Recipe> recipes = new ArrayList<>();

    RecipeViewModel(Application application, RecipeContract.View mView) {
        super(application);
        this.mView = mView;
        remoteRecipeDataSource = new RemoteRecipeDataSourceImpl();
        localRecipeDataSource = new LocalRecipeDataSourceImpl(application.getApplicationContext());
    }

    @Override
    public void loadRecipes() {
        mView.setLoading(true, "Preparing Recipes...");
        if (recipes.size() == 0) {
            // Try to Get From Cache First
            localRecipeDataSource.getAll(new LocalDataObserver<ArrayList<Recipe>>() {
                @Override
                public void onNext(@io.reactivex.annotations.NonNull ArrayList<Recipe> cachedRecipes) {
                    if (cachedRecipes == null || cachedRecipes.size() != RECIPE_COUNT) {
                        getRecipeFromRemote();
                    } else {
                        getRecipeThumbnail(cachedRecipes);
                        recipes.clear();
                        recipes.addAll(cachedRecipes);
                        mView.setLoading(false, null);
                        mView.updateRecipeList();
                    }
                }

                @Override
                public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                    super.onError(e);
                    getRecipeFromRemote();
                }
            });
        } else {
            mView.setLoading(false, null);
            mView.updateRecipeList();
        }
    }

    private void getRecipeFromRemote() {
        remoteRecipeDataSource.getRecipes(new RemoteResponseListener<Recipe>() {
            @Override
            public void onRequestStart(Call<Recipe> call) {
            }

            @Override
            public void onSuccess(ArrayList<Recipe> result) {
                recipes.clear();
                recipes.addAll(result);
                makeRecipeCache(result);
                getRecipeThumbnail(result);
                mView.setLoading(false, null);
                mView.updateRecipeList();
            }

            @Override
            public void onFailure(Throwable throwable) {
                mView.setLoading(false, null);
                mView.showNoConnection();
            }
        });
    }

    private void getRecipeThumbnail(ArrayList<Recipe> result) {
        ArrayList<Thumbnail> thumbnails = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            Recipe recipe = result.get(i);
            if (recipe.getImage() == null || recipe.getImage().isEmpty()) {
                int stepCount = recipe.getSteps().size();
                for (int j = stepCount - 1; j >= 0; j--) {
                    if (recipe.getSteps().get(j).getThumbnailURL() != null &&
                            !recipe.getSteps().get(j).getThumbnailURL().isEmpty()) {
                        recipe.setImage(recipe.getSteps().get(j).getThumbnailURL());
                        break;
                    } else if (recipe.getSteps().get(j).getVideoURL() != null &&
                            !recipe.getSteps().get(j).getVideoURL().isEmpty()) {
                        Thumbnail thumbnail = new Thumbnail();
                        thumbnail.setPath(recipe.getSteps().get(j).getVideoURL());
                        thumbnail.setPosition(i);
                        thumbnails.add(thumbnail);
                        break;
                    }
                }
            }
        }

        if (thumbnails.size() > 0 && !mView.isRunningTest()) {
            new GetVideoThumbnailTask().execute(thumbnails.toArray(new Thumbnail[thumbnails.size()]));
        }
    }

    private void makeRecipeCache(ArrayList<Recipe> recipes) {
        localRecipeDataSource.insertMany(recipes, new LocalDataObserver<Uri>() {
        });
    }

    @Override
    public ArrayList<Recipe> getLoadedRecipes() {
        return recipes;
    }

    @Override
    public void setFavoriteRecipe(Recipe recipe, int position) {
        localRecipeDataSource.update(recipes.get(position), new LocalDataObserver<Integer>());
    }

    @Override
    public Recipe getRecipeById(long recipeId) {
        for (int i = 0; i < recipes.size(); i++) {
            if (recipes.get(i).getId() == recipeId) {
                return recipes.get(i);
            }
        }
        return null;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        private final RecipeContract.View mView;

        Factory(@NonNull Application application, RecipeContract.View view) {
            mApplication = application;
            mView = view;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new RecipeViewModel(mApplication, mView);
        }
    }

    private class GetVideoThumbnailTask extends AsyncTask<Thumbnail, Thumbnail, Thumbnail[]> {
        @Override
        protected Thumbnail[] doInBackground(Thumbnail... thumbnails) {
            Thumbnail[] thumbnailResult = new Thumbnail[thumbnails.length];
            for (int i = 0; i < thumbnails.length; i++) {
                Bitmap bitmap = null;
                MediaMetadataRetriever mediaMetadataRetriever = null;
                try {
                    mediaMetadataRetriever = new MediaMetadataRetriever();
                    mediaMetadataRetriever.setDataSource(thumbnails[i].getPath(), new HashMap<String, String>());
                    bitmap = mediaMetadataRetriever.getFrameAtTime();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (mediaMetadataRetriever != null) {
                        mediaMetadataRetriever.release();
                    }
                }

                String path = "";
                if (bitmap != null) {
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bytes);
                    path = MediaStore.Images.Media.insertImage(
                            getApplication().getApplicationContext().getContentResolver(),
                            bitmap, "tmp_recipe_thumb" + thumbnails[i].getPosition(), null);
                }

                Thumbnail thumbnail = new Thumbnail();
                thumbnail.setPath(path);
                thumbnail.setPosition(thumbnails[i].getPosition());
                thumbnailResult[i] = thumbnail;

                publishProgress(thumbnail);
            }

            return thumbnailResult;
        }

        @Override
        protected void onProgressUpdate(Thumbnail... values) {
            super.onProgressUpdate(values);

            recipes.get(values[0].getPosition()).setImage(values[0].getPath());
            localRecipeDataSource.update(recipes.get(values[0].getPosition()),
                    new LocalDataObserver<Integer>());
            mView.updateRecipeList();
        }

        @Override
        protected void onPostExecute(Thumbnail[] result) {
            mView.updateRecipeList();
        }
    }
}
