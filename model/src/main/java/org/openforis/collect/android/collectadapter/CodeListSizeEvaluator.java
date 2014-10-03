package org.openforis.collect.android.collectadapter;

import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Daniel Wiell
 */
public class CodeListSizeEvaluator {
    private final CodeListSizeDao codeListSizeDao;
    private final Map<Integer, Integer> sizeByCodeListDefinition = new ConcurrentHashMap<Integer, Integer>();

    public CodeListSizeEvaluator(CodeListSizeDao codeListSizeDao) {
        this.codeListSizeDao = codeListSizeDao;
    }

    public synchronized int size(CodeAttributeDefinition definition) {
        Integer size = sizeByCodeListDefinition.get(definition.getId());
        if (size == null) {
            size = determineMaxSize(definition);
            sizeByCodeListDefinition.put(definition.getId(), size);
        }
        return size;
    }

    public void reset() {
        sizeByCodeListDefinition.clear();
    }

    private int determineMaxSize(CodeAttributeDefinition definition) {
        CodeList list = definition.getList();
        if (list.isExternal())
            return loadExternalListMaxSize(definition);
        if (!hasItems(definition))
            return loadMaxCodeListSize(definition);
        if (hasParentCodeDefinition(definition))
            return maxChildListSize(definition.getParentCodeAttributeDefinition());
        return loadMaxCodeListSize(definition);
    }

    private int loadExternalListMaxSize(CodeAttributeDefinition definition) {
        return codeListSizeDao.externalCodeListSize(definition.getList().getId(), definition.getLevelPosition());
    }

    private int loadMaxCodeListSize(CodeAttributeDefinition definition) {
        return codeListSizeDao.codeListSize(definition.getList().getId(), definition.getLevelPosition());
    }

    private int maxChildListSize(CodeAttributeDefinition parentCodeAttributeDefinition) {
        int max = 0;
        for (CodeListItem parentItem : parentCodeAttributeDefinition.getList().getItems())
            max = Math.max(max, parentItem.getChildItems().size());
        return max;
    }

    private boolean hasItems(CodeAttributeDefinition definition) {
        return !definition.getList().getItems().isEmpty();
    }

    private boolean hasParentCodeDefinition(CodeAttributeDefinition definition) {
        return definition.getParentCodeAttributeDefinition() != null;
    }
}
