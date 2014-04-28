package org.openforis.collect.android.gui.input;

import android.support.v4.app.FragmentActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.viewmodel.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Daniel Wiell
 */
public abstract class CodeAttributeCollectionComponent extends AttributeCollectionComponent {
    protected final CodeListService codeListService;

    protected CodeAttributeCollectionComponent(UiAttributeCollection attributeCollection, CodeListService codeListService, SurveyService surveyService, FragmentActivity context) {
        super(attributeCollection, surveyService, context);
        this.codeListService = codeListService;
    }

    public static CodeAttributeCollectionComponent create(UiAttributeCollection attributeCollection, SurveyService surveyService, FragmentActivity context) {
        CodeListService codeListService = ServiceLocator.codeListService();
        int maxCodeListSize = codeListService.getMaxCodeListSize(attributeCollection.getDefinition().attributeDefinition);  // TODO: Different implementations depending on  list size
        if (maxCodeListSize <= CodeAttributeComponent.RADIO_GROUP_MAX_SIZE)
            return new CheckboxCodeAttributeCollectionComponent(attributeCollection, codeListService, surveyService, context);
        return new AutoCompleteCodeAttributeCollectionComponent(attributeCollection, codeListService, surveyService, context);
    }
}


