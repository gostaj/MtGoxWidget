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
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Arrays;

import static st.brothas.mtgoxwidget.MtGoxWidgetProvider.LOG_TAG;

/**
  * Util class for the Mt Gox Ticker.
  */
public class MtGoxTickerUtil {

    public static String getJSONTickerKey(JSONObject json, String... objectNames) {
        JSONObject tickerObject = json;
        String key = objectNames[objectNames.length - 1];
        
        for (int i = 0; i < objectNames.length - 1; i++) {
            try {
                tickerObject = tickerObject.getJSONObject(objectNames[i]);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error when getting JSON at path: '" + Arrays.toString(objectNames) + "', from json: '" + json + "'", e);
                return "N/A";
            }
        }

        try {
            return tickerObject.getString(key);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error when getting JSON at path: '" + Arrays.toString(objectNames) + "', from json: '" + json + "'", e);
            return "N/A";
        }
    }

    public static Double tryToParseDouble(String last) {
        try {
            return Double.parseDouble(last);
        } catch (NumberFormatException e) {
            Log.w(LOG_TAG, "Unable to parse float: '" + last + "'");
            return null;
        }
    }



}
