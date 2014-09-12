package org.openforis.collect.android.collectadapter;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.Calculable;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Node;

public class AttributeUtils {

    public static boolean isCalculated(Node<? extends NodeDefinition> node) {
        return isCalculated(node.getDefinition());
    }

    public static boolean isCalculated(NodeDefinition nodeDefinition) {
        return nodeDefinition instanceof Calculable && ((Calculable) nodeDefinition).isCalculated();
    }

    public static boolean isHidden(Survey survey, NodeDefinition definition) {
        return ((CollectSurvey) survey).getUIOptions().isHidden(definition);
    }

    public static boolean isShown(Survey survey, NodeDefinition definition) {
        return !isHidden(survey, definition);
    }

    public static boolean isHidden(Node node) {
        return isHidden(node.getSurvey(), node.getDefinition());
    }

    public static boolean isShown(Node node) {
        return !isHidden(node);
    }
}
