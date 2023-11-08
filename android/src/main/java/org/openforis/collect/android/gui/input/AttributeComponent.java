package org.openforis.collect.android.gui.input;

import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.util.Tasks;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiNodeChange;
import org.openforis.collect.android.viewmodel.UiRecord;
import org.openforis.collect.android.viewmodel.UiValidationError;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * @author Daniel Wiell
 */
public abstract class AttributeComponent<T extends UiAttribute> extends SavableComponent {

    private static final int SAVE_NODE_DELAY = 500;

    protected final T attribute;
    private Handler delayedSaveNodeHandler;

    protected AttributeComponent(T attribute,
                                 SurveyService surveyService,
                                 FragmentActivity context) {
        super(surveyService, context);
        this.attribute = attribute;
    }

    protected TextView errorMessageContainerView() {
        return (TextView) ((ViewGroup) toInputView().getParent().getParent()).findViewById(R.id.node_label); // TODO: Ugly!!!
    }

    protected final void setValidationError(final Set<UiValidationError> validationErrors) {
        if (!attribute.isRelevant()) // Only show validation errors for relevant components
            return;
        uiHandler.post(new Runnable() {
            public void run() {
                if (!isSelected())
                    return;
                TextView labelView = errorMessageContainerView();
                if (validationErrors == null || validationErrors.isEmpty()) {
                    labelView.setError(null);
                    return;
                }
                // TODO: Move functionality to UiValidationError class}
                StringBuilder message = new StringBuilder();
                for (Iterator<UiValidationError> iterator = validationErrors.iterator(); iterator.hasNext(); ) {
                    UiValidationError validationError = iterator.next();
                    message.append(validationError);  // TODO: Only include messages from the worst level?
                    if (iterator.hasNext())
                        message.append('\n');
                    focusOnMessageContainerView();
                    labelView.setError(message);
                }
            }
        });
    }

    protected final void resetValidationErrors() {
        setValidationErrorMessage(null);
    }

    protected final void setValidationErrorMessage(CharSequence errorMessage) {
        errorMessageContainerView().setError(errorMessage);
    }

    /**
     * Updates the {@code UiAttribute} value if current attribute value is different from the input component value.
     *
     * @return true if attribute changed
     */
    protected abstract boolean updateAttributeIfChanged();

    protected void onAttributeChange(UiAttribute attribute) {
        if (hasChanged()) {
            Log.e("-----", "onAttributeChange: not same value!");
        }
    }

    public final void onNodeChange(UiNode node, Map<UiNode, UiNodeChange> nodeChange) {
        if (node instanceof UiAttribute)
            onAttributeChange((UiAttribute) node);
        UiNodeChange attributeChange = nodeChange.get(attribute);
        if (attributeChange != null) {
            setValidationError(attributeChange.validationErrors);
        }
    }

    protected void notifyAboutAttributeChanging() {
        surveyService.notifyAttributeChanging(attribute);
    }

    protected void notifyAboutAttributeChange() {
        surveyService.updateAttribute(attribute);
    }

    public final int getViewResource() {
        return R.layout.fragment_attribute_detail;
    }

    public final void setupView(ViewGroup view) {
        ViewGroup attributeInputContainer = (ViewGroup) view.findViewById(R.id.input_container);
        view.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        attributeInputContainer.addView(toInputView());
    }

    public final void saveNode() {
        stopDelayedSaveNodeHandler();
        CharSequence prevErrorMessage = errorMessageContainerView().getError();
        resetValidationErrors();
        if (updateAttributeIfChanged()) {
            notifyAboutAttributeChange();
        } else {
            // restore validation errors
            setValidationErrorMessage(prevErrorMessage);
        }
    }

    protected void delaySaveNode() {
        notifyAboutAttributeChanging();

        stopDelayedSaveNodeHandler();
        delayedSaveNodeHandler = Tasks.runDelayedOnUiThread(context, new Runnable() {
            public void run() {
                saveNode();
            }
        }, SAVE_NODE_DELAY);
    }

    protected void stopDelayedSaveNodeHandler() {
        if (delayedSaveNodeHandler != null) {
            delayedSaveNodeHandler.removeCallbacksAndMessages(null);
            delayedSaveNodeHandler = null;
        }
    }

    public final void validateNode() {
        Set<UiValidationError> validationErrors = attribute.getValidationErrors();
        if (validationErrors != null)
            setValidationError(validationErrors);
    }
    
    protected boolean isRecordEditLocked() {
        UiRecord record = attribute == null ? null : attribute.getUiRecord();
        return record == null ? true: record.isEditLocked();
    }

    public String toString() {
        return getClass().getSimpleName() + " for " + attribute;
    }
}
