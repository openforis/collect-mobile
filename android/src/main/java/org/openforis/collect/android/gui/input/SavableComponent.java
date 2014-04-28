package org.openforis.collect.android.gui.input;

import android.content.Context;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.*;

import java.util.Map;
import java.util.Set;

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

    public abstract void onAttributeChange(UiAttribute attribute);

    public abstract void onValidationError(Map<UiAttribute, Set<UiValidationError>> validationErrorsByAttribute);

    protected abstract void resetValidationErrors();


    public final void onSelect() {
        selected = true;
        validateNode();
    }

    public final void onDeselect() {
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

    protected final void focus(View inputView) {
        inputView.requestFocus();
        if (inputView instanceof EditText) {
            InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(inputView, InputMethodManager.SHOW_FORCED);
        }
    }

    public static <T extends UiNode> SavableComponent create(T node, SurveyService surveyService, FragmentActivity context) {
        if (node instanceof UiAttribute)
            return createAttributeComponent((UiAttribute) node, surveyService, context);
        if (node instanceof UiAttributeCollection)
            return createAttributeCollectionComponent((UiAttributeCollection) node, surveyService, context);
        throw new IllegalStateException("Unexpected node type: " + node.getClass());
    }

    private static AttributeComponent createAttributeComponent(UiAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        if (attribute instanceof UiTextAttribute)
            return new TextAttributeComponent((UiTextAttribute) attribute, surveyService, context);
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
//        if (attribute instanceof UiTaxonAttribute)
//            return new TaxonComponent((UiTaxonAttribute) attribute, context);
        // TODO: Other attribute types
//        throw new IllegalStateException("Unexpected attribute type: " + attribute.getClass());
        return new DummyAttributeComponent(attribute, surveyService, context);
    }

    private static SavableComponent createAttributeCollectionComponent(UiAttributeCollection attributeCollection, SurveyService surveyService, FragmentActivity context) {
        Class<? extends UiAttribute> attributeType = attributeCollection.getDefinition().attributeType;
        if (attributeType.isAssignableFrom(UiTextAttribute.class))
            return new TextAttributeCollectionComponent(attributeCollection, surveyService, context);
        // TODO: Other attribute types in collection
//        throw new IllegalStateException("Attribute collection contains attributes of unexpected type: " + attributeType);
        return new DummyAttributeCollectionComponent(surveyService, context);
    }

    private static class DummyAttributeCollectionComponent extends SavableComponent {
        private final TextView view;

        private DummyAttributeCollectionComponent(SurveyService surveyService, FragmentActivity context) {
            super(surveyService, context);
            view = new TextView(context);
            view.setText("Unsupported type");
        }

        public void onValidationError(Map<UiAttribute, Set<UiValidationError>> validationErrorsByAttribute) {

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

        public void onAttributeChange(UiAttribute attribute) {

        }

        protected void resetValidationErrors() {

        }
    }

    private static class DummyAttributeComponent extends AttributeComponent {
        private final TextView view;

        private DummyAttributeComponent(UiAttribute attribute, SurveyService surveyService, FragmentActivity context) {
            super(attribute, surveyService, context);
            view = new TextView(context);
            view.setText("Unsupported type");
        }

        protected boolean updateAttributeIfChanged() {
            return false;
        }

        protected View toInputView() {
            return view;
        }
    }
}
