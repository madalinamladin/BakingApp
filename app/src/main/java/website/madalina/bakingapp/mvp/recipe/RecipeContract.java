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

import android.support.annotation.Nullable;

import website.madalina.bakingapp.data.models.Recipe;

import java.util.ArrayList;

public interface RecipeContract {
    interface View {
        void setLoading(boolean status, @Nullable String message);

        void showError(String message);

        void updateRecipeList();

        void showNoConnection();

        void setIdlingResourceStatus(boolean isIdle);

        boolean isRunningTest();
    }

    interface ViewModel {
        void loadRecipes();

        ArrayList<Recipe> getLoadedRecipes();

        void setFavoriteRecipe(Recipe recipe, int position);

        Recipe getRecipeById(long recipeId);
    }
}
