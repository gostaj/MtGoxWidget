package st.brothas.mtgoxwidget;

import org.json.JSONObject;

import java.util.*;

import static st.brothas.mtgoxwidget.CurrencyConversion.*;
import static st.brothas.mtgoxwidget.MtGoxTickerUtil.*;

public enum RateService {
    MTGOX(1, "Mt Gox",
            new TickerUrl(BTC_USD, "https://data.mtgox.com/api/2/BTCUSD/money/ticker"),
            new TickerUrl(BTC_EUR, "https://data.mtgox.com/api/2/BTCEUR/money/ticker"),
            new TickerUrl(BTC_GBP, "https://data.mtgox.com/api/2/BTCGBP/money/ticker"),
            new TickerUrl(BTC_AUD, "https://data.mtgox.com/api/2/BTCAUD/money/ticker"),
            new TickerUrl(BTC_CAD, "https://data.mtgox.com/api/2/BTCCAD/money/ticker"),
            new TickerUrl(BTC_CNY, "https://data.mtgox.com/api/2/BTCCNY/money/ticker"),
            new TickerUrl(BTC_JPY, "https://data.mtgox.com/api/2/BTCJPY/money/ticker"),
            new TickerUrl(BTC_RUB, "https://data.mtgox.com/api/2/BTCRUB/money/ticker"),
            new TickerUrl(BTC_SEK, "https://data.mtgox.com/api/2/BTCSEK/money/ticker")),

            // TradeHill shut down trading February 13, 2012.
    TRADEHILL(2,"TradeHill"),

    CAMPBX(3, "Camp BX", new TickerUrl(BTC_USD, "http://campbx.com/api/xticker.php")),

    // ExchB is closed since October 16, 2011.
    EXCHB(4,"ExchB"),

    // Bitfloor is closed since April 17, 2013.
    BITFLOOR(5,"Bitfloor", new TickerUrl(BTC_USD, "https://api.bitfloor.com/ticker/1")),
    BITSTAMP(6,"Bitstamp", new TickerUrl(BTC_USD, "https://www.bitstamp.net/api/ticker/")),
    CRYPTOXCHANGE (7, "Crypto X Change", new TickerUrl(BTC_USD, "http://cryptoxchange.com/api/v0/data/BTCUSD/ticker")),
    BTCE (8, "BTC-e", new TickerUrl(BTC_USD, "https://btc-e.com/api/2/btc_usd/ticker"));

    private final int id;
    private final String name;
    private final TickerUrl[] tickerUrls;
    private static final Map<Integer,RateService> lookup = new HashMap<Integer,RateService>();

    RateService(int id, String name, TickerUrl... tickerUrls) {
        this.id = id;
        this.name = name;
        this.tickerUrls = tickerUrls;
    }

    static {
        for(RateService s : EnumSet.allOf(RateService.class))
            lookup.put(s.getId(), s);
    }

