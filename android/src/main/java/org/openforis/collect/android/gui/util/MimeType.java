package org.openforis.collect.android.gui.util;

public enum MimeType {
    BINARY("application/octet");

    private String code;

    MimeType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
