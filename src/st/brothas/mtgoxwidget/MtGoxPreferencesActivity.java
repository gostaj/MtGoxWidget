package st.brothas.mtgoxwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.List;

/**
 * http://www.kaloer.com/android-preferences
 * http://www.vogella.de/articles/Android/article.html#preferences
 */
public class MtGoxPreferencesActivity extends PreferenceActivity {
    private static final String SERVICE_KEY = "service";
    private static final String COLOR_MODE_KEY = "colorMode";
    private static final String CURRENCY_CONVERSION_KEY = "currencyConversion";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED); // Cancelled until the user decides to add it
        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false); // To set default values in dropdowns
        final WidgetPreferences widgetPreferences = new WidgetPreferences();

        // Get the service ListPreference from the GUI
        final Preference servicePref = findPreference("servicePref");
        //updatePreferenceSummary(servicePref, selectedRateService.getName());
        updateCurrencyChoices(widgetPreferences.getRateService());
        servicePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                widgetPreferences.setRateService(RateService.valueOf((String) newValue));
                updatePreferenceSummary(servicePref, widgetPreferences.getRateService().getName());
                updateCurrencyChoices(widgetPreferences.getRateService());

                return true;
            }
        });

        // Get the currency conversion ListPreference from the GUI
        final Preference currencyConversionPref = findPreference("currencyConversionPref");
        currencyConversionPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                widgetPreferences.setCurrencyConversion(CurrencyConversion.valueOf((String) newValue));
                updatePreferenceSummary(currencyConversionPref, widgetPreferences.getCurrencyConversion().description);

                return true;
            }
        });

        // Get the color mode ListPreference from the GUI
        final Preference colorModePref = findPreference("colorModePref");
        //updatePreferenceSummary(colorModePref, selectedColorMode.name());
        colorModePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                widgetPreferences.setColorMode(ColorMode.valueOf((String) newValue));
                updatePreferenceSummary(colorModePref, widgetPreferences.getColorMode().name());

                return true;
            }
        });

        // Get the service Add Widget button from the GUI
        Preference addWidgetButton = findPreference("addWidget");
        addWidgetButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                updateWidgetWithWaitMessage(getAppWidgetId());
                storePreferences(widgetPreferences);
                startWidget(getAppWidgetId());
                return true;
            }
        });
    }

    private void updateCurrencyChoices(RateService selectedRateService) {
        ListPreference lp = (ListPreference)findPreference("currencyConversionPref");
        List<CurrencyConversion> currencyConversions = selectedRateService.getCurrencyConversions();
        CharSequence[] entries = new CharSequence[currencyConversions.size()];
        CharSequence[] entryValues = new CharSequence[currencyConversions.size()];
        int i = 0;
        for (CurrencyConversion currencyConversion : currencyConversions) {
            entries[i] = currencyConversion.description;
            entryValues[i] = currencyConversion.name();
            i++;
        }
        lp.setEntries(entries);
        lp.setEntryValues(entryValues);
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

    private void updatePreferenceSummary(Preference preference, String currentPreference) {
        preference.setSummary("Using: " + currentPreference);
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


    private void storePreferences(WidgetPreferences widgetPreferences) {
        SharedPreferences sharedPreferences = getSharedPreferences("" + getAppWidgetId(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SERVICE_KEY, widgetPreferences.getRateService().name());
        editor.putString(COLOR_MODE_KEY, widgetPreferences.getColorMode().name());
        editor.putString(CURRENCY_CONVERSION_KEY, widgetPreferences.getCurrencyConversion().name());
        editor.commit();
    }

    // Deletes the preferences for the appWidgetId
    public static void deletePrefs(Context context, int appWidgetId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("" + appWidgetId, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

    // Returns null if no preferences are set for this widget id.
    public static WidgetPreferences getWidgetPreferences(Context context, int appWidgetId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("" + appWidgetId, Context.MODE_PRIVATE);
        if (!sharedPreferences.contains(SERVICE_KEY)) {
            return null;
        }

        WidgetPreferences widgetPreferences = new WidgetPreferences();

        String colorModeName = sharedPreferences.getString(COLOR_MODE_KEY, ColorMode.Default.name());
        widgetPreferences.setColorMode(ColorMode.valueOf(colorModeName));
        String serviceName = sharedPreferences.getString(SERVICE_KEY, RateService.getDefaultService().name());
        widgetPreferences.setRateService(RateService.valueOf(serviceName));
        String currencyConversionName = sharedPreferences.getString(CURRENCY_CONVERSION_KEY, CurrencyConversion.getDefault().name());
        widgetPreferences.setCurrencyConversion(CurrencyConversion.valueOf(currencyConversionName));

        return widgetPreferences;
    }

}