    public MtGoxTickerData parseJSON(JSONObject json) {
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
                // {"high": "5.5000","low": "5.1700","vol": "413.1294","last": "5.1700","buy": "5.1800",
                //  "sell": "5.3680","market": "BTCUSD","ReturnCodes": 1,"err": "","stamp": ""
                tickerData.setLast(tryToParseDouble(getJSONTickerKey(json, "last")));
                tickerData.setLow(tryToParseDouble(getJSONTickerKey(json, "low")));
                tickerData.setHigh(tryToParseDouble(getJSONTickerKey(json, "high")));
                tickerData.setBuy(tryToParseDouble(getJSONTickerKey(json, "buy")));
                tickerData.setSell(tryToParseDouble(getJSONTickerKey(json, "sell")));
                break;
            case BITSTAMP:
                // {"high": "5.19", "last": "5.17", "bid": "5.17", "volume": "479.80406816", "low": "5.10", "ask": "5.20"}
                tickerData.setLast(tryToParseDouble(getJSONTickerKey(json, "last")));
                tickerData.setLow(tryToParseDouble(getJSONTickerKey(json, "low")));
                tickerData.setHigh(tryToParseDouble(getJSONTickerKey(json, "high")));
                tickerData.setBuy(tryToParseDouble(getJSONTickerKey(json, "bid")));
                tickerData.setSell(tryToParseDouble(getJSONTickerKey(json, "ask")));
                break;
            case BTCE:
                // {"ticker":{"high":143.99001,"low":52.999,"avg":98.494505,"vol":7945244.62099,"vol_cur":99804.06692,"last":65.989,"buy":65.201,"sell":65.11,"server_time":1365771883}}
                tickerData.setLast(tryToParseDouble(getJSONTickerKeyFromObject(json, "ticker", "last")));
                tickerData.setLow(tryToParseDouble(getJSONTickerKeyFromObject(json, "ticker", "low")));
                tickerData.setHigh(tryToParseDouble(getJSONTickerKeyFromObject(json, "ticker", "high")));
                tickerData.setBuy(tryToParseDouble(getJSONTickerKeyFromObject(json, "ticker", "buy")));
                tickerData.setSell(tryToParseDouble(getJSONTickerKeyFromObject(json, "ticker", "sell")));
                break;
            default:
                // Mt Gox:
                // {"result":"success","data":{"high":{"value":"74.90000","value_int":"7490000","display":"$74.90","display_short":"$74.90","currency":"USD"},
                // "low":{"value":"65.28800","value_int":"6528800","display":"$65.29","display_short":"$65.29","currency":"USD"},
                // "avg":{"value":"70.57377","value_int":"7057377","display":"$70.57","display_short":"$70.57","currency":"USD"},
                // "vwap":{"value":"70.43952","value_int":"7043952","display":"$70.44","display_short":"$70.44","currency":"USD"},
                // "vol":{"value":"97482.89064204","value_int":"9748289064204","display":"97,482.89\u00a0BTC","display_short":"97,482.89\u00a0BTC","currency":"BTC"},
                // "last_local":{"value":"71.32568","value_int":"7132568","display":"$71.33","display_short":"$71.33","currency":"USD"},
                // "last":{"value":"71.32568","value_int":"7132568","display":"$71.33","display_short":"$71.33","currency":"USD"},
                // "last_orig":{"value":"47.54586","value_int":"4754586","display":"\u00a347.55","display_short":"\u00a347.55","currency":"GBP"},
                // "last_all":{"value":"72.07386","value_int":"7207386","display":"$72.07","display_short":"$72.07","currency":"USD"},
                // "buy":{"value":"70.06874","value_int":"7006874","display":"$70.07","display_short":"$70.07","currency":"USD"},
                // "sell":{"value":"71.32568","value_int":"7132568","display":"$71.33","display_short":"$71.33","currency":"USD"},
                // "now":"1363938453428998"}}

                tickerData.setLast(tryToParseDouble(getJSONTickerKeyFromObjects(json, "data", "last", "value")));
                tickerData.setLow(tryToParseDouble(getJSONTickerKeyFromObjects(json, "data", "low", "value")));
                tickerData.setHigh(tryToParseDouble(getJSONTickerKeyFromObjects(json, "data", "high", "value")));
                tickerData.setBuy(tryToParseDouble(getJSONTickerKeyFromObjects(json, "data", "buy", "value")));
                tickerData.setSell(tryToParseDouble(getJSONTickerKeyFromObjects(json, "data", "sell", "value")));
        }

        return tickerData;
    }

    public String getName() {
        return name;
    }

    public String getTickerUrl(CurrencyConversion currencyConversion) {
        for (TickerUrl tickerUrl : tickerUrls) {
            if (tickerUrl.currencyConversion.equals(currencyConversion))
                return tickerUrl.tickerUrl;
        }
        throw new IllegalArgumentException("Currency " + currencyConversion + " is not supported by " + name);
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

    public List<CurrencyConversion> getCurrencyConversions() {
        List<CurrencyConversion> currencyConversions = new ArrayList<CurrencyConversion>();
        for (TickerUrl tickerUrl : tickerUrls) {
            currencyConversions.add(tickerUrl.currencyConversion);
        }
        return currencyConversions;
    }

    private static class TickerUrl {
        private final CurrencyConversion currencyConversion;
        private final String tickerUrl;

        TickerUrl(CurrencyConversion currencyConversion, String tickerUrl) {
            this.currencyConversion = currencyConversion;
            this.tickerUrl = tickerUrl;
        }
    }
}
