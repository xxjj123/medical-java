package com.yinhai.mids.common.exception;

import com.yinhai.ta404.core.exception.AppException;

import java.util.Map;
import java.util.function.Supplier;

/**
 * @author zhuhs
 * @date 2023/11/15 11:50
 */
public class AppAssert {

    private static final Assertable<AppException> ASSERT = ExceptionEnum.APP;

    public static <X extends RuntimeException> void isTrue(boolean expression, Supplier<? extends X> supplier) {
        ASSERT.isTrue(expression, supplier);
    }

    public static void isTrue(boolean expression, String errorMsgTemplate, Object... params) {
        ASSERT.isTrue(expression, errorMsgTemplate, params);
    }

    public static void isTrue(boolean expression) {
        ASSERT.isTrue(expression);
    }

    public static <X extends RuntimeException> void isFalse(boolean expression, Supplier<X> errorSupplier) {
        ASSERT.isFalse(expression, errorSupplier);
    }

    public static void isFalse(boolean expression, String errorMsgTemplate, Object... params) {
        ASSERT.isFalse(expression, errorMsgTemplate, params);
    }

    public static void isFalse(boolean expression) {
        ASSERT.isFalse(expression);
    }

    public static <X extends RuntimeException> void isNull(Object object, Supplier<X> errorSupplier) {
        ASSERT.isNull(object, errorSupplier);
    }

    public static void isNull(Object object, String errorMsgTemplate, Object... params) {
        ASSERT.isNull(object, errorMsgTemplate, params);
    }

    public static void isNull(Object object) {
        ASSERT.isNull(object);
    }

    public static <T, X extends RuntimeException> T notNull(T object, Supplier<X> errorSupplier) {
        return ASSERT.notNull(object, errorSupplier);
    }

    public static <T> T notNull(T object, String errorMsgTemplate, Object... params) {
        return ASSERT.notNull(object, errorMsgTemplate, params);
    }

    public static <T> T notNull(T object) {
        return ASSERT.notNull(object);
    }

    public static <T extends CharSequence, X extends RuntimeException> T notEmpty(T text, Supplier<X> errorSupplier) {
        return ASSERT.notEmpty(text, errorSupplier);
    }

    public static <T extends CharSequence> T notEmpty(T text, String errorMsgTemplate, Object... params) {
        return ASSERT.notEmpty(text, errorMsgTemplate, params);
    }

    public static <T extends CharSequence> T notEmpty(T text) {
        return ASSERT.notEmpty(text);
    }

    public static <T extends CharSequence, X extends RuntimeException> T notBlank(T text, Supplier<X> errorMsgSupplier) {
        return ASSERT.notBlank(text, errorMsgSupplier);
    }

    public static <T extends CharSequence> T notBlank(T text, String errorMsgTemplate, Object... params) {
        return ASSERT.notBlank(text, errorMsgTemplate, params);
    }

    public static <T extends CharSequence> T notBlank(T text) {
        return ASSERT.notBlank(text);
    }

    public static <T extends CharSequence, X extends RuntimeException> T notContain(CharSequence textToSearch, T substring, Supplier<X> errorSupplier) {
        return ASSERT.notContain(textToSearch, substring, errorSupplier);
    }

    public static String notContain(String textToSearch, String substring, String errorMsgTemplate, Object... params) {
        return ASSERT.notContain(textToSearch, substring, errorMsgTemplate, params);
    }

    public static String notContain(String textToSearch, String substring) {
        return ASSERT.notContain(textToSearch, substring);
    }

    public static <T, X extends RuntimeException> T[] notEmpty(T[] array, Supplier<X> errorSupplier) {
        return ASSERT.notEmpty(array, errorSupplier);
    }

    public static <T> T[] notEmpty(T[] array, String errorMsgTemplate, Object... params) {
        return ASSERT.notEmpty(array, errorMsgTemplate, params);
    }

    public static <T> T[] notEmpty(T[] array) {
        return ASSERT.notEmpty(array);
    }

    public static <E, T extends Iterable<E>, X extends RuntimeException> T notEmpty(T collection, Supplier<X> errorSupplier) {
        return ASSERT.notEmpty(collection, errorSupplier);
    }

    public static <E, T extends Iterable<E>> T notEmpty(T collection, String errorMsgTemplate, Object... params) {
        return ASSERT.notEmpty(collection, errorMsgTemplate, params);
    }

    public static <E, T extends Iterable<E>> T notEmpty(T collection) {
        return ASSERT.notEmpty(collection);
    }

    public static <K, V, T extends Map<K, V>, X extends RuntimeException> T notEmpty(T map, Supplier<X> errorSupplier) {
        return ASSERT.notEmpty(map, errorSupplier);
    }

    public static <K, V, T extends Map<K, V>> T notEmpty(T map, String errorMsgTemplate, Object... params) {
        return ASSERT.notEmpty(map, errorMsgTemplate, params);
    }

    public static <K, V, T extends Map<K, V>> T notEmpty(T map) {
        return ASSERT.notEmpty(map);
    }

    public static <X extends RuntimeException> void notEquals(Object obj1, Object obj2, Supplier<X> errorSupplier) {
        ASSERT.notEquals(obj1, obj2, errorSupplier);
    }

    public static void notEquals(Object obj1, Object obj2, String errorMsgTemplate, Object... params) {
        ASSERT.notEquals(obj1, obj2, errorMsgTemplate, params);
    }

    public static void notEquals(Object obj1, Object obj2) {
        ASSERT.notEquals(obj1, obj2);
    }

    public static <X extends RuntimeException> void equals(Object obj1, Object obj2, Supplier<X> errorSupplier) {
        ASSERT.equals(obj1, obj2, errorSupplier);
    }

    public static void equals(Object obj1, Object obj2, String errorMsgTemplate, Object... params) {
        ASSERT.equals(obj1, obj2, errorMsgTemplate, params);
    }

    public static void equals(Object obj1, Object obj2) {
        ASSERT.equals(obj1, obj2);
    }
}
