package org.openforis.collect.android.gui.input;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import org.openforis.collect.R;
import org.openforis.collect.android.viewmodel.UiCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Wiell
 */
class UiCodeAdapter extends ArrayAdapter<UiCode> {
//    private static final int LAYOUT_RESOURCE_ID = R.layout.listview_code;
//    private static final int LAYOUT_RESOURCE_ID = android.R.layout.simple_list_item_1;
    private static final int LAYOUT_RESOURCE_ID = android.R.layout.simple_dropdown_item_1line;
    private final Context context;
    private final List<UiCode> codes;
    private List<UiCode> filteredCodes;

    UiCodeAdapter(Context context, List<UiCode> codes) {
        super(context, LAYOUT_RESOURCE_ID, codes);
        this.context = context;
        this.codes = codes;
        filteredCodes = codes;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        CodeHolder holder;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(LAYOUT_RESOURCE_ID, parent, false);

            holder = new CodeHolder();
//            holder.codeValue = (TextView) row.findViewById(R.id.codeListItemCode);
//            holder.label = (TextView) row.findViewById(R.id.codeListItemLabel);
            holder.code = (TextView) row.findViewById(android.R.id.text1);

            row.setTag(holder);
        } else {
            holder = (CodeHolder) row.getTag();
        }

        UiCode code = filteredCodes.get(position);
        holder.code.setText(code.toString());
//        holder.codeValue.setText(codeValue.getValue());
//        holder.label.setText(codeValue.getLabel());

        return row;
    }

    public UiCode getItem(int position) {
        return filteredCodes.get(position);
    }

    public int getCount() {
        return filteredCodes.size();
    }

    public Filter getFilter() {
        return new Filter() {
            protected FilterResults performFiltering(CharSequence constraint) {
                List<UiCode> codes = findCodes(constraint);
                FilterResults results = new FilterResults();
                results.values = codes;
                results.count = codes.size();
                return results;
            }

            private List<UiCode> findCodes(CharSequence constraint) {
                if (constraint == null || constraint.length() == 0)
                    return codes;
                String query = constraint.toString().trim().toLowerCase();
                ArrayList<UiCode> matchingCodes = new ArrayList<UiCode>();
                for (UiCode code : codes) {
                    if (matches(code, query))
                        matchingCodes.add(code);
                }
                return matchingCodes;
            }

            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredCodes = (List<UiCode>) results.values;
                if (results.count > 0)
                    notifyDataSetChanged();
                else
                    notifyDataSetInvalidated();
            }
        };
    }

    private boolean matches(UiCode code, String query) {
        return matches(query, code.getValue()) || matches(query, code.getLabel()) ||  matches(query, code.toString());
    }

    private boolean matches(String query, String s) {
        return (s != null && s.toLowerCase().startsWith(query));
    }

    private static class CodeHolder {
        TextView code;
        TextView label;
    }
}
