package org.f1x.log.file;

import org.f1x.api.session.SessionID;
import org.gflogger.GFLogEntry;

public class LogUtils {

    public static void log (SessionID sessionID, GFLogEntry entry) {
        if (sessionID != null) {
            entry.append('[');
            entry.append(sessionID.getTargetCompId());
            entry.append(':');
            entry.append(sessionID.getSenderCompId());
            entry.append(']');
        } else {
            entry.append("[UNKNOWN]");
        }
    }

    public static void configure() {
        try {
//            org.gflogger.config.xml.XmlLogFactoryConfigurator.configure();
            org.gflogger.config.xml.Configurator.configure();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
