package org.openforis.collect.android;

import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiCode;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;

import java.util.List;

/**
 * @author Daniel Wiell
 */
public interface CodeListService {
    List<UiCode> codeList(UiCodeAttribute attribute);

    boolean isParentCodeAttribute(UiAttribute attribute, UiCodeAttribute codeAttribute);

    int getMaxCodeListSize(UiCodeAttribute attribute);
}
