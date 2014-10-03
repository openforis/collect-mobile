package com.innovatrics.fingeraclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Android does not support class scanning. Therefore, we will do it the simple way: we will receive a list of classes in a simple text file.
 * This text file lists one class per line, e.g. <code>./liquibase/changelog/RanChangeSet.class</code>.
 * You can create this text file by unpacking the jar files and running <code>find .|grep class$</code>.
 * @author Martin Vysny
 *
 */
public class AndroidClassScanner {
	/**
	 * Class matcher. First, the {@link #matches(String)} is invoked to check whether a class can be filtered out by its name, to avoid unnecessary class loading.
	 * If the class passes this test, the class is loaded and {@link #matches(Class)} is invoked.
	 * @author Martin Vysny
	 */
	public static interface ClassMatcher {
		/**
		 * Checks if this class is matched.
		 * @param classname fully qualified class name, e.g. java.lang.String
		 * @return true if this class qualifies for next matching round, false otherwise.
		 */
		boolean matches(String classname);
		/**
		 * Checks if this class is matched.
		 * @param clazz the class, never null.
		 * @return true if this class matches, false otherwise.
		 */
		boolean matches(Class<?> clazz);
	}
	/**
	 * Matches all classes which implement given interface.
	 * @author Martin Vysny
	 */
	public static class InterfaceMatcher implements ClassMatcher {
		public final Class<?> intf;

		public InterfaceMatcher(Class<?> intf) {
			super();
			this.intf = intf;
		}

		public boolean matches(String classname) {
			// no way to check this from a simple class name, return true
			return true;
		}

		public boolean matches(Class<?> clazz) {
			return intf.isAssignableFrom(clazz);
		}
	}
	/**
     * Contains package contents.
     */
	private static class PackageContents {
		/**
		 * The package name, separated by a dot, e.g. java.lang
		 */
		public final String name;
		/**
		 * Simple class names, contained in this package, e.g. String
		 */
		public final Set<String> classes = new HashSet<String>();
		/**
		 * List of subpackages, gradually filled in as the text file is parsed.
		 */
		public final Set<PackageContents> subpackages = new HashSet<PackageContents>();
		public PackageContents(String name) {
			super();
			this.name = name;
		}
		@Override
		public int hashCode() {
			return name.hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PackageContents other = (PackageContents) obj;
			return name.equals(other.name);
		}
		/**
		 * Returns a list of fully qualified class names in this package (e.g. java.lang.String)
		 * @param includingSubpackages if true, subpackages are listed as well
		 * @return set of class names.
		 */
		public Set<String> listClasses(boolean includingSubpackages) {
			final Set<String> result = new HashSet<String>();
			for (String clazz: classes) {
				result.add(name + "." + clazz);
				if (includingSubpackages) {
					for (PackageContents pc: subpackages) {
						result.addAll(pc.listClasses(true));
					}
				}
			}
			return result;
		}
	}
	
	/**
	 * Contains all packages enlisted in the text file.
	 */
	private Map<String, PackageContents> classes = new HashMap<String, PackageContents>();
	
    /**
     * Creates new class scanner instance.
     * @param classList reads contents of the text file containing class list. Not null. Not closed.
     * @throws IOException on I/O error.
     */
	public AndroidClassScanner(InputStream classList) throws IOException {
		final BufferedReader r = new BufferedReader(new InputStreamReader(classList));
		while (true) {
			String line = r.readLine();
			if (line == null) {
				break;
			}
			if (line.trim().isEmpty() || !line.endsWith(".class")) {
				continue;
			}
			if (line.startsWith("./")) {
				// linuxovy find prefixuje s "./", odstranit
				line = line.substring(2);
			}
			final int lastSlash = line.lastIndexOf('/');
			String packageName;
			String className;
			if (lastSlash < 0) {
				packageName = "";
				className = line;
			} else {
				packageName = line.substring(0, lastSlash);
				className = line.substring(lastSlash + 1);
			}
			// remove trailing ".class"
			className = className.substring(0, className.length() - 6);
			packageName = packageName.replace('/', '.');
			PackageContents list = classes.get(packageName);
			if (list == null) {
				list = new PackageContents(packageName);
				classes.put(packageName, list);
				registerToSuperpackages(list);
			}
			list.classes.add(className);
		}
	}
	
	private void registerToSuperpackages(PackageContents newPackage) {
		for (int i=0; i >= 0; i = newPackage.name.indexOf('.', i + 1)) {
			final String superpackage = newPackage.name.substring(0, i);
			PackageContents sp = classes.get(superpackage);
			if (sp == null) {
				sp = new PackageContents(superpackage);
				classes.put(superpackage, sp);
			}
			sp.subpackages.add(newPackage);
		}
	}
	/**
     * Finds all classes which matches given criteria.
     * @param pkg the package to scan. If null, the <i>default</i> ("") package is used.
     * @param matcher class matcher, not null.
     * @param includeSubpackages if true, all subpackages are searched as well. If false, only given package is searched (subpackages are excluded).
     * @return a set of class matching given criteria. Never null, may be empty.
     */
	public Set<Class<?>> find(String pkg, ClassMatcher matcher, boolean includeSubpackages) {
		if(pkg==null){
			pkg ="";
		}
		pkg = pkg.replace('/', '.');
		final PackageContents classes = this.classes.get(pkg);
		if (classes == null) {
			// non existing package, pretend as there are no classes
			return Collections.emptySet();
		}
		final Set<Class<?>> result = new HashSet<Class<?>>();
		for(String fqcn: classes.listClasses(includeSubpackages)) {
			if (!matcher.matches(fqcn)) {
				continue;
			}
			try {
				final Class<?> clazz = getClass().getClassLoader().loadClass(fqcn);
				if (matcher.matches(clazz)) {
					result.add(clazz);
				}
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Failed to load class " + fqcn, e);	
			}
		}
		return result;
	}
	
	/**
     * Finds all classes which matches given criteria.
     * @param pkgs the packages to scan, must not be null. May contain null though: this is the <i>default</i> ("") package.
     * @param matcher class matcher, not null.
     * @param includeSubpackages if true, all subpackages of all packages given are searched as well. If false, only given packages is searched (subpackages are excluded).
     * @return a set of class matching given criteria. Never null, may be empty.
     */
	public Set<Class<?>> find(String[] pkgs, ClassMatcher matcher, boolean includeSubpackages) {
		final Set<Class<?>> list = new HashSet<Class<?>>();
		for (String pkg: pkgs) {
			list.addAll(find(pkg, matcher, includeSubpackages));
		}
		return list;
	}
}