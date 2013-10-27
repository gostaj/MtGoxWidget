package st.brothas.mtgoxwidget;

public class WidgetPreferences {

    private RateService rateService = RateService.getDefaultService();
    private ColorMode colorMode = ColorMode.Default;
    private CurrencyConversion currencyConversion = CurrencyConversion.getDefault();

    public ColorMode getColorMode() {
        return colorMode;
    }

    public void setColorMode(ColorMode colorMode) {
        this.colorMode = colorMode;
    }

    public CurrencyConversion getCurrencyConversion() {
        return currencyConversion;
    }

    public void setCurrencyConversion(CurrencyConversion currencyConversion) {
        this.currencyConversion = currencyConversion;
    }

    public RateService getRateService() {
        return rateService;
    }

    public void setRateService(RateService rateService) {
        this.rateService = rateService;
    }

    @Override
    public String toString() {
        return "WidgetPreferences{" +
                "colorMode=" + colorMode +
                ", rateService=" + rateService +
                ", currencyConversion=" + currencyConversion +
                '}';
    }
}
