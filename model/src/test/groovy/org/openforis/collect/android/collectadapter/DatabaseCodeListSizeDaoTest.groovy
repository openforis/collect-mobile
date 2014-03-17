package org.openforis.collect.android.collectadapter

import org.openforis.collect.android.IdGenerator
import spock.lang.Ignore
import spock.lang.Specification

/**
 * @author Daniel Wiell
 */
class DatabaseCodeListSizeDaoTest extends Specification {
    def codeListId = IdGenerator.nextId()
    def database = new ModelTestDatabase()
    def dao = new DatabaseCodeListSizeDao(database)

    def cleanup() {
        database.reset()
    }

    def 'Can load root code list size'() {
        3.times { insertItem(null) }

        expect: dao.codeListSize(codeListId, 1) == 3
    }

    def 'Child items are not counted when loading root code list size'() {
        def ids = [] as List<Integer>
        3.times { ids << insertItem(null) }
        insertItem(ids.first())

        expect: dao.codeListSize(codeListId, 1) == 3
    }

    @Ignore
    def 'The size of provided level is returned, not the largest level overall'() {
        def ids = [] as List<Integer>
        3.times { ids << insertItem(null) }
        4.times { insertItem(ids.first()) }

        expect: dao.codeListSize(codeListId, 1) == 3
    }

    private int insertItem(Integer parentId) {
        def id = IdGenerator.nextId()
        database.sql.executeInsert("""
            INSERT INTO ofc_code_list(id, code_list_id, item_id, parent_id, sort_order, code)
            VALUES ($id, $codeListId, $id, $parentId, 0, $id)
        """)
        return id
    }


    @Ignore
    def 'Can load external code list size'() {

        when: true
        then: false
    }

}