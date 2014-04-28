package org.openforis.collect.android;

import org.openforis.collect.android.viewmodel.*;

import java.util.List;

/**
 * @author Daniel Wiell
 */
public interface CodeListService {
    List<UiCode> codeList(UiCodeAttribute attribute);

    List<UiCode> codeList(UiAttributeCollection uiAttributeCollection);

    boolean isParentCodeAttribute(UiAttribute attribute, UiCodeAttribute codeAttribute);

    int getMaxCodeListSize(UiCodeAttribute attribute);

    int getMaxCodeListSize(Definition definition);
}
