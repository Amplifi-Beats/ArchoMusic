package com.gianxd.audiodev.util;

import java.util.Random;

public class IntegerUtil {

    public static int getRandom(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

}
