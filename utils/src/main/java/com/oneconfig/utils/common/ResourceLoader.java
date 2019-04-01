package com.oneconfig.utils.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.io.IOUtils;

// https://stackoverflow.com/questions/15749192/how-do-i-load-a-file-from-resource-folder
public class ResourceLoader {
    public static final String ENV_PREFIX = "env:";

    // /**
    // * Resolves the path that can be - relative to the Class Path - Absolute - EnvVar (format env:ENV_VAR) to a path to an
    // * existing resource, or returns null
    // *
    // * @param path Path to resolve
    // * @return Resolved path.
    // */
    // public String resolvePath(String path) {

    // }

    /**
     * Load all resources with a given name, potentially aggregating all results from the searched classloaders. If no
     * results are found, the resource name is prepended by '/' and tried again.
     *
     * This method will try to load the resources using the following methods (in order):
     * <ul>
     * <li>From Thread.currentThread().getContextClassLoader()
     * <li>From ClassLoaderUtil.class.getClassLoader()
     * <li>callingClass.getClassLoader()
     * </ul>
     *
     * @param resourceName The name of the resources to load
     * @param callingClass The Class object of the calling object
     */
    public static Iterator<URL> getResources(String resourceName, Class<?> callingClass, boolean aggregate) throws IOException {
        AggregateIterator<URL> iterator = new AggregateIterator<URL>();
        iterator.addEnumeration(Thread.currentThread().getContextClassLoader().getResources(resourceName));


        if (!iterator.hasNext() || aggregate) {
            iterator.addEnumeration(ResourceLoader.class.getClassLoader().getResources(resourceName));
        }

        if (!iterator.hasNext() || aggregate) {
            ClassLoader cl = callingClass.getClassLoader();

            if (cl != null) {
                iterator.addEnumeration(cl.getResources(resourceName));
            }
        }

        if (!iterator.hasNext() && (resourceName != null) && ((resourceName.length() == 0) || (resourceName.charAt(0) != '/'))) {
            return getResources('/' + resourceName, callingClass, aggregate);
        }

        return iterator;
    }

    public static String urlToAbsolutePath(URL url) {
        try {
            File f = new File(url.toURI());
            return f.getAbsolutePath();
        }
        catch (Exception ex) {
            throw new RuntimeException(String.format("URL '%s' can't be converted to the absolute path", url.toString()), ex);
        }
    }

    public static URL getResource(String resourceName) {
        return getResource(resourceName, ResourceLoader.class);
    }

    /**
     * Load a given resource.
     *
     * This method will try to load the resource using the following methods (in order):
     * <ul>
     * <li>From Thread.currentThread().getContextClassLoader()
     * <li>From ClassLoaderUtil.class.getClassLoader()
     * <li>callingClass.getClassLoader()
     * </ul>
     *
     * @param resourceName The name IllegalStateException("Unable to call ")of the resource to load
     * @param callingClass The Class object of the calling object
     */
    public static URL getResource(String resourceName, Class<?> callingClass) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(resourceName);

        if (url == null) {
            url = ResourceLoader.class.getClassLoader().getResource(resourceName);
        }

        if (url == null) {
            ClassLoader cl = callingClass.getClassLoader();

            if (cl != null) {
                url = cl.getResource(resourceName);
            }
        }

        if ((url == null) && (resourceName != null) && ((resourceName.length() == 0) || (resourceName.charAt(0) != '/'))) {
            return getResource('/' + resourceName, callingClass);
        }

