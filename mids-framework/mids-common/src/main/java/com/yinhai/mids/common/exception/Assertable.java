package com.yinhai.mids.common.exception;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author zhuhs
 * @date 2023/11/14 16:50
 */
interface Assertable<EX extends RuntimeException> {

    EX newException();

    EX newException(String msg);

    default EX newException(String errorMsgTemplate, Object... params) {
        return newException(MsgFormatter.format(errorMsgTemplate, params));
    }

    default <X extends RuntimeException> void isTrue(boolean expression, Supplier<? extends X> supplier) {
        if (!expression) {
            throw supplier.get();
        }
    }

    default void isTrue(boolean expression, String errorMsgTemplate, Object... params) {
        isTrue(expression, () -> newException(errorMsgTemplate, params));
    }

    default void isTrue(boolean expression) {
        isTrue(expression, this::newException);
    }

    default <X extends RuntimeException> void isFalse(boolean expression, Supplier<X> errorSupplier) {
        if (expression) {
            throw errorSupplier.get();
        }
    }

    default void isFalse(boolean expression, String errorMsgTemplate, Object... params) {
        isFalse(expression, () -> newException(errorMsgTemplate, params));
    }

    default void isFalse(boolean expression) {
        isFalse(expression, this::newException);
    }

    default <X extends RuntimeException> void isNull(Object object, Supplier<X> errorSupplier) {
        if (object != null) {
            throw errorSupplier.get();
        }
    }

    default void isNull(Object object, String errorMsgTemplate, Object... params) {
        isNull(object, () -> newException(errorMsgTemplate, params));
    }

    default void isNull(Object object) {
        isNull(object, this::newException);
    }

    default <T, X extends RuntimeException> T notNull(T object, Supplier<X> errorSupplier) {
        if (object == null) {
            throw errorSupplier.get();
        }
        return object;
    }

    default <T> T notNull(T object, String errorMsgTemplate, Object... params) {
        return notNull(object, () -> newException(errorMsgTemplate, params));
    }

    default <T> T notNull(T object) {
        return notNull(object, this::newException);
    }

    default <T extends CharSequence, X extends RuntimeException> T notEmpty(T text, Supplier<X> errorSupplier) {
        if (text == null || text.length() == 0) {
            throw errorSupplier.get();
        }
        return text;
    }

    default <T extends CharSequence> T notEmpty(T text, String errorMsgTemplate, Object... params) {
        return notEmpty(text, () -> newException(errorMsgTemplate, params));
    }

    default <T extends CharSequence> T notEmpty(T text) {
        return notEmpty(text, this::newException);
    }

    default <T extends CharSequence, X extends RuntimeException> T notBlank(T text, Supplier<X> errorMsgSupplier) {
        if (MsgFormatter.isBlank(text)) {
            throw errorMsgSupplier.get();
        }
        return text;
    }

    default <T extends CharSequence> T notBlank(T text, String errorMsgTemplate, Object... params) {
        return notBlank(text, () -> newException(errorMsgTemplate, params));
    }

    default <T extends CharSequence> T notBlank(T text) {
        return notBlank(text, this::newException);
    }

    default <T extends CharSequence, X extends RuntimeException> T notContain(CharSequence textToSearch, T substring, Supplier<X> errorSupplier) {
        if (textToSearch == null || substring == null || !textToSearch.toString().contains(substring)) {
            throw errorSupplier.get();
        }
        return substring;
    }

    default String notContain(String textToSearch, String substring, String errorMsgTemplate, Object... params) {
        return notContain(textToSearch, substring, () -> newException(errorMsgTemplate, params));
    }

    default String notContain(String textToSearch, String substring) {
        return notContain(textToSearch, substring, this::newException);
    }

    default <T, X extends RuntimeException> T[] notEmpty(T[] array, Supplier<X> errorSupplier) {
        if (array == null || array.length == 0) {
            throw errorSupplier.get();
        }
        return array;
    }

    default <T> T[] notEmpty(T[] array, String errorMsgTemplate, Object... params) {
        return notEmpty(array, () -> newException(errorMsgTemplate, params));
    }

    default <T> T[] notEmpty(T[] array) {
        return notEmpty(array, this::newException);
    }

    default <E, T extends Iterable<E>, X extends RuntimeException> T notEmpty(T collection, Supplier<X> errorSupplier) {
        if (collection == null || !collection.iterator().hasNext()) {
            throw errorSupplier.get();
        }
        return collection;
    }

    default <E, T extends Iterable<E>> T notEmpty(T collection, String errorMsgTemplate, Object... params) {
        return notEmpty(collection, () -> newException(errorMsgTemplate, params));
    }

    default <E, T extends Iterable<E>> T notEmpty(T collection) {
        return notEmpty(collection, this::newException);
    }

