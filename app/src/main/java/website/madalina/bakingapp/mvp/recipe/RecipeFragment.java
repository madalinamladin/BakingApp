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

import android.Manifest;
import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import website.madalina.bakingapp.R;
import website.madalina.bakingapp.data.models.Recipe;
import website.madalina.bakingapp.mvp.recipe_detail.RecipeDetailActivity;
import website.madalina.bakingapp.util.DisplayMetricUtils;
import website.madalina.bakingapp.util.RecyclerViewMarginDecoration;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RecipeFragment extends LifecycleFragment
        implements RecipeContract.View, RecipeAdapter.RecipeClickListener {

    private static final int STATE_NO_CONNECTION = 1;
    private static final int STATE_OK = 2;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 100;

    @BindView(R.id.rv_recipes)
    RecyclerView rvRecipes;
    @BindView(R.id.ll_loading)
    LinearLayout llLoading;
    @BindView(R.id.ll_offline)
    LinearLayout llOffline;
    @BindView(R.id.tv_loading_message)
    TextView tvLoadingMessage;
    @BindView(R.id.bt_refresh)
    Button btRefresh;

    private RecipeViewModel mViewModel;
    private RecipeAdapter recipeAdapter;
    private int state;
    private RecipeFragmentListener fragmentListener;
    private AlertDialog permissionDialog;
    private boolean runningTest;

    public RecipeFragment() {
        // Required empty public constructor
    }

    public static RecipeFragment newInstance(long recipeId) {
        RecipeFragment recipeFragment = new RecipeFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("recipeId", recipeId);
        recipeFragment.setArguments(bundle);

        return recipeFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);
        ButterKnife.bind(this, view);

        if (savedInstanceState != null) {
            if (savedInstanceState.getInt("state") == STATE_NO_CONNECTION) {
                showNoConnection();
            }
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fragmentListener = (RecipeFragmentListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (permissionDialog != null && permissionDialog.isShowing()) {
            permissionDialog.dismiss();
        }
    }

    @OnClick(R.id.bt_refresh)
    public void refresh() {
        llOffline.setVisibility(View.GONE);
        mViewModel.loadRecipes();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("state", state);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        RecipeViewModel.Factory factory = new RecipeViewModel.Factory(
                getActivity().getApplication(), this);

        mViewModel = ViewModelProviders.of(this, factory)
                .get(RecipeViewModel.class);

        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            showPermissionDialog();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showPermissionDialog();
        } else {
            setIdlingResourceStatus(false);
            mViewModel.loadRecipes();
        }

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        int marginInDp = 8;
        int marginInPixel = DisplayMetricUtils.convertDpToPixel(8);
        int deviceWidthInDp = DisplayMetricUtils.convertPixelsToDp(
                DisplayMetricUtils.getDeviceWidth(getActivity()));

        int column = deviceWidthInDp / 300;
        int totalMarginInDp = marginInDp * (column + 1);
        int cardWidthInDp = (deviceWidthInDp - totalMarginInDp) / column;
        int cardHeightInDp = (int) (2.0f / 3.0f * cardWidthInDp);

        RecyclerViewMarginDecoration decoration =
                new RecyclerViewMarginDecoration(RecyclerViewMarginDecoration.ORIENTATION_VERTICAL,
                        marginInPixel, column);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), column);
        rvRecipes.setLayoutManager(layoutManager);
        rvRecipes.addItemDecoration(decoration);
        recipeAdapter = new RecipeAdapter(mViewModel.getLoadedRecipes(), cardWidthInDp, cardHeightInDp, this);
        rvRecipes.setAdapter(recipeAdapter);
    }

    private void showPermissionDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_permission, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle(R.string.dialog_title_storage_permission);

        dialogBuilder.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, STORAGE_PERMISSION_REQUEST_CODE);
            }
        });
        permissionDialog = dialogBuilder.create();
        permissionDialog.setCancelable(false);
        permissionDialog.show();
    }

    @Override
    public void setLoading(boolean status, @Nullable String message) {
        if (status) {
            rvRecipes.setVisibility(View.GONE);
            tvLoadingMessage.setText(message);
            llLoading.setVisibility(View.VISIBLE);
            setIdlingResourceStatus(false);
        } else {
            llLoading.setVisibility(View.GONE);
            rvRecipes.setVisibility(View.VISIBLE);
            setIdlingResourceStatus(true);
        }
    }

    @Override
    public void showError(String message) {

    }

    @Override
    public void updateRecipeList() {
        state = STATE_OK;
        llOffline.setVisibility(View.GONE);
        rvRecipes.setVisibility(View.VISIBLE);
        if (getArguments().getLong("recipeId", 0) != 0) {
            Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
            intent.putExtra("recipe", mViewModel.getRecipeById(getArguments().getLong("recipeId", 0)));
            startActivity(intent);
        } else {
            recipeAdapter.notifyDataSetChanged();
        }

        setIdlingResourceStatus(true);
    }

    @Override
    public void showNoConnection() {
        state = STATE_NO_CONNECTION;
        rvRecipes.setVisibility(View.GONE);
        llOffline.setVisibility(View.VISIBLE);
    }

    @Override
    public void setIdlingResourceStatus(boolean isIdle) {
        fragmentListener.onIdlingResourceStatusChanged(isIdle);
    }

    @Override
    public boolean isRunningTest() {
        return runningTest;
    }

    public void setRunningTest(boolean runningTest) {
        this.runningTest = runningTest;
    }

    @Override
    public void onRecipeItemClick(Recipe recipe) {
        Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
        intent.putExtra("recipe", recipe);
        startActivity(intent);
    }

    @Override
    public void onFavoriteClick(Recipe recipe, int position) {
        if (recipe.isFavorite()) {
            recipe.setFavorite(false);
            mViewModel.setFavoriteRecipe(recipe, position);
        } else {
            recipe.setFavorite(true);
            mViewModel.setFavoriteRecipe(recipe, position);
        }
        recipeAdapter.notifyItemChanged(position);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    mViewModel.loadRecipes();
                } else {
                    showError("Permission not granted");
                }
            }
        }
    }

    public interface RecipeFragmentListener {
        void onIdlingResourceStatusChanged(boolean isIdle);
    }
}
