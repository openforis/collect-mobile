package com.innovatrics.fingeraclient;

import com.innovatrics.fingeraclient.AndroidClassScanner.ClassMatcher;
import liquibase.logging.Logger;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.servicelocator.PackageScanClassResolver;
import liquibase.servicelocator.PackageScanFilter;
import liquibase.servicelocator.ServiceLocator;

import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Liquibase performs a quite scary class scan (it scans files, jars, including subpackages) for plugins, loggers, database implementations etc.
 * This is not possible with Android. This implementation thus uses a simple approach - it receives the list of classes in a text file (see {@link AndroidClassScanner}) for details.
 *
 * @author Martin Vysny
 */
public final class AndroidClassResolver implements PackageScanClassResolver {
    private final AndroidClassScanner acs;
    private final Set<PackageScanFilter> filters = new HashSet<PackageScanFilter>();

    public AndroidClassResolver(AndroidClassScanner acs) {
        this.acs = acs;
    }

    public void setClassLoaders(Set<ClassLoader> classLoaders) {
    }

    public Set<ClassLoader> getClassLoaders() {
        return Collections.singleton(AndroidClassResolver.class.getClassLoader());
    }

    public void addClassLoader(ClassLoader classLoader) {
    }

    public Set<Class<?>> findImplementations(final Class<?> parent, final String... packageNames) {
        return acs.find(packageNames, new ClassMatcher() {

            public boolean matches(String classname) {
                return true;
            }

            public boolean matches(Class<?> clazz) {
                for (PackageScanFilter filter : filters) {
                    if (!filter.matches(clazz)) {
                        return false;
                    }
                }
                if (!parent.isAssignableFrom(clazz)) {
                    return false;
                }
                return true;
            }
        }, true);
    }

    public Set<Class<?>> findByFilter(final PackageScanFilter filter, String... packageNames) {
        return acs.find(packageNames, new ClassMatcher() {

            public boolean matches(String classname) {
                return true;
            }

            public boolean matches(Class<?> clazz) {
                for (PackageScanFilter filter : filters) {
                    if (!filter.matches(clazz)) {
                        return false;
                    }
                }
                if (!filter.matches(clazz)) {
                    return false;
                }
                return true;
            }
        }, true);
    }

    public void addFilter(PackageScanFilter filter) {
        filters.add(filter);
    }

    public void removeFilter(PackageScanFilter filter) {
        filters.remove(filter);
    }

    /**
     * Patches Liquibase in runtime - overrides class loading mechanism and configures SAXParserFactory to not to use validation.
     *
     * @param liquibaseClassList InputStream which reads the contents of the class list file. See {@link AndroidClassScanner} for details.
     * @throws IOException
     */
    public static void patchLiquibase(InputStream liquibaseClassList) throws IOException {
        patchClassLoader(liquibaseClassList);
        patchSAXParser();
    }

    private static void patchClassLoader(InputStream liquibaseClassList) throws IOException {
        // workaround for Android class-loading issues
        final InputStream in = liquibaseClassList;
        final AndroidClassScanner acs;
        try {
            acs = new AndroidClassScanner(in);
        } finally {
            if (in != null)
                in.close();
        }
        set(ServiceLocator.class, ServiceLocator.getInstance(), "classResolver", new AndroidClassResolver(acs));
        // sanity check
        Class<?>[] classes = ServiceLocator.getInstance().findClasses(Logger.class);
        if (classes.length == 0) {
            throw new RuntimeException("Liquibase ServiceLocator is not configured properly - it cannot find any classes");
        }
    }

    private static Object get(Class<?> c, Object instance, String field) {
        try {
            final Field f = c.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(instance);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void set(Class<?> c, Object instance, String field, Object value) {
        try {
            final Field f = c.getDeclaredField(field);
            f.setAccessible(true);
            f.set(instance, value);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void patchSAXParser() {
        // Android SAX Parser does not support validation
        for (ChangeLogParser clp : ChangeLogParserFactory.getInstance().getParsers()) {
            if (clp instanceof XMLChangeLogSAXParser) {
                final SAXParserFactory spf = (SAXParserFactory) get(XMLChangeLogSAXParser.class, clp, "saxParserFactory");
                spf.setValidating(false);
            }
        }
    }
}