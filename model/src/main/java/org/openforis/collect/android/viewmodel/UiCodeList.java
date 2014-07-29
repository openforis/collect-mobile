package org.openforis.collect.android.viewmodel;

import java.util.List;

public class UiCodeList {
    private final List<UiCode> codes;
    private final UiCode qualifiableCode;

    public UiCodeList(List<UiCode> codes, UiCode qualifiableCode) {
        this.codes = codes;
        this.qualifiableCode = qualifiableCode;
    }

    public List<UiCode> getCodes() {
        return codes;
    }

    public boolean isQualifiable() {
        return qualifiableCode != null;
    }

    public boolean isQualifiable(UiCode code) {
        return qualifiableCode != null && qualifiableCode.equals(code);
    }

    public UiCode getQualifiableCode() {
        return qualifiableCode;
    }
}
