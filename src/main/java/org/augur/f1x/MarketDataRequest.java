package org.augur.f1x;

import org.f1x.v1.GenericRequest;
import java.util.LinkedList;
import org.f1x.api.UncheckedFixException;
import org.f1x.api.message.MessageBuilder;
import org.f1x.api.message.fields.FixTags;
import org.f1x.api.message.fields.MDEntryType;
import org.f1x.api.message.fields.MsgType;
import org.f1x.api.message.fields.SubscriptionRequestType;
import org.f1x.api.session.FixSession;

public class MarketDataRequest extends GenericRequest {

    public static class Builder {

        private final FixSession<?, MessageBuilder, ?> owner;
        private final LinkedList<IInstrument> instrumentsList = new LinkedList<>();
        private MDEntryType[] mdEntryTypes = null;
        private SubscriptionRequestType subscriptionRequestType = SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES;
        private int marketDepth = 0; // default full book
        private int mdUpdateType = 1; // default incremental
        private boolean aggregatedBook = true;

        private Builder(final FixSession<?, MessageBuilder, ?> owner) {
            this.owner = owner;
        }

        public static Builder create(final FixSession<?, MessageBuilder, ?> owner) {
            return new Builder(owner);
        }

        public static Builder create() {
            return create(null);
        }

        public void clear() {
            instrumentsList.clear();
            subscriptionRequestType = SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES;
            marketDepth = 0;
            mdUpdateType = 1;
            aggregatedBook = true;
        }

        public MarketDataRequest build(final FixSession<?, MessageBuilder, ?> owner) {
            final MarketDataRequest request = new MarketDataRequest(owner);
            request.addSubscriptionRequestType(subscriptionRequestType);
            request.addMarketDepth(marketDepth);
            request.addMDUpdateType(mdUpdateType);
            request.addAggregatedBook(aggregatedBook);
            if (mdEntryTypes == null || mdEntryTypes.length == 0) {
                throw new UncheckedFixException("MDEntryTypes is empty for MDRequest");
            }
            request.addMDEntryTypes(mdEntryTypes);
            request.setInstruments(instrumentsList.toArray(new IInstrument[instrumentsList.size()]));
            return request;
        }

        public MarketDataRequest build() {
            return build(owner);
        }

        public void addMDEntryTypes(final MDEntryType... types) {
            mdEntryTypes = new MDEntryType[types.length];
            for (int i = 0; i < types.length; i++) {
                mdEntryTypes[i] = types[i];
            }
        }

        public void addInstrument(final IInstrument instrument) {
            instrumentsList.add(instrument);
        }

        public void addInstrument(String symbol, String securityId, String securityType, String IDSource, String securityExchange, String exDestination, String maturityMonthYear) {
            addInstrument(new TTInstrument(symbol, securityId, securityType, IDSource, securityExchange, exDestination, maturityMonthYear));
        }

        public void addInstrumentById(String symbol, String securityId, String IDSource, String securityExchange, String exDestination) {
            addInstrument(symbol, securityId, null, IDSource, securityExchange, exDestination, null);
        }

        public void setSubscriptionRequestType(final SubscriptionRequestType subscriptionRequestType) {
            this.subscriptionRequestType = subscriptionRequestType;
        }

        public SubscriptionRequestType getSubscriptionRequestType() {
            return subscriptionRequestType;
        }

        public int getMarketDepth() {
            return marketDepth;
        }

        public void setMarketDepth(int marketDepth) {
            this.marketDepth = marketDepth;
        }

        public int getMdUpdateType() {
            return mdUpdateType;
        }

        public void setMdUpdateType(int mdUpdateType) {
            this.mdUpdateType = mdUpdateType;
        }

        public boolean isAggregatedBook() {
            return aggregatedBook;
        }

        public void setAggregatedBook(boolean aggregatedBook) {
            this.aggregatedBook = aggregatedBook;
        }
    }

    private IInstrument[] instruments;
    private boolean instrumentsAdded = false;

    public MarketDataRequest(final FixSession<?, MessageBuilder, ?> owner) {
        super(owner, FixTags.MDReqID, MsgType.MARKET_DATA_REQUEST);
    }

    @Override
    public void clear() {
        super.clear();
        instruments = null;
        instrumentsAdded = false;
    }

    public void addMDUpdateType(final int type) {
        add(FixTags.MDUpdateType, type);
    }

    public void addMarketDepth(final int depth) {
        add(FixTags.MarketDepth, depth);
    }

    public void addAggregatedBook(final boolean value) {
        add(FixTags.AggregatedBook, value);
    }

    public void addSubscriptionRequestType(final SubscriptionRequestType type) {
        add(FixTags.SubscriptionRequestType, type.getCode());
    }

