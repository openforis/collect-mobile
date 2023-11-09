package org.openforis.collect.android.gui.input;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.util.ClearableAutoCompleteTextView;
import org.openforis.collect.android.gui.util.Views;
import org.openforis.collect.android.viewmodel.UITaxonVernacularName;
import org.openforis.collect.android.viewmodel.UiTaxon;
import org.openforis.collect.android.viewmodel.UiTaxonAttribute;

/**
 * @author Daniel Wiell
 */
public class TaxonAttributeComponent extends AttributeComponent<UiTaxonAttribute> {
    private LinearLayout layout;
    private ClearableAutoCompleteTextView autoComplete;
    private TextView taxonReadonlyTextView;
    private TextView vernacularNameTextView;
    private UiTaxon selectedTaxon;
    private boolean textChangingNotificationEnabled = true;

    protected TaxonAttributeComponent(UiTaxonAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);

        vernacularNameTextView = new TextView(context);
        vernacularNameTextView.setPadding(8, 16, 8, 0);

        taxonReadonlyTextView = new TextView(context);
        taxonReadonlyTextView.setTextSize(20);
        taxonReadonlyTextView.setPadding(8, 16, 8, 0);

        createAutoComplete(attribute, context);

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(autoComplete);
        layout.addView(taxonReadonlyTextView);
        layout.addView(vernacularNameTextView);

        updateEditableState();
    }

    private void createAutoComplete(UiTaxonAttribute attribute, FragmentActivity context) {
        autoComplete = new ClearableAutoCompleteTextView(context);
        autoComplete.setThreshold(1);
        autoComplete.setSingleLine();
        if (attribute.getTaxon() != null) {
            selectedTaxon = attribute.getTaxon();
            updateUIBySelectedTaxon();
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
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT)
                    updateAttributeIfChanged();
                return false;
            }
        });
        autoComplete.setOnClearListener(new ClearableAutoCompleteTextView.OnClearListener() {
            public void onClear() {
                textChangingNotificationEnabled = false;
                autoComplete.setText("");
                vernacularNameTextView.setText("");
                textChangingNotificationEnabled = true;
                updateAttributeIfChanged();
            }
        });
        autoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (textChangingNotificationEnabled) {
                    notifyAboutAttributeChanging();
                }
            }
        });
        autoComplete.setAdapter(new UiTaxonAdapter(context, attribute, ServiceLocator.taxonService()));
    }

    protected TextView errorMessageContainerView() {
        return autoComplete;
    }

    @Override
    public boolean hasChanged() {
        UiTaxon oldTaxon = attribute.getTaxon();
        if (oldTaxon == null && StringUtils.isNotBlank(getText()))
            return true;
        UiTaxon newTaxon = adjustSelectedTaxon();
        return ObjectUtils.notEqual(oldTaxon, newTaxon);
    }

    protected boolean updateAttributeIfChanged() {
        adjustSelectedTaxon();
        if (hasChanged()) {
            attribute.setTaxon(selectedTaxon);
            notifyAboutAttributeChange();
            return true;
        }
        return false;
    }

    protected View toInputView() {
        return layout;
    }

    private UiTaxon adjustSelectedTaxon() {
        String text = getText();
        if (selectedTaxon == null || !selectedTaxon.toString().equals(text)) {
            if (StringUtils.isEmpty(text))
                selectedTaxon = null;
//            else // TODO: Support entering taxon code directly, without selecting something?
//                selectedTaxon = uiTaxonByValue.get(text.trim());
        }
        if (selectedTaxon == null) {
            setText("");
            vernacularNameTextView.setText("");
            return null;
        }
        updateUIBySelectedTaxon();
        return selectedTaxon;
    }

    private void updateUIBySelectedTaxon() {
        setText(selectedTaxon.toString());
        UITaxonVernacularName vernacularName = selectedTaxon.getVernacularName();
        vernacularNameTextView.setText(vernacularName == null ? "" : vernacularName.toString());
    }

    public View getDefaultFocusedView() {
        return autoComplete;
    }

    private String getText() {
        Editable editable = autoComplete.getText();
        if (editable == null)
            return null;
        return editable.toString();
    }

    @SuppressWarnings("ConstantConditions")
    private void setText(String text) {
        // Hack to prevent pop-up from opening when setting text
        // http://www.grokkingandroid.com/how-androids-autocompletetextview-nearly-drove-me-nuts/
        textChangingNotificationEnabled = false;
        UiTaxonAdapter adapter = (UiTaxonAdapter) autoComplete.getAdapter();
        autoComplete.setAdapter(null);
        autoComplete.setText(text);
        autoComplete.setAdapter(adapter);
        taxonReadonlyTextView.setText(text);
        textChangingNotificationEnabled = true;
    }

    @Override
    protected void updateEditableState() {
        boolean editable = !isRecordEditLocked();
        Views.toggleVisibility(autoComplete, editable);
        Views.toggleVisibility(taxonReadonlyTextView, !editable);
    }
}
