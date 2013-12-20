package st.brothas.mtgoxwidget;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: admin
 * Date: 4/13/13
 * Time: 4:01 PM
 * To change this template use File | Settings | File Templates.
 */
public enum CurrencyConversion {

    BTC_USD(1, "BTC/USD", "$"),
    BTC_EUR(2, "BTC/EUR", "€"),
    BTC_GBP(3, "BTC/GBP", "£"),
    BTC_CAD(4, "BTC/CAD", "$"),
    BTC_AUD(5, "BTC/AUD", "$"),
    BTC_CNY(6, "BTC/CNY", "¥"),
    BTC_JPY(7, "BTC/JPY", "¥"),
    BTC_RUB(8, "BTC/RUB", "р"),
    BTC_SEK(9, "BTC/SEK", "k"),
    BTC_PLN(10,"BTC/PLN", "zł"),

    // Litecoins
    LTC_BTC(106, "LTC/BTC", "฿"),
    LTC_USD(107, "LTC/USD", "$");

    public final Integer id;
    public final String description;
    public final String symbol;
    private static final Map<Integer,CurrencyConversion> lookup = new HashMap<Integer,CurrencyConversion>();

    CurrencyConversion(int id, String description, String symbol) {
        this.id = id;
        this.description = description;
        this.symbol = symbol;
    }

    static {
        for(CurrencyConversion s : EnumSet.allOf(CurrencyConversion.class))
            lookup.put(s.id, s);
    }


    public static CurrencyConversion getDefault() {
        return BTC_USD;
    }

    public static CurrencyConversion getById(int id) {
        return lookup.get(id);
    }
}
