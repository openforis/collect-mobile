package org.openforis.collect.android.gui.breadcrumbs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.openforis.collect.R;
import org.openforis.collect.android.gui.NodeNavigator;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.viewmodel.UiInternalNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * A fragment representing the parents of a node.
 */
public class NodeParentsFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        UiInternalNode node = ServiceLocator.surveyService().selectedNode().getParent();
        View view = inflater.inflate(R.layout.fragment_node_parents, container, false);
        final HorizontalScrollView scrollView = (HorizontalScrollView) view.findViewById(R.id.node_parents_scroll_view);
        scrollView.setHorizontalScrollBarEnabled(false);
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
        ViewGroup parentsContainer = (ViewGroup) view.findViewById(R.id.node_parents_container);
        List<View> buttons = getParentButtons(node);
        Collections.reverse(buttons);
        for (View button : buttons) {
            parentsContainer.addView(button);
            parentsContainer.addView(createSeparator());
        }
        View currentNodeView;
        currentNodeView = node.getParent() == null
                ? createSurveySelectedView()
                : createCurrentNodeView(node);
        parentsContainer.addView(currentNodeView);
        super.onViewCreated(view, savedInstanceState);
        return view;
    }

    private View createSurveySelectedView() {
        ImageView imageView = new ImageView(getActivity());
        imageView.setImageResource(R.drawable.ic_menu_home);
        return imageView;
    }

    private View createCurrentNodeView(UiInternalNode node) {
        TextView nodeLabel = new TextView(getActivity());
        nodeLabel.setTextAppearance(getActivity(), R.style.BreadcrumbSelectedNode);
        nodeLabel.setText(node.getLabel());
        return nodeLabel;
    }

    private View createSeparator() {
        ImageView imageView = new ImageView(getActivity());
        imageView.setImageResource(R.drawable.ic_action_next_item);
        return imageView;
    }

    private List<View> getParentButtons(UiInternalNode node) {
        if (node.getParent() == null)
            return Collections.emptyList();
        List<View> views = new ArrayList<View>();
        if (node.getParent().excludeWhenNavigating())
            node = node.getParent();
        views.add(createBreadcrumbsButton(node));
        views.addAll(getParentButtons(node.getParent()));
        return views;
    }

    private View createBreadcrumbsButton(final UiInternalNode node) {
        UiInternalNode parentNode = node.getParent();
        View view = parentNode.getParent() == null
                ? createSurveyButton()
                : createNodeButton(parentNode);
        view.setLayoutParams(new ViewGroup.LayoutParams(WRAP_CONTENT, MATCH_PARENT));
        view.setBackgroundResource(R.drawable.breadcrumb_button);
        view.setPadding(0, 0, 0, 0);
        view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                nodeNavigator().navigateTo(node.getId());
            }
        });
        return view;
    }

    private View createNodeButton(UiInternalNode parentNode) {
        Button button = new Button(getActivity());
        button.setText(parentNode.getLabel());
        int textColor = getResources().getColor(R.color.default_title_indicator_text_color);
        button.setTextColor(textColor);
        button.setMinimumWidth(1);
        return button;
    }

    private View createSurveyButton() {
        ImageButton button = new ImageButton(getActivity());
        button.setImageResource(R.drawable.ic_menu_home);
        return button;
    }

    private NodeNavigator nodeNavigator() {
        return (NodeNavigator) getActivity();
    }
}
