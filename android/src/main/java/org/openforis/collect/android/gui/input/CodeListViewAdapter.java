package org.openforis.collect.android.gui.input;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.gui.util.Keyboard;
import org.openforis.collect.android.gui.util.Views;
import org.openforis.collect.android.viewmodel.UiCode;
import org.openforis.collect.android.viewmodel.UiCodeList;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

class CodeListViewAdapter extends BaseAdapter {

    private UiCodeList codeList;
    private boolean valueShown;
    private Runnable changeHandler;

    private LayoutInflater inflater;
    private Map<String, CodeAttributeComponent.CodeValue> selectedValuesByCode;
    private boolean singleSelection;

    CodeListViewAdapter(Context applicationContext, Set<CodeAttributeComponent.CodeValue> selectedCodes, UiCodeList codeList,
                        boolean valueShown, boolean singleSelection, Runnable changeHandler) {
        this.codeList = codeList;
        this.valueShown = valueShown;
        this.singleSelection = singleSelection;
        this.changeHandler = changeHandler;

        inflater = (LayoutInflater.from(applicationContext));

        setSelectedCodes(selectedCodes);
    }

    @Override
    public int getCount() {
        return codeList.getCodes().size();
    }

    @Override
    public Object getItem(int i) {
        return codeList.getCodes().get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final ViewHolder viewHolder;
        if (view == null) {
            view = inflater.inflate(R.layout.code_item, null);

            viewHolder = new ViewHolder();

            viewHolder.radioButton = view.findViewById(R.id.item_radiobutton);
            viewHolder.radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    handleItemSelection(viewHolder, checked);
                }
            });
            viewHolder.qualifier = view.findViewById(R.id.item_specify_field);
            viewHolder.qualifier.addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    handleQualifierChange(viewHolder, charSequence == null ? null : charSequence.toString());
                }

                public void afterTextChanged(Editable editable) {
                }
            });

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        fillView(viewHolder, i);

        return view;
    }

    private void handleItemSelection(ViewHolder viewHolder, boolean checked) {
        if (!singleSelection || checked) {
            UiCode uiCode = codeList.getCodes().get(viewHolder.index);
            if (singleSelection) {
                selectedValuesByCode.clear();
            }
            if (checked) {
                selectedValuesByCode.put(uiCode.getValue(), new CodeAttributeComponent.CodeValue(uiCode.getValue()));
                if (!codeList.isQualifiable(uiCode)) {
                    Keyboard.hide(viewHolder.qualifier.getContext());
                }
            } else {
                selectedValuesByCode.remove(uiCode.getValue());
            }
            notifyDataSetChanged();
            this.changeHandler.run();
        }
    }

    private void handleQualifierChange(ViewHolder viewHolder, String text) {
        UiCode uiCode = codeList.getCodes().get(viewHolder.index);
        CodeAttributeComponent.CodeValue value = selectedValuesByCode.get(uiCode.getValue());
        value.qualifier = text;
        this.changeHandler.run();
    }

    public Set<CodeAttributeComponent.CodeValue> getSelectedCodes() {
        return new LinkedHashSet<CodeAttributeComponent.CodeValue>(selectedValuesByCode.values());
    }

    public void setSelectedCodes(Set<CodeAttributeComponent.CodeValue> values) {
        this.selectedValuesByCode = new HashMap<String, CodeAttributeComponent.CodeValue>(values.size());
        for (CodeAttributeComponent.CodeValue value : values) {
            selectedValuesByCode.put(value.code, value);
        }
    }

    public void resetSelectedCodes() {
        this.setSelectedCodes(Collections.<CodeAttributeComponent.CodeValue>emptySet());
    }

    private void fillView(final ViewHolder viewHolder, int index) {
        UiCode uiCode = codeList.getCodes().get(index);

        CodeAttributeComponent.CodeValue value = selectedValuesByCode.get(uiCode.getValue());
        boolean checked = value != null;
        boolean qualifiable = codeList.isQualifiable(uiCode) && checked;

        viewHolder.index = index;
        viewHolder.radioButton.setText(
                valueShown
                        ? String.format("%s - %s", uiCode.getValue(), uiCode.getLabel())
                        : uiCode.getLabel()
        );
        viewHolder.radioButton.setChecked(checked);
        Views.toggleVisibility(viewHolder.qualifier, qualifiable);
        if (qualifiable) {
            viewHolder.qualifier.setText(value.qualifier);
            if (codeList.isQualifiable(uiCode) && StringUtils.isEmpty(value.qualifier)) {
                //set focus on qualifier text edit and show keyboard
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        viewHolder.qualifier.requestFocus();
                        Keyboard.show(viewHolder.qualifier, viewHolder.qualifier.getContext());
                    }
                }, 100);
            }

        }
    }

    private static class ViewHolder {
        int index;
        RadioButton radioButton;
        TextView qualifier;
    }
}
