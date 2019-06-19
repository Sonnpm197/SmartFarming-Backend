package com.son.CapstoneProject.entity;

/**
 * This class is for get class type in Hibernate Search
 * @param <T>
 */
public class GenericClass<T> {

    private final Class<T> type;

    public GenericClass(Class<T> type) {
        this.type = type;
    }

    public Class<T> getMyType() {
        return this.type;
    }
}