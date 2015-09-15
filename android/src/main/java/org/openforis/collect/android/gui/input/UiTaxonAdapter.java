package org.openforis.collect.android.gui.input;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.viewmodel.UiTaxon;
import org.openforis.collect.android.viewmodel.UiTaxonAttribute;
import org.openforis.collect.android.viewmodelmanager.TaxonService;

import java.util.List;

/**
 * @author Daniel Wiell
 */
public class UiTaxonAdapter extends BaseAdapter implements Filterable {
    private static final int MAX_RESULTS = 50;
    // TODO: Use custom layout
    private static final int LAYOUT_RESOURCE_ID = R.layout.taxon_dropdown_item;
    private final Context context;
    private final UiTaxonAttribute attribute;
    private final TaxonService taxonService;
    private List<UiTaxon> filteredValues;
    private String query = "";

    public UiTaxonAdapter(Context context, UiTaxonAttribute attribute, TaxonService taxonService) {
        this.context = context;
        this.attribute = attribute;
        this.taxonService = taxonService;
    }

    public UiTaxon getItem(int position) {
        return filteredValues.get(position);
    }

    public int getCount() {
        return filteredValues.size();
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        TaxonHolder holder;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(LAYOUT_RESOURCE_ID, parent, false);

            holder = new TaxonHolder();
            holder.text1 = (TextView) row.findViewById(android.R.id.text1);
            holder.text2 = (TextView) row.findViewById(android.R.id.text2);

            row.setTag(holder);
        } else {
            holder = (TaxonHolder) row.getTag();
        }

        UiTaxon taxon = filteredValues.get(position);
        holder.text1.setText(taxon.toString());
        if (!taxon.getCommonNames().isEmpty()) {
            String commonNames = StringUtils.join(taxon.getCommonNames(), ", ");
            holder.text2.setText(commonNames);
        } else
            holder.text2.setText("");

        return row;
    }

    public Filter getFilter() {
        return new Filter() {
            protected FilterResults performFiltering(CharSequence constraint) {
                constraint = constraint == null ? "" : ((String) constraint).trim();
                List values = taxonService.find(constraint.toString(), attribute.getDefinition().taxonomy, MAX_RESULTS);
                FilterResults results = new FilterResults();
                results.values = values;
                results.count = values.size();
                return results;
            }


            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredValues = (List<UiTaxon>) results.values;
                UiTaxonAdapter.this.query = (String) constraint;
                if (results.count > 0)
                    notifyDataSetChanged();
                else
                    notifyDataSetInvalidated();
            }
        };
    }

    private static class TaxonHolder {
        TextView text1;
        TextView text2;
    }
}
