package org.openforis.collect.android.gui.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.openforis.collect.R;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.input.SavableComponent;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiNodeChange;

import java.util.Map;

/**
 * @author Daniel Wiell
 */
public class SavableNodeDetailFragment<T extends UiNode> extends NodeDetailFragment<T> {
    private SavableComponent savableComponent;

    protected final View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        savableComponent = SavableComponent.create(node(), ServiceLocator.surveyService(), getActivity());
        ViewGroup view = (ViewGroup) inflater.inflate(savableComponent.getViewResource(), container, false);
        if (view == null)
            throw new IllegalStateException("View is null");
        savableComponent.setupView(view);
        return view;
    }

    protected View getDefaultFocusedView() {
        return savableComponent == null ? null : savableComponent.getDefaultFocusedView();
    }

    public void onPause() {
        super.onPause();
        if (savableComponent != null)
            savableComponent.saveNode();
    }

    public void onDeselect() {
        super.onDeselect();
        if (savableComponent != null)
            savableComponent.onDeselect();
    }

    public void onSelected() {
        super.onSelected();
        if (savableComponent != null)
            savableComponent.onSelect();
    }

    public void onNodeChange(UiNode node, Map<UiNode, UiNodeChange> attributeChanges) {
        super.onNodeChange(node, attributeChanges);
        if (savableComponent != null) {
            savableComponent.onNodeChange(node, attributeChanges);
            if (savableComponent.hasChanged()) {
                showAttributeSavingError();
            } else {
                hideAttributeSavingLoader();
            }
        }
    }


}
