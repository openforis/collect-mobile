package org.openforis.collect.android.collectadapter;

/**
 * @author Daniel Wiell
 */
public interface CodeListSizeDao {
    int codeListSize(int codeListId, int level);

    int externalCodeListSize(int codeListId, int level);
}
