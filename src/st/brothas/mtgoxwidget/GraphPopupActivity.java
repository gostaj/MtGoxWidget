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

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.List;


/**
 * Represents the graph that shows the ticker data change in time.
 */
public class GraphPopupActivity extends ActionBarActivity {

    private enum GraphTimeframe {OneDay("day", 1, "HH:mm"), OneWeek("week", 7, "MM-dd");
        public final String description;
        public final int days;
        public String timeFormat;

        GraphTimeframe(String description, int days, String timeFormat) {
            this.description = description;
            this.days = days;
            this.timeFormat = timeFormat;
        }
    }

    private GraphicalView chartView;
    private XYMultipleSeriesDataset dataset;
    private XYMultipleSeriesRenderer renderer;
//    private XYSeries currentSeries;
//    private XYSeriesRenderer currentRenderer;
    private static final long PADDING_MS = 15*60*1000; // Add 15 minutes to each side of the graph
    private static final long ONE_DAY_IN_MS = 24*60*60*1000; // 24 hours
    private boolean emptyChart = false;
    private int appWidgetId;
    private GraphTimeframe graphTimeframe = GraphTimeframe.OneDay;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        appWidgetId = getIntent().getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);

        showGraphPopup();
    }

    private void showGraphPopup() {
        MtGoxDataOpenHelper db = new MtGoxDataOpenHelper(this);
        WidgetPreferences preferences = MtGoxPreferencesActivity.getWidgetPreferences(this, appWidgetId);
        List<MtGoxTickerData> tickerData = db.getTickerData(System.currentTimeMillis() -
                (ONE_DAY_IN_MS * graphTimeframe.days), preferences);
        if (tickerData.size() > 0) {
            setupChart(tickerData);
            emptyChart = false;
        } else {
            emptyChart = true;
        }

        setTitle(preferences.getRateService().getName() + " " +
                 preferences.getCurrencyConversion().description + " - Last " + graphTimeframe.description);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);
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
        for (MtGoxTickerData data : dataList) {
            //Log.d("Graphdata", data.toString());
            addDataToSeriesIfNotNull(highSeries, data.getTimestamp().getTime(), data.getHigh());
            addDataToSeriesIfNotNull(lowSeries, data.getTimestamp().getTime(), data.getLow());
            addDataToSeriesIfNotNull(sellSeries, data.getTimestamp().getTime(), data.getSell());
            addDataToSeriesIfNotNull(buySeries, data.getTimestamp().getTime(), data.getBuy());
            addDataToSeriesIfNotNull(lastSeries, data.getTimestamp().getTime(), data.getLast());
        }

    }

    private void addDataToSeriesIfNotNull(XYSeries series, long time, Double value) {
        if (value != null && value > 0) {
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
        renderer.setXTitle("");
        renderer.setYTitle("Value"); // TODO: Change to something else
        renderer.setShowGrid(true);
        renderer.setXAxisMin(System.currentTimeMillis() - (ONE_DAY_IN_MS * graphTimeframe.days) - PADDING_MS);
        renderer.setXAxisMax(System.currentTimeMillis() + PADDING_MS);
        renderer.setAxisTitleTextSize(20);
        renderer.setChartTitleTextSize(20);
        renderer.setLegendTextSize(20);
        renderer.setLabelsTextSize(22);
        renderer.setFitLegend(true);
        return renderer;
    }

    private XYSeriesRenderer prepareSeries(int color, boolean mainLine) {
        XYSeriesRenderer seriesRenderer = new XYSeriesRenderer();
        seriesRenderer.setColor(color);
        if (mainLine) {
            //seriesRenderer.setPointStyle(PointStyle.CIRCLE);
            //seriesRenderer.setFillPoints(true);
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
//      if (chartView == null) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
        if (emptyChart) {
            TextView tview = new TextView(this);
            tview.setText(R.string.empty_graph);
            layout.addView(tview, new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));
        } else {

            chartView = ChartFactory.getTimeChartView(this, dataset, renderer, graphTimeframe.timeFormat);
            layout.addView(chartView, new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));

        }
//      } else if (chartView != null) {
//        chartView.repaint();
//      }
    }

//    @Override
//    protected void onRestoreInstanceState(Bundle savedState) {
//      super.onRestoreInstanceState(savedState);
//      dataset = (XYMultipleSeriesDataset) savedState.getSerializable("dataset");
//      renderer = (XYMultipleSeriesRenderer) savedState.getSerializable("renderer");
//      currentSeries = (XYSeries) savedState.getSerializable("current_series");
//      currentRenderer = (XYSeriesRenderer) savedState.getSerializable("current_renderer");
//    }
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//      super.onSaveInstanceState(outState);
//      outState.putSerializable("dataset", dataset);
//      outState.putSerializable("renderer", renderer);
//      outState.putSerializable("current_series", currentSeries);
//      outState.putSerializable("current_renderer", currentRenderer);
//    }


    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if(graphTimeframe.equals(GraphTimeframe.OneDay)) {
            menu.findItem(R.id.switchTimeframe).setTitle("Show last " + GraphTimeframe.OneWeek.description);
            menu.findItem(R.id.switchTimeframe).setIcon(R.drawable.ic_menu_week);

        } else {
            menu.findItem(R.id.switchTimeframe).setTitle("Show last " + GraphTimeframe.OneDay.description);
            menu.findItem(R.id.switchTimeframe).setIcon(R.drawable.ic_menu_day);
        }
        return super.onPrepareOptionsMenu(menu);
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
        switch (item.getItemId()) {
            case R.id.refreshMenu:
                finish();
                String toastText = "Refreshing rate from " +
                        MtGoxPreferencesActivity.getWidgetPreferences(this, appWidgetId).getRateService().getName()+ "...";
                Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                MtGoxWidgetProvider.updateAppWidgetAsync(this, appWidgetManager, appWidgetId);
                return true;
            case R.id.switchTimeframe:
                if(graphTimeframe.equals(GraphTimeframe.OneDay)) {
                    graphTimeframe = GraphTimeframe.OneWeek;
                } else {
                    graphTimeframe = GraphTimeframe.OneDay;
                }

                showGraphPopup();
                onResume();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
