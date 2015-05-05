package org.openforis.collect.android.gui.input;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.LinearLayout;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.util.Keyboard;
import org.openforis.collect.android.viewmodel.*;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Daniel Wiell
 */
class AutoCompleteCodeAttributeCollectionComponent extends CodeAttributeCollectionComponent {
    private final Map<UiAttribute, CodeAttributeComponent> attributeComponentByAttribute = new LinkedHashMap<UiAttribute, CodeAttributeComponent>();
    private final LinearLayout view;

    AutoCompleteCodeAttributeCollectionComponent(UiAttributeCollection attributeCollection, CodeListService codeListService, SurveyService surveyService, FragmentActivity context) {
        super(attributeCollection, codeListService, surveyService, context);
        view = new LinearLayout(context);
        view.setOrientation(LinearLayout.VERTICAL);
        for (UiNode child : attributeCollection.getChildren())
            addAttributeToComponent((UiCodeAttribute) child);
    }

    protected View.OnClickListener onAddAttribute() {
        return new View.OnClickListener() {
            public void onClick(View v) {
                UiCodeAttribute attribute = (UiCodeAttribute) surveyService.addAttribute();
                addAttributeToComponent(attribute);
                setValidationError(attribute, attribute.getValidationErrors());
            }
        };
    }

    protected final View toInputView() {
        return view;
    }

    protected final void setValidationError(UiAttribute attribute, Set<UiValidationError> validationErrors) {
        AttributeComponent attributeComponent = attributeComponentByAttribute.get(attribute);
        if (attributeComponent != null) // While we're adding an attribute, it's not in the map yet
            attributeComponent.setValidationError(validationErrors);
    }

    protected void resetValidationErrors() {
        for (AttributeComponent attributeComponent : attributeComponentByAttribute.values())
            attributeComponent.resetValidationErrors();
    }

    protected Set<UiAttribute> updateChangedAttributes() {
        Set<UiAttribute> changedAttributes = new HashSet<UiAttribute>();
        for (AttributeComponent attributeComponent : attributeComponentByAttribute.values())
            if (attributeComponent.updateAttributeIfChanged())
                changedAttributes.add(attributeComponent.attribute);
        return changedAttributes;
    }

    private void addAttributeToComponent(UiCodeAttribute attribute) {
        CodeAttributeComponent attributeComponent = createAttributeComponent(attribute);
        attributeComponentByAttribute.put(attribute, attributeComponent);
        View inputView = attributeComponent.toInputView();
        view.addView(inputView);
        showKeyboard(inputView);
    }

    protected CodeAttributeComponent createAttributeComponent(UiCodeAttribute attribute) {
        return new AutoCompleteCodeAttributeComponent(attribute, codeListService, surveyService, context);
    }
}
