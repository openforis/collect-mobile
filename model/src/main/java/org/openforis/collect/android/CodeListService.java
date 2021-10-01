package org.openforis.collect.android;

import org.openforis.collect.android.viewmodel.*;

/**
 * @author Daniel Wiell
 */
public interface CodeListService {
    UiCodeList codeList(UiCodeAttribute attribute);

    UiCodeList codeList(UiAttributeCollection uiAttributeCollection);

    UiCode codeListItem(UiCodeAttribute attribute);

    boolean isParentCodeAttribute(UiAttribute attribute, UiCodeAttribute codeAttribute);

    boolean isParentCodeAttribute(UiAttribute attribute, UiAttributeCollection attributeCollection);

    int getMaxCodeListSize(UiCodeAttribute attribute);

    int getMaxCodeListSize(Definition definition);
}
