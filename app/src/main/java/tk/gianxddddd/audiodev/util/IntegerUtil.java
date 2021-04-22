package tk.gianxddddd.audiodev.util;

import java.util.Random;

public class IntegerUtil {
    static final Random random = new Random();

    public static int getRandom(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
}
