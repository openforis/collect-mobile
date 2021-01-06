package org.openforis.collect.android.gui.input;

import android.view.View;
import android.widget.LinearLayout;

import androidx.fragment.app.FragmentActivity;

import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiAttributeCollection;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiValidationError;

import java.util.ArrayList;
import java.util.Collection;
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

    public View getDefaultFocusedView() { // Focus the last component
        Collection<CodeAttributeComponent> components = attributeComponentByAttribute.values();
        if (components.isEmpty())
            return null;
        return new ArrayList<CodeAttributeComponent>(components).get(components.size() - 1).toInputView();
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

    protected void initOptions() {
        for (CodeAttributeComponent component : attributeComponentByAttribute.values())
            component.initOptions();
    }

    protected void resetSelection() {
        attributeComponentByAttribute.clear();
    }

    protected Set<UiAttribute> updateChangedAttributes() {
        Set<UiAttribute> changedAttributes = new HashSet<UiAttribute>();
        for (AttributeComponent attributeComponent : attributeComponentByAttribute.values())
            if (attributeComponent.updateAttributeIfChanged())
                changedAttributes.add(attributeComponent.attribute);
        deleteEmptyAttributes();
        return changedAttributes;
    }

    private void deleteEmptyAttributes() {
        for (CodeAttributeComponent component : attributeComponentByAttribute.values()) {
            if (component.attribute.isEmpty())
                deleteAttribute(component.attribute);
        }
    }

    private void addAttributeToComponent(UiCodeAttribute attribute) {
        deleteEmptyAttributes();
        CodeAttributeComponent attributeComponent = createAttributeComponent(attribute);
        attributeComponentByAttribute.put(attribute, attributeComponent);
        View inputView = attributeComponent.toInputView();
        view.addView(inputView);

        showKeyboard(inputView);
        // TODO: Register listener when clearing
    }

    private void deleteAttribute(UiCodeAttribute attribute) {
        surveyService.deletedAttribute(attribute.getId());
        CodeAttributeComponent component = attributeComponentByAttribute.remove(attribute);
        view.removeView(component.toInputView());
    }

    protected CodeAttributeComponent createAttributeComponent(UiCodeAttribute attribute) {
        return new AutoCompleteCodeAttributeComponent(attribute, codeListService, surveyService, context) {
            protected void initCodeList() {
                codeList = codeListService.codeList(attribute);
                AutoCompleteCodeAttributeCollectionComponent.this.initCodeList();
            }

            public void notifyAboutAttributeChange() {
                if (attribute.isEmpty())
                    deleteAttribute(attribute);
                else
                    super.notifyAboutAttributeChange();

            }
        };
    }
}
