package org.openforis.collect.android;

import org.openforis.collect.android.viewmodel.*;

import java.util.List;

/**
 * @author Daniel Wiell
 */
public interface CodeListService {
    UiCodeList codeList(UiCodeAttribute attribute);

    UiCodeList codeList(UiAttributeCollection uiAttributeCollection);

    boolean isParentCodeAttribute(UiAttribute attribute, UiCodeAttribute codeAttribute);

    int getMaxCodeListSize(UiCodeAttribute attribute);

    int getMaxCodeListSize(Definition definition);
}
