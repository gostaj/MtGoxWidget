package st.brothas.mtgoxwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

/**
 * http://www.kaloer.com/android-preferences
 * http://www.vogella.de/articles/Android/article.html#preferences
 */
public class MtGoxPreferences extends PreferenceActivity {
    private static final String SERVICE_KEY = "service";
    private RateService selectedRateService = RateService.getDefaultService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED); // Cancelled until the user decides to add it
        addPreferencesFromResource(R.xml.preferences);
        final int appWidgetId = getAppWidgetId();

        // Get the service ListPreference from the GUI
        final Preference servicePref = findPreference("servicePref");
        updateServicePreferenceSummary(servicePref);
        servicePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                selectedRateService = RateService.valueOf((String) newValue);
                updateServicePreferenceSummary(preference);

                return true;
            }
        });

        // Get the service Add Widget button from the GUI
        Preference addWidgetButton = findPreference("addWidget");
        addWidgetButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                updateWidgetWithWaitMessage(appWidgetId);
                setServicePreference(appWidgetId, selectedRateService);
                startWidget(appWidgetId);
                return true;
            }
        });

    }

    private void updateWidgetWithWaitMessage(int appWidgetId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        MtGoxWidgetProvider.updateAppWidgetWithWaitMessage(this, appWidgetManager, appWidgetId);

    }

    private void startWidget(int appWidgetId) {
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        MtGoxWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId);
    }

    private void updateServicePreferenceSummary(Preference servicePref) {
        servicePref.setSummary("Currently using: " + selectedRateService.getName());
    }


    private int getAppWidgetId() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            return extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        } else {
            Log.e("mtgox", "AppWidgetId not found!");
            return 0;
        }
    }

    private void setServicePreference(int appWidgetId, RateService service) {
        SharedPreferences sharedPreferences = getSharedPreferences("" + appWidgetId, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SERVICE_KEY, service.name());
        editor.commit();
    }

    // Returns the default service if no RateService is found for this appWidgetId
    public static RateService getRateService(Context context, int appWidgetId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("" + appWidgetId, Context.MODE_PRIVATE);
        if (sharedPreferences.contains(SERVICE_KEY)) {
            String serviceName = sharedPreferences.getString(SERVICE_KEY, RateService.getDefaultService().name());
            return RateService.valueOf(serviceName);
        } else {
            return RateService.getDefaultService();
        }
    }

    // Deletes the preferences for the appWidgetId
    public static void deletePrefs(Context context, int appWidgetId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("" + appWidgetId, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }
}
