package org.openforis.collect.android.gui.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.openforis.collect.R;
import org.openforis.collect.android.gui.input.AttributeCollectionInputComponent;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiAttributeCollection;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

// TODO: Implement this...

/**
 * @author Daniel Wiell
 */
public class AttributeCollectionDetailFragment extends NodeDetailFragment<UiAttributeCollection> {
    private AttributeCollectionInputComponent inputComponent;

    public View createView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        inputComponent = AttributeCollectionInputComponent.create(node(), inflater, getActivity());
        View rootView = inputComponent.getRootView();
        ((TextView) rootView.findViewById(R.id.node_label)).setText(node().getLabel());
        TextView nodeLabel = (TextView) rootView.findViewById(R.id.node_label);
        nodeLabel.setText(node().getLabel());
        addInputComponentToView(rootView);
        return rootView;
    }

    protected View getDefaultFocusedView() {
        return inputComponent.getDefaultFocusedView();
    }

    public void onPause() {
        inputComponent.updateAttributeCollection();
        super.onPause();
    }

    public void onDeselect() {
        super.onDeselect();
        if (inputComponent != null)
            inputComponent.updateAttributeCollection();
    }

    public void onAttributeChange(UiAttribute attribute) {
        if (inputComponent != null)
            inputComponent.onAttributeChange(attribute);
    }

    private void addInputComponentToView(View rootView) {
        ViewGroup attributeInputContainer = (ViewGroup) rootView.findViewById(R.id.input_container);
        View view = inputComponent.getView();
        view.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        attributeInputContainer.addView(view);
    }
}
