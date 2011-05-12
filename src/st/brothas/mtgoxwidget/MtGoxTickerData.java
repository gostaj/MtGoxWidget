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

import android.util.Log;
import org.json.JSONObject;

import java.util.Date;

import static st.brothas.mtgoxwidget.MtGoxWidgetProvider.LOG_TAG;


/**
 *
 */
public class MtGoxTickerData {

    Double last = 0D;
    Double low = 0D;
    Double high = 0D;
    Double buy = 0D;
    Double sell = 0D;
    Date timestamp;

    public MtGoxTickerData(JSONObject json) {
        this();
        last = parseDouble(MtGoxTickerUtil.getJSONTickerKey(json, "last"));
        low = parseDouble(MtGoxTickerUtil.getJSONTickerKey(json, "low"));
        high = parseDouble(MtGoxTickerUtil.getJSONTickerKey(json, "high"));
        buy = parseDouble(MtGoxTickerUtil.getJSONTickerKey(json, "buy"));
        sell = parseDouble(MtGoxTickerUtil.getJSONTickerKey(json, "sell"));
    }

    public MtGoxTickerData() {
        timestamp = new Date();
    }

    public Double getLast() {
        return last;
    }

    public void setLast(Double last) {
        this.last = last;
    }

    public Double getLow() {
        return low;
    }

    public void setLow(Double low) {
        this.low = low;
    }

    public Double getHigh() {
        return high;
    }

    public void setHigh(Double high) {
        this.high = high;
    }

    public Double getBuy() {
        return buy;
    }

    public void setBuy(Double buy) {
        this.buy = buy;
    }

    public Double getSell() {
        return sell;
    }

    public void setSell(Double sell) {
        this.sell = sell;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    private static Double parseDouble(String last) {
        try {
            return Double.parseDouble(last);
        } catch (NumberFormatException e) {
            Log.w(LOG_TAG, "Unable to parse float: '" + last + "'");
            return null;
        }
    }

    @Override
    public String toString() {
        return "MtGoxTickerData{" +
                "buy=" + buy +
                ", last=" + last +
                ", low=" + low +
                ", high=" + high +
                ", sell=" + sell +
                ", timestamp=" + timestamp +
                '}';
    }
}
