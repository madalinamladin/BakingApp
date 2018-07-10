package website.madalina.bakingapp.mvp.recipe;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import website.madalina.bakingapp.R;
import website.madalina.bakingapp.util.SimpleIdlingResource;

import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.ButterKnife;

public class RecipeActivity extends AppCompatActivity implements RecipeFragment.RecipeFragmentListener {

    private AtomicBoolean runningTest;

    @Nullable
    private SimpleIdlingResource mIdlingResource;

    @Nullable
    @VisibleForTesting
    public SimpleIdlingResource getIdlingResource() {
        if (mIdlingResource == null) {
            mIdlingResource = new SimpleIdlingResource();
        }
        return mIdlingResource;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);
        ButterKnife.bind(this);

        SimpleIdlingResource idlingResource = getIdlingResource();
        if (idlingResource != null) {
            idlingResource.setIdleState(false);
            Log.d("SetIdlingStatus", String.valueOf(false));
        } else {
            Log.d("SetIdlingStatus", "Null");
        }

        if (savedInstanceState == null) {
            RecipeFragment recipeFragment = RecipeFragment.newInstance(
                    getIntent().getLongExtra("recipeId", 0L));
            recipeFragment.setRunningTest(isRunningTest());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_recipe_container, recipeFragment)
                    .commit();
        }
    }

    @Override
    public void onIdlingResourceStatusChanged(boolean isIdle) {
        SimpleIdlingResource idlingResource = getIdlingResource();
        if (idlingResource != null) {
            Log.d("SetIdlingStatus", String.valueOf(isIdle));
            idlingResource.setIdleState(isIdle);
        } else {
            Log.d("SetIdlingStatus", "Null");
        }
    }

    public synchronized boolean isRunningTest() {
        if (null == runningTest) {
            boolean isTest;

            try {
                Class.forName("android.support.test.espresso.Espresso");
                isTest = true;
            } catch (ClassNotFoundException e) {
                isTest = false;
            }

            runningTest = new AtomicBoolean(isTest);
        }

        return runningTest.get();
    }
}
