package org.augur.f1x.log;

import java.util.logging.Level;
import org.f1x.log.LogFormatter;
import org.f1x.log.MessageLog;

/**
 *
 * @author niels.kowalski
 */
public class JULMessageLogFactory extends MessageLogFactoryImpl {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("augur.fix.f1x");
    private Level lvl;

    public JULMessageLogFactory(final Level lvl) {
        this.lvl = lvl;
    }

    public JULMessageLogFactory() {
        this(Level.INFO);
    }

    @Override
    protected MessageLog create(final LogFormatter formatter) {
        return new MsgLogImpl(LOGGER, formatter);
    }

//    @Override
//    protected Logger createLogger(String string) {
//        return new MsgLogImpl(java.util.logging.Logger.getLogger(string), new LogFormatterString(string));
//    }

    /**
     * @return the lvl
     */
    public Level getLvl() {
        return lvl;
    }

    /**
     * @param lvl the lvl to set
     */
    public void setLvl(Level lvl) {
        this.lvl = lvl;
    }

}
