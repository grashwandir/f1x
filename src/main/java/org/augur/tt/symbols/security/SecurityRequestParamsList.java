package org.augur.tt.symbols.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.augur.tt.symbols.security.SecurityRequestParamsList.SecurityRequestParams;
import org.f1x.api.session.InitiatorFixSessionListener;
import org.f1x.v1.FixSessionInitiator;

/**
 *
 * @author niels.kowalski
 */
@XmlRootElement(name = "security-request-params-list")
@XmlAccessorType(XmlAccessType.FIELD)
public class SecurityRequestParamsList implements Collection<SecurityRequestParams> {

    @XmlElementWrapper(name = "list")
    @XmlElement(name = "security-request-params")
    private final Set<SecurityRequestParams> securityGroups = new HashSet<>();

    public SecurityRequestParamsList() {
    }

    @Override
    public boolean add(SecurityRequestParams e) {
        return securityGroups.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends SecurityRequestParams> c) {
        return securityGroups.addAll(c);
    }

    public SecurityRequestParams addSecurityGroup(String exDestination, String exchange, boolean tickTable, String securityID, String securityType, String symbol, SecurityRequestType type) {
        final SecurityRequestParams req = new SecurityRequestParams(exDestination, exchange, tickTable, securityID, securityType, symbol, type);
        this.securityGroups.add(req);
        return req;
    }

    public void addSecurityGroups(final Collection<SecurityRequestParams> securityGroups) {
        this.securityGroups.addAll(securityGroups);
    }

    @Override
    public void clear() {
        securityGroups.clear();
    }

    @Override
    public boolean contains(Object o) {
        return securityGroups.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return securityGroups.containsAll(c);
    }

    public void setSecurityGroups(final Collection<SecurityRequestParams> securityGroups) {
        this.securityGroups.clear();
        this.securityGroups.addAll(securityGroups);
    }

    @Override
    public boolean isEmpty() {
        return securityGroups.isEmpty();
    }

    @Override
    public Iterator<SecurityRequestParams> iterator() {
        return securityGroups.iterator();
    }

    public boolean load(final String path) throws Exception {
        final SecurityRequestParamsList list = loadFromFile(path);
        return this.securityGroups.addAll(list.securityGroups);
    }

    @Override
    public boolean remove(Object o) {
        return securityGroups.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return securityGroups.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return securityGroups.retainAll(c);
    }

    public boolean save(final String path) throws Exception {
        return saveToFile(this, path);
    }

    @Override
    public int size() {
        return securityGroups.size();
    }

    @Override
    public Object[] toArray() {
        return securityGroups.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return securityGroups.toArray(a);
    }

    public static SecurityRequestParamsList loadFromFile(final String path) throws Exception {
//        return XMLUtil.readXml(SecurityRequestParamsList.class, path);
        throw new UnsupportedOperationException();
    }

    public static boolean saveToFile(final SecurityRequestParamsList object, final String path) throws Exception {
//        return XMLUtil.writeXml(SecurityRequestParamsList.class, object, path, true);
        throw new UnsupportedOperationException();
    }

    @XmlType(name = "security-request-params")
    @XmlAccessorType(value = XmlAccessType.FIELD)
    public static final class SecurityRequestParams {

        @XmlElement(name = "exchange_destination")
        private String exDestination;
        @XmlElement(name = "exchange")
        private String exchange;
        @XmlElement(name = "sec_id")
        private String securityID;
        @XmlElement(name = "sec_type")
        private String securityType;
        @XmlElement(name = "symbol")
        private String symbol;
        @XmlElement(name = "tick_table")
        private boolean tickTable;
        @XmlElement(name = "req_sec_type")
        private SecurityRequestType type;

        public SecurityRequestParams(String exDestination, String exchange, boolean tickTable, String securityID, String securityType, String symbol, SecurityRequestType type) {
            this.exDestination = exDestination;
            this.exchange = exchange;
            this.tickTable = tickTable;
            this.securityID = securityID;
            this.securityType = securityType;
            this.symbol = symbol;
            this.type = type;
        }

        public SecurityRequestParams(String exchange, boolean tickTable, String securityID, String securityType, String symbol, SecurityRequestType type) {
            this(null, exchange, tickTable, securityID, securityType, symbol, type);
        }

        public SecurityRequestParams() {
            this(null, null, false, null, null, null, null);
        }

        @Override
        public boolean equals(Object obj) {
            //TODO: replace by TextUtil String compare function
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SecurityRequestParams other = (SecurityRequestParams) obj;
            if (this.tickTable != other.tickTable) {
                return false;
            }
            if (!Objects.equals(this.exDestination, other.exDestination)) {
                return false;
            }
            if (!Objects.equals(this.exchange, other.exchange)) {
                return false;
            }
            if (!Objects.equals(this.securityID, other.securityID)) {
                return false;
            }
            if (!Objects.equals(this.securityType, other.securityType)) {
                return false;
            }
            if (!Objects.equals(this.symbol, other.symbol)) {
                return false;
            }
            if (this.type != other.type) {
                return false;
            }
            return true;
        }

        public SecurityRequest generateRequest(final FixSessionInitiator<? extends InitiatorFixSessionListener> session) {
//            final SecurityRequest req = new SecurityRequest(session);
//            req.addSecuritySymbol(symbol);
//            req.addSecurityType(securityType);
//            req.addSecurityExchange(exchange);
//            req.addSecurityID(securityID);
//            req.addSecurityReqType(type);
//            req.addExchangeDestination(exDestination);
//            req.setRequestTickTable(tickTable);
//            return req;
            throw new UnsupportedOperationException();
        }

        public String getExDestination() {
            return exDestination;
        }

        public void setExDestination(String exDestination) {
            this.exDestination = exDestination;
        }

        public String getExchange() {
            return exchange;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public String getSecurityID() {
            return securityID;
        }

        public void setSecurityID(String securityID) {
            this.securityID = securityID;
        }

        public String getSecurityType() {
            return securityType;
        }

        public void setSecurityType(String securityType) {
            this.securityType = securityType;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public SecurityRequestType getType() {
            return type;
        }

        public void setType(SecurityRequestType type) {
            this.type = type;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + Objects.hashCode(this.exDestination);
            hash = 53 * hash + Objects.hashCode(this.exchange);
            hash = 53 * hash + Objects.hashCode(this.securityID);
            hash = 53 * hash + Objects.hashCode(this.securityType);
            hash = 53 * hash + Objects.hashCode(this.symbol);
            hash = 53 * hash + (this.tickTable ? 1 : 0);
            hash = 53 * hash + Objects.hashCode(this.type);
            return hash;
        }

        public boolean isTickTable() {
            return tickTable;
        }

        public void setTickTable(boolean tickTable) {
            this.tickTable = tickTable;
        }

        @Override
        public String toString() {
            return new StringBuilder("SecurityGroup{exDestination=").append(exDestination).append(", exchange=").append(exchange).append(", securityID=").append(securityID).append(", securityType=").append(securityType).append(", symbol=").append(symbol).append(", tickTable=").append(tickTable).append(", type=").append(type).append("}").toString();
        }

    }

}
