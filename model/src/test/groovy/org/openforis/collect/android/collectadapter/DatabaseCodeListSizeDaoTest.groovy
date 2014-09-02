package org.openforis.collect.android.collectadapter

import org.openforis.collect.android.IdGenerator
import spock.lang.Ignore
import spock.lang.Specification

/**
 * @author Daniel Wiell
 */
class DatabaseCodeListSizeDaoTest extends Specification {
    public static final NO_PARENT = null
    def codeListId = IdGenerator.nextId()
    def database = new ModelTestDatabase()
    def dao = new DatabaseCodeListSizeDao(database)

    def cleanup() {
        database.reset()
    }

    def 'Can load root code list size'() {
        3.times { insertItem(NO_PARENT, 1) }

        expect: dao.codeListSize(codeListId, 1) == 3
    }

    def 'Child items are not counted when loading root code list size'() {
        def ids = [] as List<Integer>
        3.times { ids << insertItem(NO_PARENT, 1) }
        insertItem(ids.first(), 2)

        expect: dao.codeListSize(codeListId, 1) == 3
    }

    def 'The size of provided level is returned, not the largest level overall'() {
        def ids = [] as List<Integer>
        3.times { ids << insertItem(NO_PARENT, 1) }
        4.times { insertItem(ids.first(), 2) }

        expect: dao.codeListSize(codeListId, 1) == 3
    }

    private int insertItem(Integer parentId, int level) {
        def id = IdGenerator.nextId()
        database.sql.executeInsert("""
            INSERT INTO ofc_code_list(id, code_list_id, item_id, parent_id, level, sort_order, code)
            VALUES ($id, $codeListId, $id, $parentId, $level, 0, $id)
        """)
        return id
    }
}