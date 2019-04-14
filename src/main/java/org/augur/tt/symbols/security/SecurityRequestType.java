package org.augur.tt.symbols.security;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author niels.kowalski
 */
public enum SecurityRequestType {
    INFORMATIONS(0),
    IDENTITY(1),
    TYPES(2),
    LIST(3);
    public final static int FIELD = 321;
    public final static int REQ_MSG_TYPE = 321;

    private final int code;

    private final static Map<Integer, SecurityRequestType> values;

    static {
        values = new HashMap<>();
        final EnumSet<SecurityRequestType> securities = EnumSet.allOf(SecurityRequestType.class);
        for (final SecurityRequestType security : securities) {
            values.put(security.code, security);
        }
    }

    public int getCode() {
        return code;
    }

    private SecurityRequestType(final int code) {
        this.code = code;
    }

    public static SecurityRequestType fromCode(final int code) {
        final SecurityRequestType res = values.get(code);
        if (res == null) {
            throw new IllegalArgumentException("Unknown security ID source code: " + code);
        }
        return res;
    }

}
