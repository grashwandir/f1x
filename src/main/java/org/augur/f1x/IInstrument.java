package org.augur.f1x;

/**
 *
 * @author niels.kowalski
 */
public interface IInstrument {

    public CharSequence getSymbol();

    public CharSequence getSecurityType();

    public CharSequence getSecurityId();

    public CharSequence getSecurityExchange();

    public CharSequence getExDestination();

    public CharSequence getMaturityMonthYear();

    public String getIDSource();
}
