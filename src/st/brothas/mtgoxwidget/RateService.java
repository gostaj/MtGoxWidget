package st.brothas.mtgoxwidget;

import static st.brothas.mtgoxwidget.MtGoxTickerUtil.getJSONTickerKey;
import static st.brothas.mtgoxwidget.MtGoxTickerUtil.getJSONTickerKeyFromObject;
import static st.brothas.mtgoxwidget.MtGoxTickerUtil.tryToParseDouble;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.util.Log;

public enum RateService {
	MTGOX(1, "Mt Gox", "https://data.mtgox.com/code/data/ticker.php"), TRADEHILL(2, "TradeHill", "https://api.tradehill.com/APIv1/USD/Ticker"),
	// TradeHill shut down trading February 13, 2012.
	CAMPBX(3, "Camp BX", "http://campbx.com/api/xticker.php"), EXCHB(4, "ExchB", "https://www.exchangebitcoins.com/data/ticker"),
	// ExchB is closed since October 16, 2011.
	BITFLOOR(5, "Bitfloor", "https://api.bitfloor.com/ticker/1"), BITSTAMP(6, "Bitstamp", "https://www.bitstamp.net/api/ticker/"), CRYPTOXCHANGE(7,
			"Crypto X Change", "http://cryptoxchange.com/api/v0/data/BTCUSD/ticker");

	private final int id;
	private final String name;
	private final String tickerUrl;
	// private static final SparseArray<RateService> lookup = new
	// SparseArray<RateService>();
	private static final Map<Integer, RateService> lookup = new HashMap<Integer, RateService>();

	private RateService(int id, String name, String tickerUrl) {
		Log.d(Constants.TAG, "RateService.RateService: ");
		this.id = id;
		this.name = name;
		this.tickerUrl = tickerUrl;
	}

	static {
		for (RateService s : EnumSet.allOf(RateService.class)) {
			lookup.put(s.getId(), s);
		}
	}

	public MtGoxTickerData parseJSON(JSONObject json) {
		Log.d(Constants.TAG, "RateService.parseJSON: " + json);
		MtGoxTickerData tickerData = new MtGoxTickerData();

		tickerData.setRateService(this);
		switch (this) {
		case CAMPBX:
			// {"Last Trade":"11.75","Best Bid":"11.40","Best Ask":"11.67"}
			tickerData.setLast(tryToParseDouble(getJSONTickerKey(json, "Last Trade")));
			tickerData.setBuy(tryToParseDouble(getJSONTickerKey(json, "Best Bid")));
			tickerData.setSell(tryToParseDouble(getJSONTickerKey(json, "Best Ask")));
			break;
		case CRYPTOXCHANGE:
			// {"high": "5.5000","low": "5.1700","vol": "413.1294","last":
			// "5.1700","buy": "5.1800",
			// "sell": "5.3680","market": "BTCUSD","ReturnCodes": 1,"err":
			// "","stamp": ""
			tickerData.setLast(tryToParseDouble(getJSONTickerKey(json, "last")));
			tickerData.setLow(tryToParseDouble(getJSONTickerKey(json, "low")));
			tickerData.setHigh(tryToParseDouble(getJSONTickerKey(json, "high")));
			tickerData.setBuy(tryToParseDouble(getJSONTickerKey(json, "buy")));
			tickerData.setSell(tryToParseDouble(getJSONTickerKey(json, "sell")));
			break;
		case BITFLOOR:
			// {"price":"4.89000000","size":"1.00000000","timestamp":1330185448.833175}
			tickerData.setLast(tryToParseDouble(getJSONTickerKey(json, "price")));
			break;
		case BITSTAMP:
			// {"high": "5.19", "last": "5.17", "bid": "5.17", "volume":
			// "479.80406816", "low": "5.10", "ask": "5.20"}
			tickerData.setLast(tryToParseDouble(getJSONTickerKey(json, "last")));
			tickerData.setLow(tryToParseDouble(getJSONTickerKey(json, "low")));
			tickerData.setHigh(tryToParseDouble(getJSONTickerKey(json, "high")));
			tickerData.setBuy(tryToParseDouble(getJSONTickerKey(json, "bid")));
			tickerData.setSell(tryToParseDouble(getJSONTickerKey(json, "ask")));
			break;
		default:
			// Mt Gox:
			// {"ticker":{"high":11.60000000,"low":10.88510000,"vol":624.15380000,
			// "buy":11.4800,"sell":11.5998,"last":11.47010000}}
			tickerData.setLast(tryToParseDouble(getJSONTickerKeyFromObject(json, "ticker", "last")));
			tickerData.setLow(tryToParseDouble(getJSONTickerKeyFromObject(json, "ticker", "low")));
			tickerData.setHigh(tryToParseDouble(getJSONTickerKeyFromObject(json, "ticker", "high")));
			tickerData.setBuy(tryToParseDouble(getJSONTickerKeyFromObject(json, "ticker", "buy")));
			tickerData.setSell(tryToParseDouble(getJSONTickerKeyFromObject(json, "ticker", "sell")));
			tickerData.setVwap(tryToParseDouble(getJSONTickerKeyFromObject(json, "ticker", "vwap")));
		}
		return tickerData;
	}

	public String getName() {
		return name;
	}

	public String getTickerUrl() {
		return tickerUrl;
	}

	public Integer getId() {
		return id;
	}

	public static RateService getDefaultService() {
		return MTGOX;
	}

	public static RateService getById(int id) {
		return lookup.get(id);
	}
}