    default <K, V, T extends Map<K, V>, X extends RuntimeException> T notEmpty(T map, Supplier<X> errorSupplier) {
        if (map == null || map.isEmpty()) {
            throw errorSupplier.get();
        }
        return map;
    }

    default <K, V, T extends Map<K, V>> T notEmpty(T map, String errorMsgTemplate, Object... params) {
        return notEmpty(map, () -> newException(errorMsgTemplate, params));
    }

    default <K, V, T extends Map<K, V>> T notEmpty(T map) {
        return notEmpty(map, this::newException);
    }

    default <X extends RuntimeException> void notEquals(Object obj1, Object obj2, Supplier<X> errorSupplier) {
        if (Objects.equals(obj1, obj2)) {
            throw errorSupplier.get();
        }
    }

    default void notEquals(Object obj1, Object obj2, String errorMsgTemplate, Object... params) {
        notEquals(obj1, obj2, () -> newException(errorMsgTemplate, params));
    }

    default void notEquals(Object obj1, Object obj2) {
        notEquals(obj1, obj2, this::newException);
    }

    default <X extends RuntimeException> void equals(Object obj1, Object obj2, Supplier<X> errorSupplier) {
        if (!Objects.equals(obj1, obj2)) {
            throw errorSupplier.get();
        }
    }

    default void equals(Object obj1, Object obj2, String errorMsgTemplate, Object... params) {
        equals(obj1, obj2, () -> newException(errorMsgTemplate, params));
    }

    default void equals(Object obj1, Object obj2) {
        equals(obj1, obj2, this::newException);
    }

    class MsgFormatter {
        private static final char BACKSLASH = '\\';

        private static final String EMPTY_JSON = "{}";

        public static boolean isBlank(CharSequence str) {
            if (str == null) {
                return true;
            }
            final int length = str.length();
            if (length == 0) {
                return true;
            }
            for (int i = 0; i < length; i++) {
                char c = str.charAt(i);
                boolean isBlankChar = Character.isWhitespace(c)
                                      || Character.isSpaceChar(c)
                                      || c == '\ufeff'
                                      || c == '\u202a'
                                      || c == '\u0000'
                                      || c == '\u3164'
                                      || c == '\u2800'
                                      || c == '\u180e';
                if (!isBlankChar) {
                    return false;
                }
            }
            return true;
        }

        public static String format(String template, Object... params) {
            if (isBlank(template) || (params == null || params.length == 0)) {
                return template;
            }
            final int strPatternLength = template.length();
            final int placeHolderLength = EMPTY_JSON.length();

            final StringBuilder sb = new StringBuilder(strPatternLength + 50);

            int handledPosition = 0;
            int delimIndex;
            for (int argIndex = 0; argIndex < params.length; argIndex++) {
                delimIndex = template.indexOf(EMPTY_JSON, handledPosition);
                if (delimIndex == -1) {
                    if (handledPosition == 0) {
                        return template;
                    }
                    sb.append(template, handledPosition, strPatternLength);
                    return sb.toString();
                }

                if (delimIndex > 0 && template.charAt(delimIndex - 1) == BACKSLASH) {
                    if (delimIndex > 1 && template.charAt(delimIndex - 2) == BACKSLASH) {
                        sb.append(template, handledPosition, delimIndex - 1);
                        sb.append(str(params[argIndex]));
                        handledPosition = delimIndex + placeHolderLength;
                    } else {
                        argIndex--;
                        sb.append(template, handledPosition, delimIndex - 1);
                        sb.append(EMPTY_JSON.charAt(0));
                        handledPosition = delimIndex + 1;
                    }
                } else {
                    sb.append(template, handledPosition, delimIndex);
                    sb.append(str(params[argIndex]));
                    handledPosition = delimIndex + placeHolderLength;
                }
            }

            sb.append(template, handledPosition, strPatternLength);
            return sb.toString();
        }

        private static String str(Object obj) {
            if (obj == null) {
                return null;
            }
            if (obj instanceof String) {
                return (String) obj;
            }
            if (obj.getClass().isArray()) {
                if (obj instanceof long[]) {
                    return Arrays.toString((long[]) obj);
                }
                if (obj instanceof int[]) {
                    return Arrays.toString((int[]) obj);
                }
                if (obj instanceof short[]) {
                    return Arrays.toString((short[]) obj);
                }
                if (obj instanceof char[]) {
                    return Arrays.toString((char[]) obj);
                }
                if (obj instanceof byte[]) {
                    return Arrays.toString((byte[]) obj);
                }
                if (obj instanceof boolean[]) {
                    return Arrays.toString((boolean[]) obj);
                }
                if (obj instanceof float[]) {
                    return Arrays.toString((float[]) obj);
                }
                if (obj instanceof double[]) {
                    return Arrays.toString((double[]) obj);
                }
                return Arrays.deepToString((Object[]) obj);
            }
            return obj.toString();
        }
    }
}
