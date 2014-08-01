package org.openforis.collect.android.collectadapter;

import org.openforis.idm.metamodel.Calculable;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Node;

public class CalculatedAttributeUtils {

    public static boolean isCalculated(Node<? extends NodeDefinition> node) {
        return isCalculated(node.getDefinition());
    }

    public static boolean isCalculated(NodeDefinition nodeDefinition) {
        return nodeDefinition instanceof Calculable && ((Calculable) nodeDefinition).isCalculated();
    }

}
