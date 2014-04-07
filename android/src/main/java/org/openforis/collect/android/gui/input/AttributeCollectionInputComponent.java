package org.openforis.collect.android.gui.input;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiAttributeCollection;
import org.openforis.collect.android.viewmodel.UiTextAttribute;

/**
 * @author Daniel Wiell
 */
public abstract class AttributeCollectionInputComponent<T extends UiAttribute> {
    private final Context context;
    private final LayoutInflater inflater;
    protected final SurveyService surveyService;

    protected AttributeCollectionInputComponent(LayoutInflater inflater, Context context) {
        this.inflater = inflater;
        this.context = context;
        this.surveyService = ServiceLocator.surveyService();
    }

    public abstract View getView();

    public View getRootView() {
        return inflate(R.layout.fragment_attribute_detail);
    }

    protected View inflate(int resource) {
        return inflater.inflate(resource, null, false);
    }

    public abstract void updateAttributeCollection();

    public View getDefaultFocusedView() {
        return null;
    }

    /**
     * Invoked when an attribute has changed.
     */
    public void onAttributeChange(UiAttribute attribute) {
        // Empty default implementation
    }

    protected final Context context() {
        return context;
    }

    public static AttributeCollectionInputComponent create(UiAttributeCollection attributeCollection, LayoutInflater inflater, Context context) {
        if (attributeCollection.getDefinition().isOfAttributeType(UiTextAttribute.class))
            return new TextCollectionComponent(attributeCollection, inflater, context);
        return new DummyInputComponent(inflater, context);
    }

    private static class DummyInputComponent extends AttributeCollectionInputComponent {
        @SuppressWarnings("unchecked")
        public DummyInputComponent(LayoutInflater inflater, Context context) {
            super(inflater, context);
        }

        public View getView() {
            TextView textView = new TextView(context());
            textView.setText("An Attribute Collection");
            return textView;
        }

        public void updateAttributeCollection() {

        }
    }
}
