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
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
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
public class GraphPopupActivity extends Activity {

    private GraphicalView chartView;
    private XYMultipleSeriesDataset dataset;
    private XYMultipleSeriesRenderer renderer;
//    private XYSeries currentSeries;
//    private XYSeriesRenderer currentRenderer;
    private static final long PADDING_MS = 15*60*1000; // Add 15 minutes to each side of the graph
    private static final long ONE_DAY_IN_MS = 24*60*60*1000; // 24 hours
    private boolean emptyChart = false;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        MtGoxDataOpenHelper db = new MtGoxDataOpenHelper(this);
        List<MtGoxTickerData> tickerData = db.getTickerData(System.currentTimeMillis() - ONE_DAY_IN_MS);
        if (tickerData.size() > 0) {
            setupChart(tickerData);
            emptyChart = false;
        } else {
            emptyChart = true;
        }

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
            highSeries.add(data.getTimestamp().getTime(), data.getHigh());
            lowSeries.add(data.getTimestamp().getTime(), data.getLow());
            sellSeries.add(data.getTimestamp().getTime(), data.getSell());
            buySeries.add(data.getTimestamp().getTime(), data.getBuy());
            lastSeries.add(data.getTimestamp().getTime(), data.getLast());
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
            layout.addView(tview, new LayoutParams(LayoutParams.FILL_PARENT,
                    LayoutParams.FILL_PARENT));
        } else {
            chartView = ChartFactory.getTimeChartView(this, dataset, renderer, "HH:mm");
            layout.addView(chartView, new LayoutParams(LayoutParams.FILL_PARENT,
                    LayoutParams.FILL_PARENT));
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


}
