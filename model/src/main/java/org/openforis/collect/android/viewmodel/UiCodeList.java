package org.openforis.collect.android.viewmodel;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UiCodeList {
    private final List<UiCode> codes;
    private final Map<String, UiCode> codeByValue = new HashMap<String, UiCode>();
    private final Set<UiCode> qualifiableCodes;

    public UiCodeList(List<UiCode> codes, UiCode qualifiableCode) {
        this.codes = codes;
        this.qualifiableCodes = qualifiableCode == null
                ? Collections.<UiCode>emptySet()
                : Collections.singleton(qualifiableCode);
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
        return !qualifiableCodes.isEmpty();
    }

    public boolean isQualifiable(UiCode code) {
        return qualifiableCodes.contains(code);
    }

    public UiCode getQualifiableCode() {
        return isQualifiable() ? qualifiableCodes.iterator().next() : null;
    }

    public boolean containsDescription() {
        for (UiCode code : getCodes())
            if (StringUtils.isNotEmpty(code.getDescription()))
                return true;
        return false;
    }
}
