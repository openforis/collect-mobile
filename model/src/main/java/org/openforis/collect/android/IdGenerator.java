package org.openforis.collect.android;

/**
 * @author Daniel Wiell
 */
public class IdGenerator {
    private static int lastId;

    public static synchronized void setLastId(int lastId) {
        IdGenerator.lastId = lastId;
    }

    public static synchronized int nextId() {
        return ++lastId;
    }
}
