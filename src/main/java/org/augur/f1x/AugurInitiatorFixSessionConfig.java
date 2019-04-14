package org.augur.f1x;

import java.util.logging.Logger;
import org.augur.f1x.log.JULMessageLogFactory;
import org.f1x.SessionIDBean;
import org.f1x.api.FixInitiatorSettings;
import org.f1x.api.FixVersion;
import org.f1x.api.session.SessionID;

/**
 *
 * @author niels.kowalski
 */
public class AugurInitiatorFixSessionConfig extends FixInitiatorSettings implements InitiatorFixSessionConfig {

    private final SessionID sessionID;
    private String password;
    private String onBehalfOfCompID = null;
    private FixVersion fixVersion = null;
    private String host;
    private int port;

    public AugurInitiatorFixSessionConfig(SessionID sessionID) {
        this.sessionID = sessionID;
    }

    public AugurInitiatorFixSessionConfig(String senderCompId, String senderSubId, String targetCompId, String targetSubId) {
        this(new SessionIDBean(senderCompId, senderSubId, targetCompId, targetSubId));
    }

    @Override
    public String getBeginString() {
        return fixVersion.getBeginString();
    }

    @Override
    public String getSenderCompID() {
        return sessionID.getSenderCompId().toString();
    }

    @Override
    public String getTargetCompID() {
        return sessionID.getTargetCompId().toString();
    }

    @Override
    public String getOnBehalfOfCompID() {
        return onBehalfOfCompID;
    }

    @Override
    public String getSenderSubID() {
        return sessionID.getSenderSubId().toString();
    }

    @Override
    public String getTargetSubID() {
        return sessionID.getTargetSubId().toString();
    }

    @Override
    public int getInputBufferSize() {
        return getMaxInboundMessageSize();
    }

    @Override
    public String getID() {
        return getBeginString() + '-' + getSenderCompID() + '-' + getTargetCompID();
    }

    @Override
    public Logger getLogger() {
        return JULMessageLogFactory.LOGGER;
    }

    @Override
    public int getHeartbeatPeriod() {
        return getHeartBeatIntervalSec();
    }

    public void setOnBehalfOfCompID(String onBehalfOfCompID) {
        this.onBehalfOfCompID = onBehalfOfCompID;
    }

    public void setFixVersion(FixVersion fixVersion) {
        this.fixVersion = fixVersion;
    }

    public SessionID getSessionID() {
        return sessionID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public FixVersion getFixVersion() {
        return fixVersion;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
