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


import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Util class for the Mt Gox Ticker.
 */
public class MtGoxTickerUtil {
	public static String getJSONTickerKeyFromObject(JSONObject json, String objectName, String key) {
		Log.d(Constants.TAG, "MtGoxTickerUtil.getJSONTickerKeyFromObject: ");
		JSONObject tickerObject;
		try {
			tickerObject = json.getJSONObject(objectName);
			return tickerObject.getString(key);
		} catch(JSONException e) {
			Log.e(Constants.TAG, "Error when getting JSON key: '" + key + "' from json: '" + json + "'", e);
		}
		return "N/A";
	}

	public static String getJSONTickerKey(JSONObject json, String key) {
		Log.d(Constants.TAG, "MtGoxTickerUtil.getJSONTickerKey: ");
		try {
			return json.getString(key);
		} catch(JSONException e) {
			Log.e(Constants.TAG, "Error when getting JSON key: '" + key + "' from json: '" + json + "'", e);
		}
		return "N/A";
	}

	public static Double tryToParseDouble(String last) {
		Log.d(Constants.TAG, "MtGoxTickerUtil.tryToParseDouble: ");
		try {
			return Double.parseDouble(last);
		} catch(NumberFormatException e) {
			Log.w(Constants.TAG, "Unable to parse float: '" + last + "'");
			return null;
		}
	}
}
