package org.openforis.collect.android.util.persistence;

import java.util.Arrays;
import java.util.List;

/**
 * @author Daniel Wiell
 */
public final class SchemaChange {
    private final List<String> statements;

    public SchemaChange(String... statements) {
        this.statements = Arrays.asList(statements);
    }

    public List<String> statements() {
        return statements;
    }
}
