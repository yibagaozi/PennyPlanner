package org.softeng.group77.pennyplanner.util;

@FunctionalInterface
public interface JsonUpdater<T> {

    T update (T data);

}
