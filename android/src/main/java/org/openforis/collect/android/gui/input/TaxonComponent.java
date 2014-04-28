package org.openforis.collect.android.gui.input;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ScrollView;
import android.widget.TextView;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.viewmodel.UiTaxon;
import org.openforis.collect.android.viewmodel.UiTaxonAttribute;

/**
 * @author Daniel Wiell
 */
class TaxonComponent  {
//        extends AttributeInputComponent<UiTaxonAttribute> { // TODO: Refactor - remove duplication from CodeComponent
//    private final ViewGroup view;
//    private final AutoCompleteTextView autoComplete;
//    private UiTaxon selectedTaxon;
//
//
//    TaxonComponent(UiTaxonAttribute attribute, Context context) {
//        super(attribute, context);
//        autoComplete = new AutoCompleteTextView(context());
//        autoComplete.setThreshold(1);
//        autoComplete.setSingleLine();
//        if (attribute().getTaxon() != null) {
//            setText(attribute().getTaxon().toString());
//            selectedTaxon = attribute().getTaxon();
//        }
//        autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                selectedTaxon = (UiTaxon) autoComplete.getAdapter().getItem(position);
//                updateAttribute();
//            }
//        });
//        autoComplete.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                selectedTaxon = (UiTaxon) autoComplete.getAdapter().getItem(position);
//            }
//
//            public void onNothingSelected(AdapterView<?> parent) {
//                selectedTaxon = null;
//            }
//        });
//        autoComplete.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_DONE)
//                    updateAttribute();
//                return false;
//            }
//        });
//        autoComplete.setAdapter(new UiTaxonAdapter(context, attribute(), ServiceLocator.taxonService()));
//        view = createView(autoComplete);
//    }
//
//    private ViewGroup createView(AutoCompleteTextView autoComplete) {
//        ViewGroup input = new ScrollView(context());
//        input.addView(autoComplete);
//        return input;
//    }
//
//    public View getView() {
//        return view;
//    }
//
//    public void updateAttribute() {
//        UiTaxon newTaxon = selectedTaxon();
//        if (hasChanged(newTaxon)) {
//            attribute().setTaxon(selectedTaxon);
//            notifyAboutAttributeChange();
//        }
//    }
//
//    private UiTaxon selectedTaxon() {
//        String text = autoComplete.getText().toString();
//        if (selectedTaxon == null || !selectedTaxon.toString().equals(text)) {
//            if (StringUtils.isEmpty(text))
//                selectedTaxon = null;
////            else // TODO: Support entering taxon code directly, without selecting something?
////                selectedTaxon = uiTaxonByValue.get(text.trim());
//        }
//        if (selectedTaxon == null) {
//            setText("");
//            return null;
//        }
//        setText(selectedTaxon.toString());
//        return selectedTaxon;
//    }
//
//    public View getDefaultFocusedView() {
//        return autoComplete;
//    }
//
//    private boolean hasChanged(UiTaxon newTaxon) {
//        UiTaxon oldTaxon = attribute().getTaxon();
//        if (oldTaxon == null)
//            return newTaxon != null;
//        return !oldTaxon.equals(newTaxon);
//    }
//
//    @SuppressWarnings("ConstantConditions")
//    private void setText(String text) {
//        // Hack to prevent pop-up from opening when setting text
//        // http://www.grokkingandroid.com/how-androids-autocompletetextview-nearly-drove-me-nuts/
//        UiTaxonAdapter adapter = (UiTaxonAdapter) autoComplete.getAdapter();
//        autoComplete.setAdapter(null);
//        autoComplete.setText(text);
//        autoComplete.setAdapter(adapter);
//    }
//
//    public String toString() {
//        return attribute().toString();
//    }

}
