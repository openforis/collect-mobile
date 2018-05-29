package org.openforis.collect.android.collectadapter

import groovy.xml.MarkupBuilder

/**
 * @author Daniel Wiell
 */
class IdmBuilder {
    private int lastId
    private writer = new StringWriter()
    private xml = new MarkupBuilder(writer)


    InputStream idmXmlStream(Closure closure) {
        xml.doubleQuotes = true
        xml.survey(xmlns: 'http://www.openforis.org/idml/3.0', 'xmlns:ui': 'http://www.openforis.org/collect/3.0/ui',
                lastId: Integer.MAX_VALUE, closure << {
            applicationOptions {
                options(type: 'ui') {
                    tabSet(xmlns: 'http://www.openforis.org/collect/3.0/ui', name: 'tabset') {
                        tab(name: 'tab') {
                            label('Tab')
                        }
                    }
                }
            }
        })
        xmlStream()
    }

    void language(String langCode) {
        xml.language(langCode)
    }

    void project(String name) {
        xml.project(name)
    }

    void schema(Closure closure = {}) {
        tag('schema', closure)
    }

    void entity(String name, String label, Map args, Closure body) {
        def bodyWithLabel = body << { xml.label(label) }
        tag('entity', [id: ++lastId, name: name, 'ui:tabSet': 'tabset', 'ui:tab': 'tab'] + args, bodyWithLabel)
    }

    void entity(String name, String label, Closure body = {}) {
        entity(name, label, [:], body)
    }

    void text(String name, String label, Map args = [:], Closure body = {}) {
        attribute('text', name, label, args, body)
    }

    void attribute(String type, String name, String label, Map args, Closure body = {}) {
        def bodyWithLabel = body << { xml.label(label) }
        tag(type, [id: ++lastId, name: name] + args, bodyWithLabel)
    }

    private void tag(String tag, Map args = [:], Closure body) {
        body.delegate = xml
        xml."$tag"(args, body)
        xml = body
    }

    private InputStream xmlStream() {
        new ByteArrayInputStream(writer.toString().bytes)
    }
}
