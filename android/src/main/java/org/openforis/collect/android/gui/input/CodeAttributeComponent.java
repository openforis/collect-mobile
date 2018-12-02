package org.openforis.collect.android.gui.input;

import android.content.Context;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import static org.apache.commons.lang3.StringUtils.*;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.detail.CodeListDescriptionDialogFragment;
import org.openforis.collect.android.gui.util.Keyboard;
import org.openforis.collect.android.gui.util.Views;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiCode;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;
import org.openforis.collect.android.viewmodel.UiCodeList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static org.apache.commons.lang3.ObjectUtils.notEqual;

/**
 * @author Daniel Wiell
 */
public abstract class CodeAttributeComponent extends AttributeComponent<UiCodeAttribute> {
    static final int RADIO_GROUP_MAX_SIZE = 100;
    static final String DESCRIPTION_BUTTON_TAG = "descriptionButton";
    private UiCode parentCode;
    final CodeListService codeListService;
    final boolean enumerator;
    protected UiCodeList codeList;
    private boolean codeListRefreshForced;

    CodeAttributeComponent(UiCodeAttribute attribute, CodeListService codeListService, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
        this.codeListService = codeListService;
        this.enumerator = attribute.getDefinition().isEnumerator();
    }

    public static CodeAttributeComponent create(UiCodeAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        CodeListService codeListService = ServiceLocator.codeListService();
        int maxCodeListSize = codeListService.getMaxCodeListSize(attribute);
        boolean enumerator = attribute.getDefinition().isEnumerator();
        if (maxCodeListSize <= RADIO_GROUP_MAX_SIZE && !enumerator)
            return new NewCodeAttributeRadioComponent(attribute, codeListService, surveyService, context);
        else
            return new AutoCompleteCodeAttributeComponent(attribute, codeListService, surveyService, context);
    }

    protected UiCode selectedCode() {
        return null;
    }

    protected CodeValue selectedCode2() {
        return null;
    }

    public final void onAttributeChange(UiAttribute changedAttribute) {
        if (changedAttribute != attribute && codeListService.isParentCodeAttribute(changedAttribute, attribute)) {
            UiCode newParentCode = ((UiCodeAttribute) changedAttribute).getCode();
            if (newParentCode == parentCode) return;
            if (newParentCode == null || !newParentCode.equals(parentCode)) {
                parentCode = newParentCode;
                setCodeListRefreshForced(true);
                initOptions();
            }
        }
    }

    private synchronized boolean isCodeListRefreshForced() {
        return codeListRefreshForced;
    }

    private synchronized void setCodeListRefreshForced(boolean codeListRefreshForced) {
        this.codeListRefreshForced = codeListRefreshForced;
    }

    protected final boolean updateAttributeIfChanged() {
        if (codeList == null)
            return false;
        CodeValue newValue = selectedCode2();
        String newCode = newValue == null ? null : newValue.code;
        String newQualifier = newValue == null ? null : newValue.qualifier;

        if (hasChanged(newCode, newQualifier)) {
            attribute.setCode(codeList.getCode(newCode));
            attribute.setQualifier(newQualifier);
            return true;
        }
        return false;
    }

    private boolean containsDescription() {
        for (UiCode code : codeList.getCodes())
            if (isNotEmpty(code.getDescription()))
                return true;
        return false;
    }

    protected void initCodeList() {
        if (codeList == null || isCodeListRefreshForced()) {
            setCodeListRefreshForced(false);
            codeList = codeListService.codeList(attribute);
            uiHandler.post(new Runnable() {
                public void run() {
                    if (containsDescription())
                        includeDescriptionsButton();
                }
            });
        }
    }

    private void includeDescriptionsButton() {
        View inputView = toInputView();
        ViewGroup parent = (ViewGroup) inputView.getParent();
        if (parent == null)
            return;
        if (parent.findViewWithTag(DESCRIPTION_BUTTON_TAG) == null) {
            Button button = new AppCompatButton(context);
            button.setTextAppearance(context, android.R.style.TextAppearance_Small);
            button.setTag(DESCRIPTION_BUTTON_TAG);
            button.setLayoutParams(new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
            button.setText(context.getResources().getString(R.string.label_show_code_descriptions));
            button.setBackgroundDrawable(null);
            button.setPaintFlags(button.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CodeListDescriptionDialogFragment.show(context.getSupportFragmentManager());
                }
            });
            int linkColor = new TextView(context).getLinkTextColors().getDefaultColor();
            button.setTextColor(linkColor);
            parent.addView(button);
        }
    }

    private boolean hasChanged(String newCode, String newQualifier) {
        return notEqual(attribute.getCode(), newCode)
                || notEqual(
                trimToEmpty(attribute.getQualifier()),
                trimToEmpty(newQualifier)
        );
    }

    protected abstract void initOptions();

    protected abstract String qualifier(UiCode selectedCode);

    final boolean isAttributeCode(UiCode code) {
        return code.equals(attribute.getCode());
    }

    protected static EditText createQualifierInput(Context context, String text, final Runnable onChangeHandler) {
        final EditText editText = new AppCompatEditText(context);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    onChangeHandler.run();
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT)
                    onChangeHandler.run();
                return false;
            }
        });
        editText.setText(text);
        editText.setSingleLine();
        editText.setHint(R.string.hint_code_qualifier_specify);
        return editText;
    }

    static class CodesAdapter extends BaseAdapter {

        private UiCodeList codeList;
        private boolean valueShown;
        private Runnable changeHandler;

        private LayoutInflater inflater;
        private Map<String, CodeValue> selectedValuesByCode = new HashMap<String, CodeValue>();
        private boolean singleSelection = true;

        CodesAdapter(Context applicationContext, Set<CodeValue> selectedCodes, UiCodeList codeList,
                     boolean valueShown, Runnable changeHandler) {
            this.codeList = codeList;
            this.valueShown = valueShown;
            this.changeHandler = changeHandler;

            inflater = (LayoutInflater.from(applicationContext));

            for (CodeValue value : selectedCodes) {
                selectedValuesByCode.put(value.code, value);
            }
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
                    selectedValuesByCode.put(uiCode.getValue(), new CodeValue(uiCode.getValue()));
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
            CodeValue value = selectedValuesByCode.get(uiCode.getValue());
            value.qualifier = text;
            this.changeHandler.run();
        }

        public Collection<CodeValue> getSelectedCodes() {
            return selectedValuesByCode.values();
        }

        private void fillView(final ViewHolder viewHolder, int index) {
            UiCode uiCode = codeList.getCodes().get(index);

            CodeValue value = selectedValuesByCode.get(uiCode.getValue());
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

    public static class CodeValue {
        public String code;
        public String qualifier;

        CodeValue(String code) {
            this(code, null);
        }

        CodeValue(String code, String qualifier) {
            this.code = code;
            this.qualifier = qualifier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CodeValue codeValue = (CodeValue) o;

            return code.equals(codeValue.code);
        }

        @Override
        public int hashCode() {
            return code.hashCode();
        }
    }
}


