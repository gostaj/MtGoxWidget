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

import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Represents the graph that shows the ticker data change in time.
 */
public class GraphPopupActivity extends Activity {
	private GraphicalView chartView;
	private XYMultipleSeriesDataset dataset;
	private XYMultipleSeriesRenderer renderer;
	// Add 15 minutes to each side of the graph
	private static final long PADDING_MS = 15 * 60 * 1000;
	// 24 hours
	private static final long ONE_DAY_IN_MS = 24 * 60 * 60 * 1000;
	private boolean emptyChart = false;
	private int appWidgetId;

	@Override
	protected void onCreate(Bundle bundle) {
		Log.d(Constants.TAG, "GraphPopupActivity.onCreate: ");
		super.onCreate(bundle);

		appWidgetId = getIntent().getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);

		MtGoxDataOpenHelper db = new MtGoxDataOpenHelper(this);
		RateService rateService = MtGoxPreferences.getRateService(this, appWidgetId);
		List<MtGoxTickerData> tickerData = db.getTickerData(System.currentTimeMillis() - ONE_DAY_IN_MS, rateService);
		if(tickerData.size() > 0) {
			setupChart(tickerData);
			emptyChart = false;
		} else {
			emptyChart = true;
		}

		setTitle(rateService.getName() + " - Last 24 hours");

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		setContentView(R.layout.popup_graph_layout);
	}

	private void setupChart(List<MtGoxTickerData> dataList) {
		dataset = new XYMultipleSeriesDataset();
		renderer = initChart();
		XYSeries highSeries = addSeries(getString(R.string.high), Color.parseColor("#00CC00"), false);
		XYSeries lowSeries = addSeries(getString(R.string.low), Color.parseColor("#CC0000"), false);
		XYSeries sellSeries = addSeries(getString(R.string.sell), Color.parseColor("#AAFFAA"), false);
		XYSeries buySeries = addSeries(getString(R.string.buy), Color.parseColor("#FFAAAA"), false);
		XYSeries lastSeries = addSeries(getString(R.string.last), Color.WHITE, true);
		for(MtGoxTickerData data : dataList) {
			addDataToSeriesIfNotNull(highSeries, data.getTimestamp().getTime(), data.getHigh());
			addDataToSeriesIfNotNull(lowSeries, data.getTimestamp().getTime(), data.getLow());
			addDataToSeriesIfNotNull(sellSeries, data.getTimestamp().getTime(), data.getSell());
			addDataToSeriesIfNotNull(buySeries, data.getTimestamp().getTime(), data.getBuy());
			addDataToSeriesIfNotNull(lastSeries, data.getTimestamp().getTime(), data.getLast());
		}
	}

	private void addDataToSeriesIfNotNull(XYSeries series, long time, Double value) {
		if(value != null && value > 0) {
			series.add(time, value);
		}
	}

	private XYSeries addSeries(String title, int color, boolean mainLine) {
		prepareSeries(color, mainLine);
		XYSeries series = new XYSeries(title);
		dataset.addSeries(series);
		return series;
	}

	private XYMultipleSeriesRenderer initChart() {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		renderer.setXLabels(8);
		renderer.setYLabels(12);
		renderer.setXTitle("@");
		renderer.setYTitle("$");
		renderer.setShowGrid(true);
		renderer.setXAxisMin(System.currentTimeMillis() - ONE_DAY_IN_MS - PADDING_MS);
		renderer.setXAxisMax(System.currentTimeMillis() + PADDING_MS);
		return renderer;
	}

	private XYSeriesRenderer prepareSeries(int color, boolean mainLine) {
		XYSeriesRenderer seriesRenderer = new XYSeriesRenderer();
		seriesRenderer.setColor(color);
		if(mainLine) {
			seriesRenderer.setLineWidth(2);
		} else {
			seriesRenderer.setLineWidth(1);
		}
		renderer.addSeriesRenderer(seriesRenderer);
		return seriesRenderer;
	}

	@Override
	protected void onResume() {
		super.onResume();
		LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
		if(emptyChart) {
			TextView tview = new TextView(this);
			tview.setText(R.string.empty_graph);
			layout.addView(tview, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		} else {
			chartView = ChartFactory.getTimeChartView(this, dataset, renderer, "HH:mm");
			layout.addView(chartView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}
	}

	// http://developer.android.com/guide/topics/ui/menus.html
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.graph_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(Constants.TAG, "GraphPopupActivity.onOptionsItemSelected: ");
		if(item.getItemId() == R.id.refreshMenu) {
			finish();
			String toastText = "Refreshing rate from " + MtGoxPreferences.getRateService(this, appWidgetId).getName() + "...";
			Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
			MtGoxWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
