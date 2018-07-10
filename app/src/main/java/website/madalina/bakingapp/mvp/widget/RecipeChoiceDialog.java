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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import website.madalina.bakingapp.R;
import website.madalina.bakingapp.data.models.Recipe;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecipeChoiceDialog extends DialogFragment implements RecipeChoiceContract.View {

    @BindView(R.id.rv_recipes)
    RecyclerView rvRecipes;

    private SimpleRecipeAdapter recipeAdapter;
    private RecipeChoiceDialogListener dialogListener;

    public void setDialogListener(RecipeChoiceDialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_recipe_choice, container);
        ButterKnife.bind(this, view);

        RecipeChoiceDialogPresenter presenter = new RecipeChoiceDialogPresenter(getActivity().getApplication(), this);
        presenter.loadRecipes();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvRecipes.setLayoutManager(layoutManager);
        recipeAdapter = new SimpleRecipeAdapter(presenter.getLoadedRecipes(), new SimpleRecipeAdapter.SimpleRecipeClickListener() {
            @Override
            public void onRecipeItemClick(Recipe recipe) {
                dialogListener.onDismiss(recipe);
            }
        });
        rvRecipes.setAdapter(recipeAdapter);

        return view;
    }

    @Override
    public void updateRecipeList() {
        recipeAdapter.notifyDataSetChanged();
    }

    public interface RecipeChoiceDialogListener {
        void onDismiss(Recipe recipe);
    }
}
