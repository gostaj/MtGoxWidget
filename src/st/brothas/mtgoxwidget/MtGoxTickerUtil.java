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

import static st.brothas.mtgoxwidget.MtGoxWidgetProvider.LOG_TAG;

/**
  * Util class for the Mt Gox Ticker.
  */
public class MtGoxTickerUtil {

    // TODO: Change to one method with "String... objects"
    public static String getJSONTickerKeyFromObjects(JSONObject json, String objectNameLevel1, String objectNameLevel2,
                                                     String key) {
        JSONObject tickerObject;
		try {
			tickerObject = json.getJSONObject(objectNameLevel1).getJSONObject(objectNameLevel2);
			return tickerObject.getString(key);
		} catch (JSONException e) {
			Log.e(LOG_TAG, "Error when getting JSON object1: '" + objectNameLevel1 + "', object2: '" + objectNameLevel2 +
                    "'," + " key: '" + key + "', from json: '" + json + "'", e);
		}
		return "N/A";
    }

    public static String getJSONTickerKeyFromObject(JSONObject json, String objectName, String key) {
        try {
            return json.getJSONObject(objectName).getString(key);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error when getting JSON object: '" + objectName + "'," + " key: '" + key +
                    "', from json: '" + json + "'", e);
        }
        return "N/A";
    }

    public static String getJSONTickerKey(JSONObject json, String key) {
		try {
			return json.getString(key);
		} catch (JSONException e) {
			Log.e(LOG_TAG, "Error when getting JSON key: '" + key + "' from json: '" + json + "'", e);
		}
		return "N/A";
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
