package st.brothas.mtgoxwidget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

import static st.brothas.mtgoxwidget.CurrencyConversion.*;
import static st.brothas.mtgoxwidget.MtGoxTickerUtil.*;

public enum RateService {
            // Mtgox shut down trading February 2014.
    MTGOX(1, "Mt Gox",
            new TickerUrl(BTC_USD, "https://data.mtgox.com/api/2/BTCUSD/money/ticker"),
            new TickerUrl(BTC_EUR, "https://data.mtgox.com/api/2/BTCEUR/money/ticker"),
            new TickerUrl(BTC_GBP, "https://data.mtgox.com/api/2/BTCGBP/money/ticker"),
            new TickerUrl(BTC_AUD, "https://data.mtgox.com/api/2/BTCAUD/money/ticker"),
            new TickerUrl(BTC_CAD, "https://data.mtgox.com/api/2/BTCCAD/money/ticker"),
            new TickerUrl(BTC_CNY, "https://data.mtgox.com/api/2/BTCCNY/money/ticker"),
            new TickerUrl(BTC_JPY, "https://data.mtgox.com/api/2/BTCJPY/money/ticker"),
            new TickerUrl(BTC_RUB, "https://data.mtgox.com/api/2/BTCRUB/money/ticker"),
            new TickerUrl(BTC_SEK, "https://data.mtgox.com/api/2/BTCSEK/money/ticker"),
            new TickerUrl(BTC_PLN, "https://data.mtgox.com/api/2/BTCPLN/money/ticker")),

            // TradeHill shut down trading February 13, 2012.
    TRADEHILL(2,"TradeHill"),

    CAMPBX(3, "Camp BX", new TickerUrl(BTC_USD, "http://campbx.com/api/xticker.php")),

    // ExchB is closed since October 16, 2011.
    EXCHB(4,"ExchB"),

    // Bitfloor is closed since April 17, 2013.
    BITFLOOR(5,"Bitfloor", new TickerUrl(BTC_USD, "https://api.bitfloor.com/ticker/1")),

    BITSTAMP(6,"Bitstamp", new TickerUrl(BTC_USD, "https://www.bitstamp.net/api/ticker/")),

    // CryptoXChange is closed since November 19, 2012.
    CRYPTOXCHANGE (7, "Crypto X Change", new TickerUrl(BTC_USD, "http://cryptoxchange.com/api/v0/data/BTCUSD/ticker")),

    BTCE (8, "BTC-e",
            new TickerUrl(BTC_USD, "https://btc-e.com/api/2/btc_usd/ticker"),
            new TickerUrl(BTC_EUR, "https://btc-e.com/api/2/btc_eur/ticker"),
            new TickerUrl(LTC_USD, "https://btc-e.com/api/2/ltc_usd/ticker"),
            new TickerUrl(LTC_EUR, "https://btc-e.com/api/2/ltc_eur/ticker"),
            new TickerUrl(LTC_BTC, "https://btc-e.com/api/2/ltc_btc/ticker")),

    COINBASE (9, "Coinbase", new TickerUrl(BTC_USD, "https://coinbase.com/api/v1/currencies/exchange_rates")),

    BITCUREX(10, "Bitcurex",
            new TickerUrl(BTC_PLN, "https://pln.bitcurex.com/data/ticker.json"),
            new TickerUrl(BTC_EUR, "https://eur.bitcurex.com/data/ticker.json")),

    BITPAY(11, "Bitpay", new TickerUrl(BTC_USD, "https://bitpay.com/api/rates")),

    BTER(12, "BTER",
            new TickerUrl(BTC_CNY, "https://bter.com/api/1/ticker/btc_cny"),
            new TickerUrl(LTC_CNY, "https://bter.com/api/1/ticker/ltc_cny"),
            new TickerUrl(LTC_BTC, "https://bter.com/api/1/ticker/ltc_btc"),
            new TickerUrl(QRK_BTC, "https://bter.com/api/1/ticker/qrk_btc"),
            new TickerUrl(QRK_CNY, "https://bter.com/api/1/ticker/qrk_cny")),

    BITKONAN(13, "BKonan", new TickerUrl(BTC_USD, "https://bitkonan.com/api/ticker/")),

    THEROCK(14, "TheRock",
            new TickerUrl(BTC_USD, "https://www.therocktrading.com/api/ticker/BTCUSD"),
            new TickerUrl(BTC_EUR, "https://www.therocktrading.com/api/ticker/BTCEUR"),
            new TickerUrl(LTC_BTC, "https://www.therocktrading.com/api/ticker/LTCBTC"),
            new TickerUrl(LTC_USD, "https://www.therocktrading.com/api/ticker/LTCUSD"),
            new TickerUrl(LTC_EUR, "https://www.therocktrading.com/api/ticker/LTCEUR")),

    BIT2C(15, "Bit2C", new TickerUrl(BTC_ILS, "https://www.bit2c.co.il/Exchanges/NIS/Ticker.json"));


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