        return url;
    }

    public static String getResourcePath(String resourceName) {
        URL resourceUrl = getResource(resourceName);
        return urlToAbsolutePath(resourceUrl);
    }

    /**
     * This is a convenience method to load a resource as a stream.
     *
     * The algorithm used to find the resource is given in getResource()
     *
     * @param resourceName The name of the resource to load
     * @param callingClass The Class object of the calling object
     */
    public static InputStream getResourceAsStream(String resourceName, Class<?> callingClass) {
        if (resourceName.startsWith(ENV_PREFIX)) {
            String envVar = resourceName.substring(ENV_PREFIX.length(), resourceName.length());
            String tempResName = System.getenv(envVar);
            if (tempResName == null) {
                String msg = String.format(
                    "Error resolving the resource name '%s'. Make sure that environment variable '%s' is defined",
                    resourceName,
                    envVar
                );
                throw new RuntimeException(msg);
            }
            resourceName = tempResName;
        }
        try {
            return new FileInputStream(new File(resourceName)); // first, try to get the resource as it is
        }
        catch (FileNotFoundException ex) {
            URL url = getResource(resourceName, callingClass); // if resourceName is not pointing to resource literally (for example, the relative
                                                              // path is used), try discover it
            if (url == null) {
                throw new RuntimeException(String.format("Error finding resource '%s'", resourceName), ex);
            }
            try {
                return url.openStream();
            }
            catch (IOException ex2) {
                throw new RuntimeException(String.format("Error loading resource '%s'", resourceName), ex2);
            }
        }
    }

    public static InputStream getResourceAsStream(String resourceName) {
        return getResourceAsStream(resourceName, ResourceLoader.class);
    }

    /**
     * This is a convenience method to load a resource as a string.
     *
     * The algorithm used to find the resource is given in getResource()
     *
     * @param resourceName The name of the resource to load
     * @param callingClass The Class object of the calling object
     */
    public static String getResourceAsString(String resourceName, Class<?> callingClass) {
        try {
            return IOUtils.toString(getResourceAsStream(resourceName, callingClass));
        }
        catch (IOException ex) {
            throw new RuntimeException(String.format("Error loading resource '%s'", resourceName), ex);
        }
    }

    public static String getResourceAsString(String resourceName) {
        return getResourceAsString(resourceName, ResourceLoader.class);
    }

    /**
     * Load a class with a given name.
     *
     * It will try to load the class in the following order:
     * <ul>
     * <li>From Thread.currentThread().getContextClassLoader()
     * <li>Using the basic Class.forName()
     * <li>From ClassLoaderUtil.class.getClassLoader()
     * <li>From the callingClass.getClassLoader()
     * </ul>
     *
     * @param className The name of the class to load
     * @param callingClass The Class object of the calling object
     * @throws ClassNotFoundException If the class cannot be found anywhere.
     */
    public static Class<?> loadClass(String className, Class<?> callingClass) throws ClassNotFoundException {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        }
        catch (ClassNotFoundException e) {
            try {
                return Class.forName(className);
            }
            catch (ClassNotFoundException ex) {
                try {
                    return ResourceLoader.class.getClassLoader().loadClass(className);
                }
                catch (ClassNotFoundException exc) {
                    return callingClass.getClassLoader().loadClass(className);
                }
            }
        }
    }

    public static String getCurrThreadPath() {
        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        // in case the runtime env is windows, the path is /<disk>:/...
        // in order for Paths.get and other path utils to work properly, the
        // leading '/' has to be removed
        if (path.charAt(2) == ':') {
            path = path.substring(1);
        }
        return path;
    }

    /**
     * Aggregates Enumeration instances into one iterator and filters out duplicates. Always keeps one ahead of the
     * enumerator to protect against returning duplicates.
     */
    private static class AggregateIterator<E> implements Iterator<E> {

        LinkedList<Enumeration<E>> enums = new LinkedList<Enumeration<E>>();
        Enumeration<E> cur = null;
        E next = null;
        Set<E> loaded = new HashSet<E>();

        public AggregateIterator<E> addEnumeration(Enumeration<E> e) {
            if (e.hasMoreElements()) {
                if (cur == null) {
                    cur = e;
                    next = e.nextElement();
                    loaded.add(next);
                } else {
                    enums.add(e);
                }
            }
            return this;
        }

        public boolean hasNext() {
            return (next != null);
        }

        public E next() {
            if (next != null) {
                E prev = next;
                next = loadNext();
                return prev;
            } else {
                throw new NoSuchElementException();
            }
        }

        private Enumeration<E> determineCurrentEnumeration() {
            if (cur != null && !cur.hasMoreElements()) {
                if (enums.size() > 0) {
                    cur = enums.removeLast();
                } else {
                    cur = null;
                }
            }
            return cur;
        }

        private E loadNext() {
            if (determineCurrentEnumeration() != null) {
                E tmp = cur.nextElement();
                int loadedSize = loaded.size();
                while (loaded.contains(tmp)) {
                    tmp = loadNext();
                    if (tmp == null || loaded.size() > loadedSize) {
                        break;
                    }
                }
                if (tmp != null) {
                    loaded.add(tmp);
                }
                return tmp;
            }
            return null;

        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
