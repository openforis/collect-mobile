package org.openforis.collect.android.gui.entitytable;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.inqbarna.tablefixheaders.adapters.BaseTableAdapter;
import org.openforis.collect.R;
import org.openforis.collect.android.gui.SurveyNodeActivity;
import org.openforis.collect.android.gui.util.Attrs;
import org.openforis.collect.android.viewmodel.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.graphics.Typeface.BOLD;
import static android.graphics.Typeface.NORMAL;
import static org.openforis.collect.android.util.StringUtils.ellipsisMiddle;

public class NodeMatrixTableAdapter extends BaseTableAdapter {
    public static final int MAX_ATTRIBUTE_LABEL_LENGTH = 20;
    public static final int MAX_ATTRIBUTE_VALUE_LENGTH = 20;

    private final static int MAX_WIDTH_DIP = 110;
    private final static int MAX_HEIGHT_DIP = 37;

    private final Context context;
    private final LayoutInflater inflater;

    private NodeMatrix nodeMatrix;

    private final int selectedRow;
    private final int selectedColumn;

    private final int width;
    private final int height;

    public NodeMatrixTableAdapter(UiNode selectedNode, Context context) {
        nodeMatrix = new NodeMatrix(selectedNode.getParent());
        this.context = context;
        inflater = LayoutInflater.from(context);
        Resources r = context.getResources();


        width = dpToPixels(r, MAX_WIDTH_DIP + 10);
        height = dpToPixels(r, MAX_HEIGHT_DIP + 10);

        selectedRow = nodeMatrix.rowIndex(selectedNode.getParent());
        selectedColumn = nodeMatrix.columnIndex(selectedNode);
    }

    public int[] selectedCoordinate() {
        return new int[] {
                selectedColumn * width - width,
                selectedRow * height - height
        };
    }

    @Override
    public int getRowCount() {
        return nodeMatrix.rowCount();
    }

    @Override
    public int getColumnCount() {
        return nodeMatrix.columnCount();
    }

    @Override
    public View getView(int row, int column, View convertView, ViewGroup parent) {
        final TextView textView;
        if (convertView == null) {
            textView = (TextView) inflater.inflate(R.layout.entity_table_cell, parent, false);
            if (!isHeader(row, column)) {
                textView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Object tag = v.getTag();
                        if (tag != null) {
                            UiNode node = (UiNode) tag;
                            if (node.isRelevant()) {
                                ((SurveyNodeActivity) context).navigateTo(node.getId());
                            } else {
                                Toast.makeText(context, R.string.toast_not_relevant_attribute_selected, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        } else
            textView = (TextView) convertView;
        if (!isHeader(row, column))
            textView.setTag(nodeMatrix.nodeAt(row, column));

        textView.setText(text(row, column));
        styleView(row, column, textView);
        return textView;
    }

    @Override
    public int getHeight(int row) {
        return height;
    }

    @Override
    public int getWidth(int column) {
        return width;
    }

    @Override
    public int getItemViewType(int row, int column) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    private void styleView(int row, int column, TextView textView) {
        textView.setTypeface(null, isHeader(row, column) ? BOLD : NORMAL);

        Attrs attrs = new Attrs(context);
        StateListDrawable states = new StateListDrawable();
        Drawable pressed = attrs.drawable(R.attr.entityTableCellPressed);
        states.addState(new int[]{android.R.attr.state_pressed}, pressed);
        states.addState(new int[]{}, new ColorDrawable(backgroundColor(row, column)));
        textView.setBackgroundDrawable(states);
    }

    private int backgroundColor(int row, int column) {
        boolean rowSelected = selectedRow == row;
        boolean columnSelected = selectedColumn == column;
        boolean selectedCell = rowSelected && columnSelected;
        boolean inSelectedRowOrColumn = (rowSelected && !columnSelected) || (!rowSelected && columnSelected);

        if (selectedCell)
            return Color.parseColor("#112a6d9d");
        if (inSelectedRowOrColumn)
            return Color.parseColor("#1182b8de");
        return Color.TRANSPARENT;
    }

    private String text(int row, int column) {
        String text = "";
        if (column < 0 && row != -1)
            text = rowHeader(row);
        else if (!isHeader(row, column)) {
            UiNode node = nodeMatrix.nodeAt(row, column);
            text = node instanceof UiAttribute ? ((UiAttribute) node).valueAsString() : "";
        } else if (column >= 0)
            text = nodeMatrix.headerAt(column).label;
        if (text == null)
            text = "";
        return text;
    }

    private boolean isHeader(int row, int column) {
        return row < 0 || column < 0;
    }

    private String rowHeader(int row) {
        UiInternalNode rowNode = nodeMatrix.rows().get(row);
        List<UiAttribute> keyAttributes = keyAttributes(rowNode);
        return attributesAsString(keyAttributes);
    }

    private List<UiAttribute> keyAttributes(UiInternalNode node) {
        List<UiAttribute> keyAttributes = new ArrayList<UiAttribute>();
        if (node == null)
            return keyAttributes;
        if (node instanceof UiEntity)
            keyAttributes = ((UiEntity) node).getKeyAttributes();
        return keyAttributes.isEmpty() ? keyAttributes(node.getParent()) : keyAttributes;
    }

    private String attributesAsString(List<UiAttribute> attributes) {
        StringBuilder s = new StringBuilder();
        for (Iterator<UiAttribute> iterator = attributes.iterator(); iterator.hasNext(); ) {
            UiAttribute keyAttribute = iterator.next();
            String value = keyAttribute.valueAsString();
            value = value == null ? context.getResources().getString(R.string.label_unspecified) : value;
            s.append(ellipsisMiddle(keyAttribute.getLabel(), MAX_ATTRIBUTE_LABEL_LENGTH)).append(": ")
                    .append(ellipsisMiddle(value, MAX_ATTRIBUTE_VALUE_LENGTH));
            if (iterator.hasNext())
                s.append(", ");
        }
        return s.toString();
    }

    private int dpToPixels(Resources r, int dp) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

}