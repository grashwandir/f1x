package org.augur.f1x;

import java.util.logging.Logger;
import org.augur.f1x.log.JULMessageLogFactory;
import org.f1x.api.FixInitiatorSettings;
import org.f1x.api.FixVersion;
import org.f1x.api.session.SessionID;

/**
 *
 * @author niels.kowalski
 */
public class AugurInitiatorFixSessionConfig extends FixInitiatorSettings implements InitiatorFixSessionConfig {

    private final SessionID sessionID;
//    private final MessageLog logger;
    private String onBehalfOfCompID = null;
    private FixVersion fixVersion = null;

    public AugurInitiatorFixSessionConfig(SessionID sessionID) {
        this.sessionID = sessionID;
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

    /**
     * @param onBehalfOfCompID the onBehalfOfCompID to set
     */
    public void setOnBehalfOfCompID(String onBehalfOfCompID) {
        this.onBehalfOfCompID = onBehalfOfCompID;
    }

    /**
     * @param fixVersion the fixVersion to set
     */
    public void setFixVersion(FixVersion fixVersion) {
        this.fixVersion = fixVersion;
    }

}
