/*
 * Copyright (c) 2010-2011 Gösta Jonasson. All Rights Reserved.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package st.brothas.mtgoxwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import st.brothas.mtgoxwidget.net.HttpManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import static st.brothas.mtgoxwidget.CurrencyConversion.VirtualCurrency.LITECOIN;

/**
 *
 */
public class MtGoxWidgetProvider extends AppWidgetProvider {
    public static final String LOG_TAG = "MtGox";
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("E HH:mm");
    private static final int DATA_IS_CONSIDERED_OLD_AFTER_MINUTES = 60;

    private enum WidgetColor {Warning, StartValue, Increase, Decrease, Normal}

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateAppWidgetAsync(context, appWidgetManager, appWidgetIds);
    }

    public static void updateAppWidgetAsync(final Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        updateAppWidgetAsync(context, appWidgetManager, new int[] { appWidgetId });
    }

    public static void updateAppWidgetAsync(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        new UpdateAsyncTask(context, appWidgetManager, appWidgetIds).execute();
    }

    private static void updateAppWidget(final Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        WidgetPreferences preferences = MtGoxPreferencesActivity.getWidgetPreferences(context, appWidgetId);

        if (preferences == null) {
            // Don't do anything unless the rate service has been chosen.
            // Show a "please remove this widget and add a new one"
            appWidgetManager.updateAppWidget(appWidgetId,
                    new RemoteViews(context.getPackageName(), R.layout.appwidget_replace_me));
            return;
        }

        boolean isOnLockScreen = isWidgetShownOnLockScreen(appWidgetManager, appWidgetId);

        RemoteViews views;
        if (isOnLockScreen) {
            views = new RemoteViews(context.getPackageName(), R.layout.appwidget_provider_lock_screen);
        } else {
            views = new RemoteViews(context.getPackageName(), R.layout.appwidget_provider);
        }

        Intent clickIntent = new Intent(context, GraphPopupActivity.class);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        clickIntent.setAction("dummyAction"); // Needed to get the extra variables included in the call
        // Note: the appWidgetId needs to be sent in the pendingIntent as request code, otherwise only ONE
        //       cached intent will be used for all widget instances!
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, clickIntent, 0);
        views.setOnClickPendingIntent(R.id.appwidget_box, pendingIntent);
        views.setTextViewText(R.id.appwidget_service_name, preferences.getRateService().getName());

        MtGoxDataOpenHelper dbHelper = new MtGoxDataOpenHelper(context);
        MtGoxTickerData prevData = dbHelper.getLastTickerData(preferences);

        MtGoxTickerData newData;
        String latestQuote = getLatestQuote(preferences);
        if (latestQuote != null && !latestQuote.equals("")) {
            newData = preferences.getRateService().parseJSON(latestQuote);
            newData.setCurrencyConversion(preferences.getCurrencyConversion());
            storeLastValueIfNotNull(dbHelper, newData);
            updateViews(views, prevData, newData, preferences);
        } else if (prevData != null) {
            newData = prevData;
            updateViews(views, prevData, newData, preferences);
        } else {
            updateViewsWithError(views, preferences);
        }
        appWidgetManager.updateAppWidget(appWidgetId, views);
   }

    private static boolean isWidgetShownOnLockScreen(AppWidgetManager appWidgetManager, int appWidgetId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // The below functionality was introduced in API version 16 (Jelly Bean)
            Bundle myOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);

            // Get the value of OPTION_APPWIDGET_HOST_CATEGORY
            int category = myOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, -1);

            // If the value is WIDGET_CATEGORY_KEYGUARD, it's a lockscreen widget
            return category == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD;
        }
        return false;
    }

    private static void updateViews(RemoteViews views, MtGoxTickerData prevData, MtGoxTickerData newData,
                                    WidgetPreferences preferences) {
        String updated = "@ " + dateFormat.format(newData.getTimestamp());
        String lastRounded = round(newData.getLast());
        String lowRounded = round(newData.getLow());
        String highRounded = round(newData.getHigh());

        views.setTextViewText(R.id.appwidget_last, preferences.getCurrencyConversion().symbol + lastRounded);
        views.setTextColor(R.id.appwidget_updated, getColor(preferences.getColorMode(), WidgetColor.Normal));
        if (newData.getTimestamp().before(getDateMinutesAgo(DATA_IS_CONSIDERED_OLD_AFTER_MINUTES))) {
            // Data is old, show it by "old" and "warning" colors
            views.setTextColor(R.id.appwidget_last, Color.GRAY);
            views.setTextColor(R.id.appwidget_updated, getColor(preferences.getColorMode(), WidgetColor.Warning));
        } else if (prevData != null) {
            // We have previous data, compare to get the color
            views.setTextColor(R.id.appwidget_last, getColorFromValueChange(prevData.getLast(),
                    newData.getLast(), preferences.getColorMode()));
        } else {
            // No previous data, set standard color
            views.setTextColor(R.id.appwidget_last, getColor(preferences.getColorMode(), WidgetColor.StartValue));
        }
        views.setTextViewText(R.id.appwidget_high, highRounded);
        views.setTextViewText(R.id.appwidget_low, lowRounded);
        views.setTextViewText(R.id.appwidget_updated, updated);

        // Set Litecoin logo if that is the chosen currency.
        switch (preferences.getCurrencyConversion().virtualCurrency) {
            case LITECOIN:
                views.setImageViewResource(R.id.appwidget_logo, R.drawable.lc_logo_30);
                break;
            case QUARK:
                views.setImageViewResource(R.id.appwidget_logo, R.drawable.qrk_logo_30);
                break;
            // The Bitcoin logo is default in the layout.
        }
    }

    private static void updateViewsWithError(RemoteViews views, WidgetPreferences preferences) {
        views.setTextViewText(R.id.appwidget_last, "N/A");
        views.setTextColor(R.id.appwidget_last, getColor(preferences.getColorMode(), WidgetColor.Warning));
        views.setTextViewText(R.id.appwidget_updated, "@ " + dateFormat.format(new Date()));
        views.setTextColor(R.id.appwidget_updated, getColor(preferences.getColorMode(), WidgetColor.Normal));
    }

    private static int getColor(ColorMode colorMode, WidgetColor widgetColor) {
        if (colorMode.equals(ColorMode.Default)) {
            switch(widgetColor) {
                case Warning:
                    return Color.parseColor("#ff3030");
                case StartValue:
                    return Color.YELLOW;
                case Normal:
                    return Color.LTGRAY;
                case Increase:
                    return Color.GREEN;
                case Decrease:
                    return Color.parseColor("#ff3030");
                default:
                    throw new IllegalArgumentException("No color defined for " + widgetColor);
            }
        } else if (colorMode.equals(ColorMode.Grayscale)) {
            switch(widgetColor) {
                case Warning:
                    return Color.WHITE;
                case StartValue:
                    return Color.LTGRAY;
                case Normal:
                    return Color.LTGRAY;
                case Increase:
                    return Color.WHITE;
                case Decrease:
                    return Color.GRAY;
                default:
                    throw new IllegalArgumentException("No color defined for " + widgetColor);
            }
        } else {
            throw new IllegalArgumentException("No color mode defined for " + colorMode);
        }
    }

    private static Date getDateMinutesAgo(int minutes) {
        return new Date(System.currentTimeMillis() - (minutes*60*1000));
    }

    private static String round(Double value) {
        if (value == null) {
            return "N/A";
        }

        int decimals = 2;
        if (value >= 1000) {
            decimals = 0;
        } else if (value >= 100) {
            decimals = 1;
        } else if (value < 10) {
            decimals = 3;
        }

        return BigDecimal.valueOf(value).setScale(decimals, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static int getColorFromValueChange(Double prevValue, Double nowValue, ColorMode colorMode) {
    	if (prevValue == null || nowValue == null || prevValue.equals(nowValue)) {
    		return getColor(colorMode, WidgetColor.StartValue);
    	} else if (prevValue < nowValue) {
    		return getColor(colorMode, WidgetColor.Increase);
    	} else {
    		return getColor(colorMode, WidgetColor.Decrease);
    	}
	}


	private static void storeLastValueIfNotNull(MtGoxDataOpenHelper dbHelper, MtGoxTickerData data) {
		if (data != null) {
			dbHelper.storeTickerData(data);
			dbHelper.cleanUp();
       }
	}

    private static String getLatestQuote(WidgetPreferences preferences) {
        HttpGet httpget = new HttpGet(preferences.getRateService().getTickerUrl(preferences.getCurrencyConversion()));
        HttpResponse response;
        try {
            response = HttpManager.execute(httpget);
            HttpEntity entity = response.getEntity();
            String responseString = "";
            if (entity != null) {
                InputStream inStream = entity.getContent();
                responseString = convertStreamToString(inStream);
                inStream.close();
            }
            return responseString;
        } catch (ClientProtocolException e) {
            Log.e(LOG_TAG, "Error when getting JSON", e);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error when getting JSON: " + e.getMessage());
        }
        return null;
    }

    private static String convertStreamToString(InputStream inStream) {
        BufferedReader rd = new BufferedReader(new InputStreamReader(inStream), 4096);
        String line;
        StringBuilder sb =  new StringBuilder();
        try {
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error when converting inputstream to string", e);
        }
        return sb.toString();
    }
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            MtGoxPreferencesActivity.deletePrefs(context, appWidgetId);
        }
    }

    public static void updateAppWidgetWithWaitMessage(Context context,
                                                      AppWidgetManager appWidgetManager,
                                                      int appWidgetId) {
        appWidgetManager.updateAppWidget(appWidgetId,
                new RemoteViews(context.getPackageName(), R.layout.appwidget_loading));
    }

    /**
     * Class for updating the widget(s) asynchronously (not on main thread).
     */
    private static class UpdateAsyncTask extends AsyncTask<Void, Void, Void> {
        private Context context;
        private AppWidgetManager appWidgetManager;
        private int[] appWidgetIds;

        public UpdateAsyncTask(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
            this.context = context;
            this.appWidgetManager = appWidgetManager;
            this.appWidgetIds = appWidgetIds;
        }

        @Override
        protected Void doInBackground(Void... bla) {
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
            return null;
        }
    }
}


