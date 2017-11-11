package dk.kaem.kaemwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Implementation of App Widget functionality.
 */
public class KaemWidget extends AppWidgetProvider {

    private static String usersCount;
    private static String projectsCount;
    private static String teamsCount;
    private static String lookupsCount;

    private static final String dkCountersURL = "https://kaem.dk/api/v1/app/counters";

    private static final String REFRESH_BUTTON = "dk.kaem.kaemwidget.REFRESH_BUTTON";
    private static final String PREF_NAME = "kaem_widget";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        setCounters(context);
        setCountersFromPreferences(context);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.kaem_widget);
        views.setTextViewText(R.id.kaem_users, usersCount);
        views.setTextViewText(R.id.kaem_projects, projectsCount);
        views.setTextViewText(R.id.kaem_teams, teamsCount);
        views.setTextViewText(R.id.kaem_lookups, lookupsCount);

        // Pending intent for refresh button
        Intent intent = new Intent(REFRESH_BUTTON);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.REFRESH_BUTTON, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static void setCountersFromPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // if we got data from json request, save it in shared preferences
        if (!Objects.equals(usersCount, "0")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("kaem_users", usersCount);
            editor.putString("kaem_projects", projectsCount);
            editor.putString("kaem_teams", teamsCount);
            editor.putString("kaem_lookups", lookupsCount);
            editor.apply();
        } else {
            // if we dont and we have data from shared preferences, set data
            usersCount = sharedPreferences.getString("kaem_users", "0");
            projectsCount = sharedPreferences.getString("kaem_projects", "0");
            teamsCount = sharedPreferences.getString("kaem_teams", "0");
            lookupsCount = sharedPreferences.getString("kaem_lookups", "0");
        }
    }

    private static void setCounters(Context context) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, dkCountersURL, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    usersCount = response.get("users").toString();
                    projectsCount = response.get("projects").toString();
                    teamsCount = response.get("teams").toString();
                    lookupsCount = response.get("lookups").toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // log error?
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (REFRESH_BUTTON.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            ComponentName watchWidget;

            watchWidget = new ComponentName(context, KaemWidget.class);
            onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(watchWidget));
        }
    }
}

