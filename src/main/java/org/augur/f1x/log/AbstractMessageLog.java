package org.augur.f1x.log;

import java.io.IOException;
import org.f1x.log.MessageLog;

/**
 *
 * @author niels.kowalski
 */
public abstract class AbstractMessageLog implements MessageLog {

    @Override
    public void log(boolean isInbound, byte[] buffer, int offset, int length) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