    public void addMDEntryTypes(final char... types) {
        add(FixTags.NoMDEntryTypes, types.length);
        for (int i = 0; i < types.length; ++i) {
            add(FixTags.MDEntryType, types[i]);
        }
    }

    public void addMDEntryTypes(final MDEntryType... types) {
        add(FixTags.NoMDEntryTypes, types.length);
        for (int i = 0; i < types.length; ++i) {
            add(FixTags.MDEntryType, types[i].getCode());
        }
    }

//    public void addUnsubscribeMarketDepth(final int depth) {
//        this.unsubscribeBody.addField(FixTags.MarketDepth, depth);
//    }
//
//    public void addUnsubscribeAggregatedBook(final boolean value) {
//        this.unsubscribeBody.addField(FixTags.AggregatedBook, value);
//    }
//
//    public void addUnsubscribeMDEntryTypes(final char... types) {
//        this.unsubscribeBody.addField(FixTags.NoMDEntryTypes, types.length);
//        for (int i = 0; i < types.length; ++i) {
//            this.unsubscribeBody.addField(FixTags.MDEntryType, types[i]);
//        }
//    }
//
//    public void addUnsubscribeInstruments(final boolean addUnsubscribeInstruments) {
//        this.addUnsubscribeInstruments = addUnsubscribeInstruments;
//    }
//
//    @Override
//    public void send() throws IOException {
//        if (!instrumentsAdded) {
//            addInstruments();
//        }
//        super.send();
//    }
//
//    public void resend() throws IOException {
//        this.owner.send(this.subscribe);
//    }
//
//    public void cancel() throws IOException {
//        if (this.addUnsubscribeInstruments && !this.instrumentsAddedForUnsubscribe) {
//            this.addInstrumentsForUnsubscribe();
//        }
//        this.owner.send(this.unsubscribe);
//    }
    @Override
    public MessageBuilder getMessage() {
        if (!instrumentsAdded) {
            addInstruments();
            instrumentsAdded = true;
        }
        return super.getMessage();
    }

    private void addInstruments() {
        add(FixTags.NoRelatedSym, instruments.length);
        for (int i = 0; i < instruments.length; ++i) {
            final IInstrument instrument = instruments[i];
            if (instrument.getSymbol() != null) {
                add(FixTags.Symbol, instrument.getSymbol());
            }
            if (instrument.getSecurityType() != null) {
                add(FixTags.SecurityType, instrument.getSecurityType());
            }
            if (instrument.getSecurityId() != null) {
                add(FixTags.SecurityID, instrument.getSecurityId());
            }
            if (instrument.getIDSource() != null) {
                add(22, instrument.getIDSource());
            }
            if (instrument.getSecurityExchange() != null) {
                add(FixTags.SecurityExchange, instrument.getSecurityExchange());
            } else if (instrument.getSecurityExchange() != null) {
                add(FixTags.ExDestination, instrument.getSecurityExchange());
            } else {
                //TODO: think...
                throw new UncheckedFixException("SecurityExchange(207) or ExDestination(100) must be specified");
            }
            if (instrument.getMaturityMonthYear() != null) {
                add(FixTags.MaturityMonthYear, instrument.getMaturityMonthYear());
            }
        }
    }

//    private void addInstrumentsForUnsubscribe() {
//        this.unsubscribeBody.addField(FixTags.Symbol, this.instruments.length);
//        for (int i = 0; i < this.instruments.length; ++i) {
//            final InstrumentExtended instrument = (InstrumentExtended) this.instruments[i];
//            if (instrument.getSymbol() != null) {
//                this.unsubscribeBody.addField(FixTags.Symbol, instrument.getSymbol());
//            }
//            if (instrument.getSecurityType() != null) {
//                this.unsubscribeBody.addField(FixTags.SecurityType, instrument.getSecurityType());
//            }
//            if (instrument.getSecurityId() != null) {
//                this.unsubscribeBody.addField(FixTags.SecurityID, instrument.getSecurityId());
//            }
//            if (instrument.getIDSource() != null) {
//                this.unsubscribeBody.addField(22, instrument.getIDSource());
//            }
//            if (instrument.getSecurityExchange() != null) {
//                this.unsubscribeBody.addField(FixTags.SecurityExchange, instrument.getSecurityExchange());
//            }
//            if (instrument.getMaturityMonthYear() != null) {
//                this.unsubscribeBody.addField(FixTags.MaturityMonthYear, instrument.getMaturityMonthYear());
//            }
//        }
//        this.instrumentsAddedForUnsubscribe = true;
//    }
    public IInstrument[] getInstruments() {
        return instruments;
    }

    public void setInstruments(IInstrument[] instruments) {
        this.instruments = instruments;
    }
}
