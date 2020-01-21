package org.openforis.collect.android.viewmodel;

import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.collection.Predicate;

import java.util.ArrayList;
import java.util.List;

public class NodeMatrix {
    private final List<Definition> headerRows = new ArrayList<Definition>();
    private final List<UiInternalNode> rows = new ArrayList<UiInternalNode>();

    public NodeMatrix(UiInternalNode node) {
        if (node == null)
            throw new IllegalArgumentException("node is null");
        headerRows.addAll(headerRows(node));
        rows.addAll(rows(node));
    }

    public List<Definition> headerRow() {
        return headerRows;
    }

    public List<UiInternalNode> rows() {
        return rows;
    }

    public int rowCount() {
        return rows.size();
    }

    public int columnCount() {
        return headerRows.size();
    }

    public UiNode nodeAt(int row, int column) {
        UiInternalNode rowNode = rows().get(row);
        List<UiNode> childrenFiltered = getRowChildrenVisible(rowNode);
        return column >= 0 && column < childrenFiltered.size() ? childrenFiltered.get(column) : null;
    }

    public Definition headerAt(int column) {
        return headerRow().get(column);
    }

    private List<Definition> headerRows(UiInternalNode node) {
        ArrayList<Definition> childDefinitions = new ArrayList<Definition>();
        for (UiNode childNode : getRowChildrenVisible(node)) {
            Definition childDef = childNode.getDefinition();
            childDefinitions.add(childDef);
        }
        return childDefinitions;
    }

    private List<UiInternalNode> rows(UiNode node) {
        List<UiInternalNode> rows = new ArrayList<UiInternalNode>();
        UiNode root = rootNode(node);

        if (root == null)
            rows.add((UiInternalNode) node);
        else
            for (UiNode nodeWithName : root.findAllByName(node.getName()))
                if (nodeWithName instanceof UiEntity)
                    rows.add((UiInternalNode) nodeWithName);
        return rows;
    }

    private UiNode rootNode(UiNode node) {
        UiNode root = entityCollection(node);
        if (root == null)
            root = node.getUiRecord();
        return root;
    }


    private UiEntityCollection entityCollection(UiNode node) {
        if (node instanceof UiEntityCollection)
            return (UiEntityCollection) node;
        if (node.getParent() == null)
            return null;
        return entityCollection(node.getParent());
    }

    public int rowIndex(UiInternalNode row) {
        return rows.indexOf(row);
    }

    public int columnIndex(UiNode node) {
        UiInternalNode row = node.getParent();
        List<UiNode> childrenVisible = getRowChildrenVisible(row);
        return childrenVisible.indexOf(node);
    }

    private List<UiNode> getRowChildrenVisible(UiInternalNode rowNode) {
        List<UiNode> children = rowNode.getChildren();
        List<UiNode> childrenFiltered = new ArrayList<UiNode>(children);
        CollectionUtils.filter(childrenFiltered, new Predicate<UiNode>() {
            public boolean evaluate(UiNode node) {
                Definition def = node.getDefinition();
                // Exclude hidden attribute ("calculated" attributes not "Show in entry form")
                return !(def instanceof UiAttributeDefinition && ((UiAttributeDefinition) def).hidden);
            }
        });
        return childrenFiltered;
    }
}
