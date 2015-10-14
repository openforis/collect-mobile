package org.openforis.collect.android.viewmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UiCodeList {
    private final List<UiCode> codes;
    private final Map<String, UiCode> codeByValue = new HashMap<String, UiCode>();
    private final UiCode qualifiableCode;

    public UiCodeList(List<UiCode> codes, UiCode qualifiableCode) {
        this.codes = codes;
        this.qualifiableCode = qualifiableCode;
        for (UiCode code : codes)
            codeByValue.put(code.getValue(), code);
    }

    public List<UiCode> getCodes() {
        return codes;
    }

    public UiCode getCode(String value) {
        return codeByValue.get(value);
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
