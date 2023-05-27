package org.openforis.collect.android.viewmodel;

public class UiAttributeDefinition extends Definition {
    public final boolean calculated;
    public final boolean calculatedOnlyOneTime;
    public final boolean hidden;
    public boolean showInSummary;

    public UiAttributeDefinition(String id, String name, String label, Integer keyOfDefinitionId,
                                 boolean calculated, boolean calculatedOnlyOneTime, boolean hidden,
                                 String description, String prompt, String interviewLabel, boolean required) {
        super(id, name, label, keyOfDefinitionId, description, prompt, interviewLabel, required);
        this.calculated = calculated;
        this.calculatedOnlyOneTime = calculatedOnlyOneTime;
        this.hidden = hidden;
    }

    public UiAttributeDefinition(String id, String name, String label, boolean required) {
        super(id, name, label, required);
        calculated = false;
        calculatedOnlyOneTime = false;
        hidden = false;
    }

    public boolean isShowInSummary() {
        return showInSummary;
    }

    public void setShowInSummary(boolean showInSummary) {
        this.showInSummary = showInSummary;
    }

}
