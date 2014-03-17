package org.openforis.collect.android.gui.util.meter;


import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * @author Daniel Wiell
 */
public class Timer {
    private static final Logger LOG = Logger.getLogger(Timer.class.getName());
    public static boolean enabled = true;

    public static void time(Class c, String name, Runnable action) {
        long start = System.currentTimeMillis();
        try {
            action.run();
        } finally {
            log(c, name, start);
        }
    }

    public static <T> T time(Class c, String name, Callable<T> action) {
        long start = System.currentTimeMillis();
        try {
            return action.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            log(c, name, start);
        }
    }

    private static void log(Class c, String name, long start) {
        if (enabled)
            LOG.info(c.getSimpleName() + "." + name + ": " + (System.currentTimeMillis() - start) + " millis");
    }
}
