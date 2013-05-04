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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;

import st.brothas.mtgoxwidget.net.HttpManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

public class MtGoxWidgetProvider extends AppWidgetProvider {
	// Color.RED was too dark. Maybe with the bigger font it would do again.
	private static final int RED = Color.rgb(255, 50, 50);
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("E HH:mm");
	private static final int DATA_IS_CONSIDERED_OLD_AFTER_MINUTES = 60;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(Constants.TAG, "MtGoxWidgetProvider.onUpdate: ");
		updateAppWidgetAsync(context, appWidgetManager, appWidgetIds);
	}

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
			for (int id : appWidgetIds) {
				updateAppWidget(context, appWidgetManager, id);
			}
			return null;
		}
	}

	public static void updateAppWidgetAsync(final Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
		updateAppWidgetAsync(context, appWidgetManager, new int[] { appWidgetId });
	}

	public static void updateAppWidgetAsync(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		new UpdateAsyncTask(context, appWidgetManager, appWidgetIds).execute();
	}

	private static void updateAppWidget(final Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
		Log.d(Constants.TAG, "MtGoxWidgetProvider.updateAppWidget: ");
		RateService rateService = MtGoxPreferences.getRateService(context, appWidgetId);
		if (rateService == null) {
			// Don't do anything unless the rate service has been chosen.
			// Show a "please remove this widget and add a new one"
			appWidgetManager.updateAppWidget(appWidgetId, new RemoteViews(context.getPackageName(), R.layout.appwidget_replace_me));
			return;
		}

		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_provider);
		Intent clickIntent = new Intent(context, GraphPopupActivity.class);
		clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		// Needed to get the extra variables included in the call
		clickIntent.setAction("dummyAction");
		// Note: the appWidgetId needs to be sent in the pendingIntent as
		// request code, otherwise only ONE
		// cached intent will be used for all widget instances!
		PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, clickIntent, 0);
		views.setOnClickPendingIntent(R.id.appwidget_box, pendingIntent);
		views.setTextViewText(R.id.appwidget_service_name, rateService.getName());

		MtGoxDataOpenHelper dbHelper = new MtGoxDataOpenHelper(context);
		MtGoxTickerData prevData = dbHelper.getLastTickerData(rateService);

		MtGoxTickerData newData;
		JSONObject latestQuoteJSON = getLatestQuoteJSON(rateService);
		if (latestQuoteJSON != null) {
			Log.d(Constants.TAG, "MtGoxWidgetProvider.updateAppWidget: latestQuoteJSON" + latestQuoteJSON);
			newData = rateService.parseJSON(latestQuoteJSON);
			storeLastValueIfNotNull(dbHelper, newData);
			updateViews(views, prevData, newData);
		} else if (prevData != null) {
			newData = prevData;
			updateViews(views, prevData, newData);
		} else {
			updateViewsWithError(views);
		}
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	private static void updateViews(RemoteViews views, MtGoxTickerData prevData, MtGoxTickerData newData) {
		Log.d(Constants.TAG, "MtGoxWidgetProvider.updateViews: ");
		String updated = "@ " + dateFormat.format(newData.getTimestamp());
		double last = newData.getLast();
		double low = newData.getLow();
		double high = newData.getHigh();
		int digits = 3 - (int) Math.log10(high);
		String lastRounded = round(digits, last);
		String lowRounded = round(digits, low);
		String highRounded = round(digits, high);

		views.setTextViewText(R.id.appwidget_last, "$" + lastRounded);
		views.setTextColor(R.id.appwidget_updated, Color.LTGRAY);
		// If we have no previous data, set standard color
		int lastTextColor = Color.YELLOW;
		if (newData.getTimestamp().before(getDateMinutesAgo(DATA_IS_CONSIDERED_OLD_AFTER_MINUTES))) {
			// Data is old, show it by "old" and "warning" colors
			lastTextColor = Color.LTGRAY;
			views.setTextColor(R.id.appwidget_updated, RED);
		} else if (newData.getVwap() != null) {
			// We have https://en.wikipedia.org/wiki/VWAP to compare to get the
			// color
			lastTextColor = getColorFromValueChange(newData.getVwap(), last);
		} else if (prevData != null) {
			// We have previous data to compare to. Lets use it to get the color
			lastTextColor = getColorFromValueChange(prevData.getLast(), last);
		}
		views.setTextColor(R.id.appwidget_last, lastTextColor);
		views.setTextViewText(R.id.appwidget_high, highRounded);
		views.setTextViewText(R.id.appwidget_low, lowRounded);
		views.setTextViewText(R.id.appwidget_updated, updated);
	}

	private static void updateViewsWithError(RemoteViews views) {
		views.setTextViewText(R.id.appwidget_last, "N/A");
		views.setTextColor(R.id.appwidget_last, RED);
		views.setTextViewText(R.id.appwidget_updated, "@ " + dateFormat.format(new Date()));
		views.setTextColor(R.id.appwidget_updated, Color.LTGRAY);
	}

	private static Date getDateMinutesAgo(int minutes) {
		return new Date(System.currentTimeMillis() - (minutes * 60 * 1000));
	}

	private static String round(int decimals, Double value) {
		if (value != null && value > 0) {
			return BigDecimal.valueOf(value).setScale(decimals, BigDecimal.ROUND_HALF_UP).toString();
		} else {
			return "N/A";
		}
	}

	private static int getColorFromValueChange(Double averageValue, Double nowValue) {
		if (averageValue == null || nowValue == null || averageValue.equals(nowValue)) {
			return Color.YELLOW;
		} else if (averageValue < nowValue) {
			return Color.GREEN;
		} else {
			return RED;
		}
	}

	private static void storeLastValueIfNotNull(MtGoxDataOpenHelper dbHelper, MtGoxTickerData data) {
		if (data != null) {
			dbHelper.storeTickerData(data);
			dbHelper.cleanUp();
		}
	}

	private static JSONObject getLatestQuoteJSON(RateService rateService) {
		HttpGet httpget = new HttpGet(rateService.getTickerUrl());
		HttpResponse response;
		try {
			response = HttpManager.execute(httpget);
			HttpEntity entity = response.getEntity();
			JSONObject jObject = null;
			if (entity != null) {
				InputStream instream = entity.getContent();
				String result = convertStreamToString(instream);
				jObject = new JSONObject(result);
				instream.close();
			}
			return jObject;
		} catch (ClientProtocolException e) {
			Log.e(Constants.TAG, "Error when getting JSON", e);
		} catch (IOException e) {
			Log.e(Constants.TAG, "Error when getting JSON: " + e.getMessage());
		} catch (JSONException e) {
			Log.e(Constants.TAG, "Error when parsing JSON", e);
		}
		return null;
	}

	private static String convertStreamToString(InputStream instream) {
		BufferedReader rd = new BufferedReader(new InputStreamReader(instream), 4096);
		String line;
		StringBuilder sb = new StringBuilder();
		try {
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}
			rd.close();
		} catch (IOException e) {
			Log.e(Constants.TAG, "Error when converting inputstream to string", e);
		}
		return sb.toString();
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// When the user deletes the widget, delete the preference associated
		// with it.
		Log.d(Constants.TAG, "MtGoxWidgetProvider.onDeleted: ");
		final int n = appWidgetIds.length;
		for (int i = 0; i < n; i++) {
			MtGoxPreferences.deletePrefs(context, appWidgetIds[i]);
		}
	}

	public static void updateAppWidgetWithWaitMessage(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
		Log.d(Constants.TAG, "MtGoxWidgetProvider.updateAppWidgetWithWaitMessage: ");
		appWidgetManager.updateAppWidget(appWidgetId, new RemoteViews(context.getPackageName(), R.layout.appwidget_loading));
	}
}
