package website.madalina.bakingapp.mvp.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RemoteViews;

import website.madalina.bakingapp.R;
import website.madalina.bakingapp.data.models.Recipe;
import website.madalina.bakingapp.mvp.recipe.RecipeActivity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class RecipeChoiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RecipeChoiceDialog recipeChoiceDialog = new RecipeChoiceDialog();
        recipeChoiceDialog.setDialogListener(new RecipeChoiceDialog.RecipeChoiceDialogListener() {
            @Override
            public void onDismiss(Recipe recipe) {

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(RecipeChoiceActivity.this);
                RemoteViews remoteViews = new RemoteViews(RecipeChoiceActivity.this.getPackageName(), R.layout.recipe_widget);
                ComponentName recipeWidget = new ComponentName(RecipeChoiceActivity.this, RecipeWidgetProvider.class);

                final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(recipeWidget);
                for (int appWidgetId : appWidgetIds) {
                    if (appWidgetId == getIntent().getIntExtra("widgetId", 0)) {
                        remoteViews.setTextViewText(R.id.tv_recipe_name, recipe.getName());
                        remoteViews.setViewVisibility(R.id.ll_no_recipe, GONE);
                        remoteViews.setViewVisibility(R.id.ll_ingredient, VISIBLE);

                        Intent remoteAdapterIntent = new Intent(RecipeChoiceActivity.this,
                                RecipeWidgetService.class);
                        remoteAdapterIntent.setData(Uri.fromParts("recipeId",
                                String.valueOf(recipe.getId()), null));
                        remoteViews.setRemoteAdapter(R.id.gv_ingredient, remoteAdapterIntent);
                        Intent openAppIntent = new Intent(RecipeChoiceActivity.this,
                                RecipeActivity.class);
                        openAppIntent.putExtra("recipeId", recipe.getId());
                        PendingIntent appPendingIntent = PendingIntent.getActivity(
                                RecipeChoiceActivity.this, (int) recipe.getId(), openAppIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                        remoteViews.setPendingIntentTemplate(R.id.gv_ingredient, appPendingIntent);
                        remoteViews.setEmptyView(R.id.gv_ingredient, R.id.ll_no_recipe);

                        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

                        break;
                    }
                }

                RecipeChoiceActivity.this.finish();
            }
        });
        recipeChoiceDialog.show(getSupportFragmentManager(), "recipeChoiceDialog");
    }
}
