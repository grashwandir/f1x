package org.augur.tt.symbols.security;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SecurityParser //        implements InputFIXMessage.Walker
{
//
//    private static final Logger LOGGER = Logger.getLogger(SecurityParser.class.getName());
//    public static final String MONTH_YEAR_FORMAT_STR = "YYYYMM";
//    public static final String DATE_FORMAT_STR = "YYYYMMDD";
//    private static final SimpleDateFormat MONTH_YEAR_FORMAT = new SimpleDateFormat(MONTH_YEAR_FORMAT_STR);
//    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STR);
//    // instrument parse fields
//    private String securityID;
//    private String secSubType;
//    private String currency;
//    private double displayFactor;
//    private double strike;
//    private OptionContractType right;
//    private char deliveryTerm;
//    private String deliveryDate;
//    private String expDay;
//    private String expDate;
//    private String expMonthYear;
//    private String secDesc;
//    private String secType;
//    private String symbol;
//    private String exDestination;
//    private String secExchange;
//    private String iDSource;
//    // sec alt IDs
//    private String secAltID;
//    private String alias;
//    private String name;
//    private String ricCode;
//    // default values
//    private final OptionType optStyle = OptionType.EUROPEAN;
//    private final Map<String, GenericInstrument> instruments = new HashMap<>();
//    private final String brokerID;
//    // other
//
//    private final TBSecurities securities;
//    private final SecurityIDSource secIDsrc;
//    // state
//    private int expectedCount = 0;
//    private int count = 0;
//    private String exchangeSecID;
//
//    public SecurityParser(final String tbUrl, final String brokerID, final SecurityIDSource secIDsrc) {
//        this.securities = new TBSecurities(tbUrl);
//        this.brokerID = brokerID;
//        this.secIDsrc = secIDsrc;
//    }
//
//    private static long parseDate(final String date) throws ParseException {
//        long time = DateTimeDataType.NULL;
//        time = DATE_FORMAT.parse(date).getTime();
//        return time;
//    }
//
//    private static long parseMonthYear(final String monthYear, final String day) throws ParseException {
//        long time = DateTimeDataType.NULL;
//        final Calendar utcCalendar = TimeUtil.getUtcCalendar(MONTH_YEAR_FORMAT.parse(monthYear));
//        utcCalendar.add(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
//        time = utcCalendar.getTimeInMillis();
//        return time;
//    }
//
//    private void resetInstrumentData() {
//        this.securityID = null;
//        this.secAltID = null;
//        this.secSubType = null;
//        this.currency = null;
//        this.displayFactor = FloatDataType.DECIMAL_NULL;
//        this.strike = FloatDataType.DECIMAL_NULL;
//        this.right = null;
//        this.deliveryTerm = CharDataType.NULL;
//        this.expDay = null;
//        this.expDate = null;
//        this.expMonthYear = null;
//        this.secDesc = null;
//        this.secType = null;
//        this.symbol = null;
//        this.exDestination = null;
//        this.secExchange = null;
//        this.iDSource = null;
//    }
//
//    private void flushInstrument() {
//        if (securityID == null) {
//            return;
//        }
//        count++;
//        if (instruments.containsKey(securityID)) {
//            LOGGER.log(Level.WARNING, "TT FIX SecurityDefinition: skipping duplicate SecurityID: {0}", securityID);
//            return;
//        }
//        GenericInstrument instrument = null;
//        try {
//            switch (secType) {
//                case "CS": {
//                    final Equity equity = new Equity();
//                    instrument = equity;
//                    break;
//                }
//                case "OPT": {
//                    final Option option = new Option();
//                    long time = DateTimeDataType.NULL;
//                    if (expDate != null) {
//                        time = parseDate(expDate);
//                    } else if (expMonthYear != null && expDay != null) {
//                        time = parseMonthYear(expMonthYear, expDay);
//                    } else {
//                        throw new IllegalStateException("Undefined expiration for option");
//                    }
//                    option.setExpirationDate(time);
//                    if (deliveryDate != null) {
//                        option.setInactiveDate(parseDate(deliveryDate));
//                    }
//                    if (deliveryTerm == CharDataType.NULL) {
//                        option.setOptionPeriodicity(OptionPeriodicity.MONTHLY);
//                    }
//                    /*
//                        A: Same day
//                        B: Balance of month
//                        D: Day
//                        E: Weekly
//                        L: Balance of week
//                        M: Month
//                        N: Next day
//                        P: Pack
//                        Q: Quarterly
//                        S: Seasonal
//                        T: Weekend
//                        U: Bundle
//                        V: Variable
//                        W: Week
//                        X: Custom
//                        Y: Year
//                     */
//                    switch (deliveryTerm) {
//                        case 'D':
//                            option.setOptionPeriodicity(OptionPeriodicity.DAILY);
//                            break;
//                        case 'E':
//                        case 'W':
//                            option.setOptionPeriodicity(OptionPeriodicity.WEEKLY);
//                            break;
//                        case 'Q':
//                            option.setOptionPeriodicity(OptionPeriodicity.QUARTERLY);
//                            break;
//                        case 'M':
//                            option.setOptionPeriodicity(OptionPeriodicity.MONTHLY);
//                            break;
//                        default:
//                            throw new IllegalStateException("Unsupported option periodicity: " + deliveryTerm);
//                    }
//                    option.setOptionType(optStyle);
//                    option.setPutCall(right);
//                    option.setStrikePrice(strike);
//                    option.setUnderlying(symbol);
//                    instrument = option;
//                    break;
//                }
//                case "FUT": {
//                    final Future future = new Future();
//                    if (expDate != null) {
//                        final Calendar utcCalendar = TimeUtil.getUtcCalendar(DATE_FORMAT.parse(expDate));
//                        future.setExpirationDate(utcCalendar.getTimeInMillis());
//                        future.setContractYear((short) utcCalendar.get(Calendar.YEAR));
//                        future.setContractMonth((byte) utcCalendar.get(Calendar.MONTH));
//                    } else {
//                        throw new IllegalStateException("Undefined expiration for instrument options");
//                    }
//                    if (deliveryDate != null) {
//                        final long deliveryTime = parseDate(deliveryDate);
//                        future.setLastTradedDate(deliveryTime);
//                        future.setInactiveDate(deliveryTime);
//                    }
//                    future.setRootSymbol(symbol);
//                    instrument = future;
//                    break;
//                }
//                case "FOR": {
//                    final Currency fx = new Currency();
//                    instrument = fx;
//                    break;
//                }
//                default: {
//                    throw new IllegalArgumentException("Unsupported Instrument Type: " + secType);
//                }
//            }
//            instrument.setSymbol(name);
//            switch (secIDsrc) {
//                case RIC_CODE:
//                    instrument.tt = ricCode;
//                    break;
//                case EXCHANGE:
//                    instrument.tt = exchangeSecID;
//                    break;
//                case TT_ID:
//                    instrument.tt = securityID;
//                    break;
//                case ALIAS:
//                    instrument.tt = alias;
//                    break;
//                case NAME:
//                    instrument.tt = name;
//                    break;
//                default:
//                    throw new AssertionError(secIDsrc.name());
//
//            }
//            instrument.setName(alias);
//            instrument.setBrokerID(brokerID);
//            instrument.setExchangeId(secExchange);
//
//            instruments.put(securityID, instrument);
//        } catch (Exception ex) {
////            throw new RuntimeException(ex);
//            LOGGER.log(Level.SEVERE, "while parsing security: " + instrument, ex);
//        } finally {
//            //TODO: definitly here!
//            resetInstrumentData();
//        }
//    }
//
//    @Override
//    public void onWalkBegin(final InputFIXMessage msg) throws FIXException {
//        //TODO: or here?
////        resetInstrumentData();
//    }
//
//    @Override
//    public boolean onField(final InputFIXMessage msg, final int fieldId, final InputFIXMessage.Value value) throws FIXException {
//        switch (fieldId) {
//            case 393: {
//                //TODO: missing tag TotalNumSecurities
//                if (expectedCount == 0) {
//                    expectedCount = value.getInt();
//                }
//                break;
//            }
//            case FIXField.CheckSum: {
//                flushInstrument();
//                break;
//            }
//            case FIXField.SecurityID: {
//                securityID = value.toString();
//                break;
//            }
//            case 22: {
//                //TODO:missing tag IDSource
//                iDSource = value.toString();
//                break;
//            }
//            case FIXField.SecurityExchange: {
//                secExchange = value.toString();
//                break;
//            }
//            case 100: {
//                //TODO:missing tag ExDestination
//                exDestination = value.toString();
//                break;
//            }
//            case FIXField.Symbol: {
//                symbol = value.toString();
//                break;
//            }
//            case FIXField.SecurityType: {
//                secType = value.toString();
//                break;
//            }
//            case 107: {
//                //TODO:missing tag SecurityDesc
//                secDesc = value.toString();
//                break;
//            }
//            case FIXField.MaturityMonthYear: {
//                //YYYYMM
//                expMonthYear = value.toString();
//                break;
//            }
//            case 541: {
//                //TODO:missing tag MaturityDate YYYYMMDD
//                expDate = value.toString();
//                break;
//            }
//            case 205: {
//                //TODO:missing tag MaturityDay 1-31
//                expDay = value.toString();
//                break;
//            }
//            case 18211: {
//                //TODO:missing tag DeliveryTerm
//                deliveryTerm = value.charAt(0);
//                break;
//            }
//            case 743: {
//                //TODO: missing tag DeliveryDate
//                deliveryDate = value.toString();
//                break;
//            }
//            case 201: {
//                //TODO:missing tag PutOrCall
//                right = value.getInt() == 0 ? OptionContractType.PUT : OptionContractType.CALL;
//                break;
//            }
//            case 202: {
//                //TODO:missing tag StrikePrice
//                strike = value.getFloat();
//                break;
//            }
//            case 9787: {
//                //TODO:missing tag DisplayFactor
//                displayFactor = value.getFloat();
//                break;
//            }
//            case 15: {
//                //TODO:missing tag Currency
//                currency = value.toString();
//                break;
//            }
//            case 762: {
//                //TODO: missing tag SecuritySubType
//                secSubType = value.toString();
//                break;
//            }
//            case 455: {
//                //TODO: missing tag SecurityAltID
//                secAltID = value.toString();
//                break;
//            }
//            case 456: {
//                //TODO: missing tag SecurityAltIDSource
//                switch (value.toString()) {
//                    case "4": {// ISIN
//                        break;
//                    }
//                    case "5": {// RIC
//                        ricCode = secAltID;
//                        break;
//                    }
//                    case "8": {// ExchangeSecID
//                        exchangeSecID = secAltID;
//                        break;
//                    }
//                    case "94": {// Alt Symbol (For ICE, the value is the "Cleared Alias" for the contract.)
//                        break;
//                    }
//                    case "95": {// ClearPort
//                        break;
//                    }
//                    case "97": {// Alias
//                        alias = secAltID;
//                        break;
//                    }
//                    case "98": {// Name
//                        name = secAltID;
//                        break;
//                    }
//                    case "99": {// Other
//                        break;
//                    }
//                }
//                break;
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public void onWalkFinish(final InputFIXMessage msg) throws FIXException {
//        //TODO: here?
////        resetInstrumentData();
//        if (count >= expectedCount) {
//            count = 0;
//            expectedCount = 0;
//            securities.writeSecurities(instruments.values(), true);
//            instruments.clear();
//        }
//    }

}
