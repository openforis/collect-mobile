package org.openforis.collect.android.gui.input;

import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.viewmodel.UiTaxon;
import org.openforis.collect.android.viewmodel.UiTaxonAttribute;

/**
 * @author Daniel Wiell
 */
public class TaxonAttributeComponent extends AttributeComponent<UiTaxonAttribute> {
    private final AutoCompleteTextView autoComplete;
    private UiTaxon selectedTaxon;

    protected TaxonAttributeComponent(UiTaxonAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
        autoComplete = new AutoCompleteTextView(context);
        autoComplete.setThreshold(1);
        autoComplete.setSingleLine();
        if (attribute.getTaxon() != null) {
            setText(attribute.getTaxon().toString());
            selectedTaxon = attribute.getTaxon();
        }
        autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedTaxon = (UiTaxon) autoComplete.getAdapter().getItem(position);
                updateAttributeIfChanged();
            }
        });
        autoComplete.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTaxon = (UiTaxon) autoComplete.getAdapter().getItem(position);
            }

            public void onNothingSelected(AdapterView<?> parent) {
                selectedTaxon = null;
            }
        });
        autoComplete.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    updateAttributeIfChanged();
                return false;
            }
        });
        autoComplete.setAdapter(new UiTaxonAdapter(context, attribute, ServiceLocator.taxonService()));

    }

    protected boolean updateAttributeIfChanged() {
        UiTaxon newTaxon = selectedTaxon();
        if (hasChanged(newTaxon)) {
            attribute.setTaxon(selectedTaxon);
            notifyAboutAttributeChange();
            return true;
        }
        return false;
    }

    protected View toInputView() {
        return autoComplete;
    }


    private UiTaxon selectedTaxon() {
        Editable editable = autoComplete.getText();
        if (editable == null)
            throw new IllegalStateException("autoComplete text is null");
        String text = editable.toString();
        if (selectedTaxon == null || !selectedTaxon.toString().equals(text)) {
            if (StringUtils.isEmpty(text))
                selectedTaxon = null;
//            else // TODO: Support entering taxon code directly, without selecting something?
//                selectedTaxon = uiTaxonByValue.get(text.trim());
        }
        if (selectedTaxon == null) {
            setText("");
            return null;
        }
        setText(selectedTaxon.toString());
        return selectedTaxon;
    }

    public View getDefaultFocusedView() {
        return autoComplete;
    }

    private boolean hasChanged(UiTaxon newTaxon) {
        UiTaxon oldTaxon = attribute.getTaxon();
        if (oldTaxon == null)
            return newTaxon != null;
        return !oldTaxon.equals(newTaxon);
    }

    @SuppressWarnings("ConstantConditions")
    private void setText(String text) {
        // Hack to prevent pop-up from opening when setting text
        // http://www.grokkingandroid.com/how-androids-autocompletetextview-nearly-drove-me-nuts/
        UiTaxonAdapter adapter = (UiTaxonAdapter) autoComplete.getAdapter();
        autoComplete.setAdapter(null);
        autoComplete.setText(text);
        autoComplete.setAdapter(adapter);
    }

}
