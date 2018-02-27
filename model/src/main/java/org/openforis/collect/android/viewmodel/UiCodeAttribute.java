package org.openforis.collect.android.viewmodel;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Daniel Wiell
 */
public class UiCodeAttribute extends UiAttribute {

    private static final String QUALIFIER_SEPARATOR = ": ";

    private UiCode code;
    private String qualifier;

    public UiCodeAttribute(int id, boolean relevant, UiCodeAttributeDefinition definition) {
        super(id, relevant, definition);
    }

    public synchronized void setCode(UiCode code) {
        this.code = code;
    }

    public synchronized UiCode getCode() {
        return code;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public UiCodeAttributeDefinition getDefinition() {
        return (UiCodeAttributeDefinition) super.getDefinition();
    }

    public String valueAsString() {
        return code == null ? null : (code.toString() + (StringUtils.isEmpty(qualifier) ? "" : QUALIFIER_SEPARATOR + qualifier));
    }

    public boolean isEmpty() {
        return code == null && StringUtils.isEmpty(qualifier);
    }

    public String toString() {
        return getLabel() + ": " + (code == null ? (StringUtils.isEmpty(qualifier) ? "Unspecified" : qualifier) : code);
    }
}
