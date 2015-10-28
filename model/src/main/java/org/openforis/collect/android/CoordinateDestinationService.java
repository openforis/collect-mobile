package org.openforis.collect.android;

import org.openforis.collect.android.viewmodel.*;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;

/**
 * @author Daniel Wiell
 */
public interface CoordinateDestinationService {
    double[] destination(UiCoordinateAttribute uiAttribute);

    public ValidationResultFlag validateDistance(UiCoordinateAttribute uiAttribute);
}
