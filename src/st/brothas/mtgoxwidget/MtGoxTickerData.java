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

import java.util.Date;


/**
 *
 */
public class MtGoxTickerData {

    private RateService rateService;
    private CurrencyConversion currencyConversion;
    private Double last;
    private Double low;
    private Double high;
    private Double buy;
    private Double sell;
    private Date timestamp;

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

    public RateService getRateService() {
        return rateService;
    }

    public void setRateService(RateService rateService) {
        this.rateService = rateService;
    }

    public CurrencyConversion getCurrencyConversion() {
        return currencyConversion;
    }

    public void setCurrencyConversion(CurrencyConversion currencyConversion) {
        this.currencyConversion = currencyConversion;
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
