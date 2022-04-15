package com.wireless4024.voting.util;

import java.util.Arrays;
import java.util.List;

public class ArrayUtils {
    public static <T> List<T> wrap(T[] array) {
        return Arrays.asList(array);
    }
}
