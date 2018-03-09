package org.openforis.collect.android.gui.detail;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.util.Attrs;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiEntity;
import org.openforis.collect.android.viewmodel.UiInternalNode;
import org.openforis.collect.android.viewmodel.UiNode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openforis.collect.android.gui.list.EntityListAdapter.MAX_ATTRIBUTE_LABEL_LENGTH;
import static org.openforis.collect.android.gui.list.EntityListAdapter.MAX_ATTRIBUTE_VALUE_LENGTH;
import static org.openforis.collect.android.gui.util.Views.px;
import static org.openforis.collect.android.util.StringUtils.ellipsisMiddle;

public class NodePathDetailsFragment extends Fragment {
    private UiInternalNode uiNode;
    private Set<UiAttribute> keyAttributes = new HashSet<UiAttribute>();
    private Map<UiEntity, List<UiAttribute>> keyAttributesByEntity;
    private Attrs attrs;
    private LinearLayout view;

    public NodePathDetailsFragment() {
        SurveyService surveyService = ServiceLocator.surveyService();
        if (surveyService != null) {
            UiNode selectedNode = surveyService.selectedNode();
            if (selectedNode != null) {
                uiNode = selectedNode.getParent();
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        attrs = new Attrs(getActivity());
        keyAttributesByEntity = keyAttributesByEntity(uiNode);
        for (List<UiAttribute> uiAttributes : keyAttributesByEntity.values())
            keyAttributes.addAll(uiAttributes);
        return createView();
    }

    public void nodeChanged(UiNode node) {
        if (view != null && keyAttributes.contains(node))
            replaceView();
    }

    private void replaceView() {
        ViewGroup parent = (ViewGroup) view.getParent();
        parent.removeView(view);
        parent.addView(createView());
    }

    private View createView() {
        view = new LinearLayout(getActivity());
        Context c = getContext();
        view.setPadding(px(c, 16), px(c, 8), px(c, 16), px(c,8));
        view.setOrientation(LinearLayout.VERTICAL);
        view.setBackgroundColor(attrs.color(R.attr.nodePathDetailsBackground));

        addKeyAttributes(view);
        return view;
    }

    private void addKeyAttributes(LinearLayout view) {
        for (Map.Entry<UiEntity, List<UiAttribute>> entry : keyAttributesByEntity.entrySet()) {
            UiEntity entity = entry.getKey();
            List<UiAttribute> keyAttributes = entry.getValue();

            StringBuilder s = new StringBuilder();
            for (Iterator<UiAttribute> iterator = keyAttributes.iterator(); iterator.hasNext(); ) {
                UiAttribute keyAttribute = iterator.next();
                String value = keyAttribute.valueAsString();
                value = value == null ? getString(R.string.label_unspecified) : value;
                s.append(ellipsisMiddle(keyAttribute.getLabel(), MAX_ATTRIBUTE_LABEL_LENGTH)).append(": ")
                        .append(ellipsisMiddle(value, MAX_ATTRIBUTE_VALUE_LENGTH));
                if (iterator.hasNext())
                    s.append(", ");
            }
            s.append(" (").append(ellipsisMiddle(entity.getLabel(), 20)).append(')');

            TextView textView = new TextView(getActivity());
            textView.setText(s);
            view.addView(textView);
        }
    }

    private Map<UiEntity, List<UiAttribute>> keyAttributesByEntity(UiInternalNode node) {
        if (node == null)
            return Collections.emptyMap();
        Map<UiEntity, List<UiAttribute>> keyAttributesByEntity = new LinkedHashMap<UiEntity, List<UiAttribute>>();
        if (node instanceof UiEntity) {
            UiEntity entity = (UiEntity) node;
            List<UiAttribute> keyAttributes = entity.getKeyAttributes();
            if (!keyAttributes.isEmpty())
                keyAttributesByEntity.put(entity, keyAttributes);
        } else {
            for (UiNode siblingNode : uiNode.getChildren()) {
                if (siblingNode instanceof UiEntity) {
                    UiEntity entity = (UiEntity) siblingNode;
                    List<UiAttribute> keyAttributes = entity.getKeyAttributes();
                    if (!keyAttributes.isEmpty())
                        keyAttributesByEntity.put(entity, keyAttributes);
                }
            }
        }
        keyAttributesByEntity.putAll(keyAttributesByEntity(node.getParent()));
        return keyAttributesByEntity;
    }

    // TODO: Listen for attribute updates
}
