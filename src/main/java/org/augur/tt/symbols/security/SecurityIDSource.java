package org.augur.tt.symbols.security;

/**
 * FIX 4.2
 *
 * @author niels.kowalski
 */
public enum SecurityIDSource {
    RIC_CODE("5"),
    EXCHANGE("8"),
    TT_ID("96"),
    ALIAS("97"),
    NAME("98");

    private final String code;

    private SecurityIDSource(final String code) {
        this.code = code;
    }

    public static SecurityIDSource fromCode(final String code) {
        if (code == null) {
            throw new NullPointerException("Null security ID source code: " + code);
        }
        switch (code) {
            case "5":
                return RIC_CODE;
            case "8":
                return EXCHANGE;
            case "96":
                return TT_ID;
            case "97":
                return ALIAS;
            case "98":
                return NAME;
            default:
                throw new IllegalArgumentException("Unknown security ID source code: " + code);
        }
    }

    public String getCode() {
        return code;
    }

}
