package org.augur.f1x;

public interface InitiatorFixSessionConfig extends SessionConfig {

    String getBeginString();

    String getSenderCompID();

    String getTargetCompID();

    String getOnBehalfOfCompID();

    String getSenderSubID();

    String getTargetSubID();

    int getHeartbeatPeriod();

    int getInputBufferSize();
}
