package website.madalina.bakingapp.mvp.recipe_detail;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import website.madalina.bakingapp.R;
import website.madalina.bakingapp.data.models.Recipe;
import website.madalina.bakingapp.data.models.Step;
import website.madalina.bakingapp.mvp.ingredient.IngredientFragment;
import website.madalina.bakingapp.mvp.step.StepFragment;
import website.madalina.bakingapp.mvp.step_detail.StepDetailFragment;
import website.madalina.bakingapp.util.DisplayMetricUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecipeDetailActivity extends AppCompatActivity implements StepFragment.StepFragmentListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.vp_container)
    ViewPager viewPager;
    FrameLayout flDetailStepContainer;

    private boolean isTablet;
    private Recipe recipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_recipe_detail);
        ButterKnife.bind(this);

        if (getIntent() != null && getIntent().hasExtra("recipe")) {
            recipe = getIntent().getParcelableExtra("recipe");
        }

        setupToolbar();

        flDetailStepContainer = (FrameLayout) findViewById(R.id.fl_detail_step_container);
        isTablet = flDetailStepContainer != null;

        if (isTablet) {
            ViewGroup.LayoutParams layoutParams =
                    tabLayout.getLayoutParams();
            layoutParams.width = (int) (7.0f / 18.0f * DisplayMetricUtils.getDeviceWidth(this));
            tabLayout.setLayoutParams(layoutParams);
        }

        setupFragment(savedInstanceState);

    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(recipe.getName());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupFragment(Bundle savedInstanceState) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        IngredientFragment ingredientFragment;
        StepFragment stepFragment;
        if (savedInstanceState == null) {
            ingredientFragment = IngredientFragment.newInstance(recipe.getIngredients());
            stepFragment = StepFragment.newInstance(recipe.getSteps(), isTablet);
        } else {
            ingredientFragment = (IngredientFragment) fragmentManager
                    .getFragment(savedInstanceState, "ingredientFragment");
            stepFragment = (StepFragment) fragmentManager.getFragment(savedInstanceState,
                    "stepFragment");
        }

        RecipeDetailViewPagerAdapter mSectionsPagerAdapter = new RecipeDetailViewPagerAdapter(
                fragmentManager,
                ingredientFragment,
                stepFragment);
        viewPager.setAdapter(mSectionsPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void showDetailStepFragment(Step step) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_detail_step_container, StepDetailFragment.newInstance(step, isTablet))
                .commit();
    }

    @Override
    public void onStepClicked(Step step) {
        showDetailStepFragment(step);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
