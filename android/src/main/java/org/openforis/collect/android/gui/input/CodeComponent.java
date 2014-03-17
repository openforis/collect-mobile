package org.openforis.collect.android.gui.input;

import android.content.Context;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiCode;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Wiell
 */
class CodeComponent extends InputComponent<UiCodeAttribute> {
    private static final int RADIO_GROUP_MAX_SIZE = 20;
    private final ViewGroup view;
    private final CodeListService codeListService;
    private CodeList codeList;

    CodeComponent(UiCodeAttribute attribute, Context context) {
        super(attribute, context);
        codeListService = ServiceLocator.codeListService();
        this.codeList = createCodeList();
        this.view = toView(codeList);
    }

    private ViewGroup toView(CodeList codeList) {
        ViewGroup input = new ScrollView(context());
        input.addView(codeList.getView());
        return input;
    }

    public View getView() {
        return view;
    }

    public void updateAttribute() {
        UiCode newCode = codeList.selectedCode();
        if (hasChanged(newCode)) {
            attribute().setCode(newCode);
            notifyAboutAttributeChange();
        }
    }

    public void onAttributeChange(UiAttribute attribute) {
        if (attribute() != attribute && codeListService.isParentCodeAttribute(attribute, attribute()))
            refreshCodeList();
    }

    public View getDefaultFocusedView() {
        return codeList.getView();
    }

    private void refreshCodeList() {
        view.removeView(codeList.getView());
        codeList = createCodeList();
        view.addView(codeList.getView());
        notifyAboutAttributeChange(); // TODO: There might be other code attributes dependent on us. But this saves and validate the value. OK?
    }


    private CodeList createCodeList() {
        int maxCodeListSize = codeListService.getMaxCodeListSize(attribute());
        if (maxCodeListSize <= RADIO_GROUP_MAX_SIZE)
            return new CodeRadioGroupList();
        return new CodeAutoCompleteList();
    }

    private boolean isSelectedCode(UiCode code) {
        return code.equals(attribute().getCode());
    }

    private boolean hasChanged(UiCode newCode) {
        UiCode oldCode = attribute().getCode();
        if (oldCode == null)
            return newCode != null;
        return !oldCode.equals(newCode);
    }

    public String toString() {
        return attribute().toString();
    }

    private interface CodeList {
        View getView();

        UiCode selectedCode();
    }

    private class CodeAutoCompleteList implements CodeList {
        private final AutoCompleteTextView autoComplete;
        private List<UiCode> codes;
        private Map<String, UiCode> uiCodeByValue = new HashMap<String, UiCode>();
        private UiCode selectedCode;

        CodeAutoCompleteList() {
            autoComplete = new AutoCompleteTextView(context());
            autoComplete.setThreshold(1);
            autoComplete.setSingleLine();
            if (attribute().getCode() != null) {
                setText(attribute().getCode().toString());
                selectedCode = attribute().getCode();
            }
            autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectedCode = (UiCode) autoComplete.getAdapter().getItem(position);
                    updateAttribute();
                }
            });
            autoComplete.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedCode = (UiCode) autoComplete.getAdapter().getItem(position);
                }

                public void onNothingSelected(AdapterView<?> parent) {
                    selectedCode = null;
                }
            });
            autoComplete.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE)
                        updateAttribute();
                    return false;
                }
            });
            asyncInitOptions(autoComplete);
        }

        public View getView() {
            return autoComplete;
        }

        public UiCode selectedCode() {
            String text = autoComplete.getText().toString();
            if (selectedCode == null || !selectedCode.toString().equals(text)) {
                if (StringUtils.isEmpty(text))
                    selectedCode = null;
                else
                    selectedCode = uiCodeByValue.get(text.trim());
            }
            if (selectedCode == null) {
                setText("");
                return null;
            }
            setText(selectedCode.toString());
            return selectedCode;
        }

        private void setText(String text) {
            // Hack to prevent pop-up from opening when setting text
            // http://www.grokkingandroid.com/how-androids-autocompletetextview-nearly-drove-me-nuts/
            UiCodeAdapter adapter = (UiCodeAdapter) autoComplete.getAdapter();
            autoComplete.setAdapter(null);
            autoComplete.setText(text);
            autoComplete.setAdapter(adapter);
        }

        private void asyncInitOptions(final AutoCompleteTextView autoComplete) {
            Runnable runnable = new Runnable() {
                public void run() {
                    codes = codeListService.codeList(attribute());
                    for (UiCode code : codes)
                        uiCodeByValue.put(code.getValue(), code);
                    setAdapter(autoComplete);
                }
            };
            new Thread(runnable).start();
        }

        private void setAdapter(final AutoCompleteTextView autoComplete) {
            autoComplete.post(new Runnable() {
                public void run() {
                    autoComplete.setAdapter(new UiCodeAdapter(context(), codes));
                }
            });
        }
    }

    private class CodeRadioGroupList implements CodeList {
        private final SparseArray<UiCode> codeByViewId = new SparseArray<UiCode>();
        private final RadioGroup radioGroup;

        CodeRadioGroupList() {
            radioGroup = new RadioGroup(context());
            asyncInitOptions();
        }

        public View getView() {
            return radioGroup;
        }

        public UiCode selectedCode() {
            int viewId = radioGroup.getCheckedRadioButtonId();
            return codeByViewId.get(viewId);
        }

        private void asyncInitOptions() {
            Runnable runnable = new Runnable() {
                public void run() {
                    List<UiCode> codes = codeListService.codeList(attribute());
                    addRadioButtons(codes, radioGroup);
                }
            };
            new Thread(runnable).start();
        }

        private void addRadioButtons(final List<UiCode> codes, final RadioGroup radioGroup) {
            radioGroup.post(new Runnable() {
                public void run() {
                    Integer selectedViewId = null;
                    for (UiCode code : codes) {
                        RadioButton radioButton = new RadioButton(context());
                        radioButton.setText(code.toString());
                        radioGroup.addView(radioButton);
                        codeByViewId.put(radioButton.getId(), code);
                        if (isSelectedCode(code))
                            selectedViewId = radioButton.getId();
                    }
                    if (selectedViewId != null)
                        radioGroup.check(selectedViewId);

                }
            });
        }
    }
}
