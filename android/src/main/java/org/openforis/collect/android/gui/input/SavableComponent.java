package org.openforis.collect.android.gui.input;

import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.util.Keyboard;
import org.openforis.collect.android.viewmodel.UIFileAttributeDefinition;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiAttributeCollection;
import org.openforis.collect.android.viewmodel.UiBooleanAttribute;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;
import org.openforis.collect.android.viewmodel.UiCoordinateAttribute;
import org.openforis.collect.android.viewmodel.UiDateAttribute;
import org.openforis.collect.android.viewmodel.UiDoubleAttribute;
import org.openforis.collect.android.viewmodel.UiFileAttribute;
import org.openforis.collect.android.viewmodel.UiIntegerAttribute;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiNodeChange;
import org.openforis.collect.android.viewmodel.UiTaxonAttribute;
import org.openforis.collect.android.viewmodel.UiTextAttribute;
import org.openforis.collect.android.viewmodel.UiTextAttributeDefinition;
import org.openforis.collect.android.viewmodel.UiTimeAttribute;
import org.openforis.collect.metamodel.CollectAnnotations;

import java.util.Map;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * @author Daniel Wiell
 */
public abstract class SavableComponent {
    private boolean selected;
    protected final SurveyService surveyService;
    protected final FragmentActivity context;
    protected final Handler uiHandler = new Handler(); // Handler to post actions to UI thread.

    protected SavableComponent(SurveyService surveyService, FragmentActivity context) {
        this.surveyService = surveyService;
        this.context = context;
    }

    public abstract int getViewResource();

    public void setupView(ViewGroup view) {
        ViewGroup attributeInputContainer = (ViewGroup) view.findViewById(R.id.input_container);
        view.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        attributeInputContainer.addView(toInputView());
    }

    protected abstract View toInputView();

    public abstract void saveNode();

    public abstract void validateNode();

    public abstract void onNodeChange(UiNode node, Map<UiNode, UiNodeChange> nodeChanges);

    protected abstract void resetValidationErrors();

    public void onSelect() {
        selected = true;
        validateNode();
    }

    public void onDeselect() {
        selected = false;
        resetValidationErrors();
        saveNode();
    }

    public final boolean isSelected() {
        return selected;
    }

    public View getDefaultFocusedView() {
        return null; // By default, no view is focused
    }

    protected final void showKeyboard(View inputView) {
        if (isSelected())
            Keyboard.show(inputView, context);
    }

    protected final void hideKeyboard() {
        if (isSelected())
            Keyboard.hide(context);
    }

    protected TextView errorMessageContainerView() {
        return null;
    }

    protected void focusOnMessageContainerView() {
        TextView view = errorMessageContainerView();
        if (view != null) {
            view.requestFocus();
        }
    }

    protected final String resourceString(int resourceId) {
        return context.getResources().getString(resourceId);
    }

    protected FragmentActivity getContext() {
        return context;
    }

    public static <T extends UiNode> SavableComponent create(T node, SurveyService surveyService, FragmentActivity context) {
        if (node instanceof UiAttribute)
            return createAttributeComponent((UiAttribute) node, surveyService, context);
        if (node instanceof UiAttributeCollection)
            return createAttributeCollectionComponent((UiAttributeCollection) node, surveyService, context);
        throw new IllegalStateException("Unexpected node type: " + node.getClass());
    }

    private static AttributeComponent createAttributeComponent(UiAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        if (attribute instanceof UiTextAttribute) {
            if (((UiTextAttributeDefinition) attribute.getDefinition()).getInputType() == CollectAnnotations.TextInput.BARCODE) {
                return new BarcodeTextAttributeComponent((UiTextAttribute) attribute, surveyService, context);
            } else {
                return new TextAttributeComponent((UiTextAttribute) attribute, surveyService, context);
            }
        }
        if (attribute instanceof UiIntegerAttribute)
            return new IntegerAttributeComponent((UiIntegerAttribute) attribute, surveyService, context);
        if (attribute instanceof UiDoubleAttribute)
            return new DoubleAttributeComponent((UiDoubleAttribute) attribute, surveyService, context);
        if (attribute instanceof UiCodeAttribute)
            return CodeAttributeComponent.create((UiCodeAttribute) attribute, surveyService, context);
        if (attribute instanceof UiTimeAttribute)
            return new TimeAttributeComponent((UiTimeAttribute) attribute, surveyService, context);
        if (attribute instanceof UiCoordinateAttribute)
            return new CoordinateAttributeComponent((UiCoordinateAttribute) attribute, surveyService, context);
        if (attribute instanceof UiDateAttribute)
            return new DateAttributeComponent((UiDateAttribute) attribute, surveyService, context);
        if (attribute instanceof UiTaxonAttribute)
            return new TaxonAttributeComponent((UiTaxonAttribute) attribute, surveyService, context);
        if (attribute instanceof UiBooleanAttribute)
            return new BooleanAttributeComponent((UiBooleanAttribute) attribute, surveyService, context);
        if (attribute instanceof UiFileAttribute) {
            switch (((UIFileAttributeDefinition) attribute.getDefinition()).getType()) {
                case IMAGE:
                    return new ImageFileAttributeComponent((UiFileAttribute) attribute, surveyService, context);
                case AUDIO:
                    return new AudioFileAttributeComponent((UiFileAttribute) attribute, surveyService, context);
                case VIDEO:
                    return new VideoFileAttributeComponent((UiFileAttribute) attribute, surveyService, context);
                case DOCUMENT:
            }
        }
        return new UnsupportedAttributeComponent(attribute, surveyService, context);
    }

    private static SavableComponent createAttributeCollectionComponent(UiAttributeCollection attributeCollection, SurveyService surveyService, FragmentActivity context) {
        Class<? extends UiAttribute> attributeType = attributeCollection.getDefinition().attributeType;
        if (attributeType.isAssignableFrom(UiTextAttribute.class))
            return new TextAttributeCollectionComponent(attributeCollection, surveyService, context);
        if (attributeType.isAssignableFrom(UiCodeAttribute.class))
            return CodeAttributeCollectionComponent.create(attributeCollection, surveyService, context);
        return new UnsupportedAttributeCollectionComponent(attributeCollection, surveyService, context);
    }

    private static class UnsupportedAttributeCollectionComponent extends SavableComponent {
        private final TextView view;

        private UnsupportedAttributeCollectionComponent(UiAttributeCollection attributeCollection, SurveyService surveyService, FragmentActivity context) {
            super(surveyService, context);
            view = new TextView(context);
            view.setText("Unsupported attribute collection type: " + attributeCollection.getDefinition().attributeType.getSimpleName());
        }

        public int getViewResource() {
            return R.layout.fragment_attribute_detail;
        }

        protected View toInputView() {
            return view;
        }

        public void saveNode() {

        }

        public void validateNode() {

        }

        public void onNodeChange(UiNode node, Map<UiNode, UiNodeChange> nodeChanges) {

        }

        protected void resetValidationErrors() {

        }
    }

    private static class UnsupportedAttributeComponent extends AttributeComponent<UiAttribute> {
        private final TextView view;

        private UnsupportedAttributeComponent(UiAttribute attribute, SurveyService surveyService, FragmentActivity context) {
            super(attribute, surveyService, context);
            view = new TextView(context);
            view.setText("Unsupported attribute type: " + attribute.getClass().getSimpleName());
        }

        protected boolean updateAttributeIfChanged() {
            return false;
        }

        protected View toInputView() {
            return view;
        }
    }
}
