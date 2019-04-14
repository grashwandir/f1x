package org.augur.f1x;

/**
 *
 * @author niels.kowalski
 */
public class TTInstrument implements IInstrument {

    private String symbol;
    private String securityId;
    private String securityType;
    private String IDSource;
    private String securityExchange;
    private String exDestination;
    private String maturityMonthYear;

    public TTInstrument(String symbol, String securityId, String securityType, String IDSource, String securityExchange, String exDestination, String maturityMonthYear) {
        this.symbol = symbol;
        this.securityId = securityId;
        this.securityType = securityType;
        this.IDSource = IDSource;
        this.securityExchange = securityExchange;
        this.maturityMonthYear = maturityMonthYear;
    }

    public TTInstrument() {
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String getSecurityId() {
        return securityId;
    }

    public void setSecurityId(String securityId) {
        this.securityId = securityId;
    }

    @Override
    public String getSecurityType() {
        return securityType;
    }

    public void setSecurityType(String securityType) {
        this.securityType = securityType;
    }

    @Override
    public String getIDSource() {
        return IDSource;
    }

    public void setIDSource(String IDSource) {
        this.IDSource = IDSource;
    }

    @Override
    public String getSecurityExchange() {
        return securityExchange;
    }

    public void setSecurityExchange(String securityExchange) {
        this.securityExchange = securityExchange;
    }

    @Override
    public String getMaturityMonthYear() {
        return maturityMonthYear;
    }

    public void setMaturityMonthYear(String maturityMonthYear) {
        this.maturityMonthYear = maturityMonthYear;
    }

    @Override
    public String getExDestination() {
        return exDestination;
    }

    public void setExDestination(String exDestination) {
        this.exDestination = exDestination;
    }

}
