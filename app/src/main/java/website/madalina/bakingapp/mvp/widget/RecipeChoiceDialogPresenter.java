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

package website.madalina.bakingapp.mvp.widget;

import android.app.Application;

import website.madalina.bakingapp.data.models.Recipe;
import website.madalina.bakingapp.data.source.local.LocalDataObserver;
import website.madalina.bakingapp.data.source.local.LocalRecipeDataSource;
import website.madalina.bakingapp.data.source.local.LocalRecipeDataSourceImpl;

import java.util.ArrayList;

import io.reactivex.annotations.NonNull;

public class RecipeChoiceDialogPresenter implements RecipeChoiceContract.Presenter {

    private final RecipeChoiceContract.View view;
    private final LocalRecipeDataSource localRecipeDataSource;
    private ArrayList<Recipe> recipes = new ArrayList<>();

    RecipeChoiceDialogPresenter(Application application, RecipeChoiceContract.View view) {
        this.view = view;
        this.localRecipeDataSource = new LocalRecipeDataSourceImpl(application.getApplicationContext());
    }

    @Override
    public void loadRecipes() {
        if (recipes.size() == 0) {
            // Try to Get From Cache First
            localRecipeDataSource.getAll(new LocalDataObserver<ArrayList<Recipe>>() {
                @Override
                public void onNext(@NonNull ArrayList<Recipe> cachedRecipes) {
                    if (cachedRecipes != null && cachedRecipes.size() > 0) {
                        recipes.clear();
                        recipes.addAll(cachedRecipes);
                        view.updateRecipeList();
                    }
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    super.onError(e);
                }
            });
        } else {
            view.updateRecipeList();
        }
    }

    @Override
    public ArrayList<Recipe> getLoadedRecipes() {
        return recipes;
    }

}
