package org.f1x.util.timer;

import java.util.Timer;

public abstract class GlobalTimer {

    private static final Timer INSTANCE = new Timer();

    public static Timer getInstance() {
        return INSTANCE;
    }

    private GlobalTimer() {
        throw new AssertionError("Not for you");
    }

}