    public MtGoxTickerData parseJSON(String json) {
        MtGoxTickerData tickerData = new MtGoxTickerData();

        tickerData.setRateService(this);
        switch (this) {
            case CAMPBX:
                // {"Last Trade":"11.75","Best Bid":"11.40","Best Ask":"11.67"}
                tickerData.setLast(tryToParseDouble(getJSONTickerKey(parseJSONObject(json), "Last Trade")));
                tickerData.setBuy(tryToParseDouble(getJSONTickerKey(parseJSONObject(json), "Best Bid")));
                tickerData.setSell(tryToParseDouble(getJSONTickerKey(parseJSONObject(json), "Best Ask")));
                break;
            case COINBASE:
                // {..."btc_to_usd":"598.56472"...
                tickerData.setLast(tryToParseDouble(getJSONTickerKey(parseJSONObject(json), "btc_to_usd")));
                break;
            case THEROCK:
                // {"result":[{"symbol":"BTCUSD","bid":"610.0200","ask":"660.0000","last":"658.0000","volume":"228.9800","open":"658.0000","high":"660.0000","low":"658.0000","close":"660.0000"}]}
                try {
                    json = parseJSONObject(json).getJSONArray("result").getString(0);
                } catch (JSONException e) {
                  break;
                }
            case BITSTAMP:
                // {"high": "5.19", "last": "5.17", "bid": "5.17", "volume": "479.80406816", "low": "5.10", "ask": "5.20"}
            case BITKONAN:
                // {"last":"660.00","high":"660.00","low":"650.00","bid":"555.00","ask":"700.00","open":"553.00","volume":"1.36182191"}
                tickerData.setLast(tryToParseDouble(getJSONTickerKey(parseJSONObject(json), "last")));
                tickerData.setLow(tryToParseDouble(getJSONTickerKey(parseJSONObject(json), "low")));
                tickerData.setHigh(tryToParseDouble(getJSONTickerKey(parseJSONObject(json), "high")));
                tickerData.setBuy(tryToParseDouble(getJSONTickerKey(parseJSONObject(json), "bid")));
                tickerData.setSell(tryToParseDouble(getJSONTickerKey(parseJSONObject(json), "ask")));
                break;
            case BTCE:
                // {"ticker":{"high":143.99001,"low":52.999,"avg":98.494505,"vol":7945244.62099,"vol_cur":99804.06692,"last":65.989,"buy":65.201,"sell":65.11,"server_time":1365771883}}
                tickerData.setLast(tryToParseDouble(getJSONTickerKeyFromObject(parseJSONObject(json), "ticker", "last")));
                tickerData.setLow(tryToParseDouble(getJSONTickerKeyFromObject(parseJSONObject(json), "ticker", "low")));
                tickerData.setHigh(tryToParseDouble(getJSONTickerKeyFromObject(parseJSONObject(json), "ticker", "high")));
                tickerData.setBuy(tryToParseDouble(getJSONTickerKeyFromObject(parseJSONObject(json), "ticker", "buy")));
                tickerData.setSell(tryToParseDouble(getJSONTickerKeyFromObject(parseJSONObject(json), "ticker", "sell")));
                break;
            case BITCUREX:
                // {"high":2199.76,"low":1954,"avg":2076.88,"vwap":2082.0488574,"vol":528.33722382,"last":2088,"buy":2088,"sell":2089,"time":1387543316}
            case BTER:
                // {"result":"true","last":0.03099,"high":0.0319,"low":0.02589,"avg":0.02842,"sell":0.03099,"buy":0.03,"vol_ltc":6513.7652,"vol_btc":185.14607}
                tickerData.setLast(tryToParseDouble(getJSONTickerKey(parseJSONObject(json), "last")));
                tickerData.setLow(tryToParseDouble(getJSONTickerKey(parseJSONObject(json), "low")));
                tickerData.setHigh(tryToParseDouble(getJSONTickerKey(parseJSONObject(json), "high")));
                tickerData.setBuy(tryToParseDouble(getJSONTickerKey(parseJSONObject(json), "buy")));
                tickerData.setSell(tryToParseDouble(getJSONTickerKey(parseJSONObject(json), "sell")));
                break;
            case BIT2C:
                tickerData.setLast(tryToParseDouble(getJSONTickerKey(parseJSONObject(json), "ll")));
                tickerData.setBuy(tryToParseDouble(getJSONTickerKey(parseJSONObject(json), "h")));
                tickerData.setSell(tryToParseDouble(getJSONTickerKey(parseJSONObject(json), "l")));
                break;
            case BITPAY:
                // [{"code":"USD","name":"US Dollar","rate":597.4889},{"code":"EUR","name":"Eurozone Euro","rate":442.3828},...
                try {
                    final JSONArray currencyRates = new JSONArray(json);
                    for (int i=0; i<currencyRates.length(); i++) {
                        final JSONObject currencyRate = currencyRates.getJSONObject(i);
                        if (currencyRate.getString("code").equalsIgnoreCase("USD")) {
                            tickerData.setLast(tryToParseDouble(getJSONTickerKey(currencyRate, "rate")));
                            break;
                        }
                    }
                } catch (JSONException e) {
                    // Do nothing
                }
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

                tickerData.setLast(tryToParseDouble(getJSONTickerKeyFromObjects(parseJSONObject(json), "data", "last", "value")));
                tickerData.setLow(tryToParseDouble(getJSONTickerKeyFromObjects(parseJSONObject(json), "data", "low", "value")));
                tickerData.setHigh(tryToParseDouble(getJSONTickerKeyFromObjects(parseJSONObject(json), "data", "high", "value")));
                tickerData.setBuy(tryToParseDouble(getJSONTickerKeyFromObjects(parseJSONObject(json), "data", "buy", "value")));
                tickerData.setSell(tryToParseDouble(getJSONTickerKeyFromObjects(parseJSONObject(json), "data", "sell", "value")));
        }

        return tickerData;
    }

    private JSONObject parseJSONObject(String json) {
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            return new JSONObject();
        }
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
        return BITSTAMP;
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
