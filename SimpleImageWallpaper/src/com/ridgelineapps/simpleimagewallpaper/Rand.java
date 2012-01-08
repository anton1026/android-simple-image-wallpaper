 /*
 * Copyright (C) 2012 Android Simple Image Wallpaper (http://code.google.com/p/android-simple-image-wallpaper/)
 * 
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *   
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ridgelineapps.simpleimagewallpaper;

import java.util.Random;


public class Rand {
    public static final Random rand = new Random(System.currentTimeMillis());

    public static boolean chance(double num) {
        return (rand.nextDouble() < num);
    }

    public static boolean b() {
        return rand.nextBoolean();
    }

    public static int i(int num) {
        return rand.nextInt(num);
    }

    public static float f(float f) {
        return rand.nextFloat() * f;
    }

    public static float f(double d) {
        return rand.nextFloat() * (float) d;
    }

    public static double d(double d) {
        return rand.nextDouble() * d;
    }

    public static int rangeI(int min, int max) {
        if (max <= min) {
            return min;
        }
        return min + rand.nextInt(max - min);
    }

    public static float rangeF(float min, float max) {
        if (max <= min) {
            return min;
        }
        return min + rand.nextFloat() * (max - min);
    }

    public static double rangeD(double min, double max) {
        if (max <= min) {
            return min;
        }
        return min + rand.nextDouble() * (max - min);
    }

    public static float plusOrMinusF(float num) {
        // There is a VERY small chance that this could return the bottom bounds (ie. passing in 0.5 could
        // return -0.5, but not 0.5) Were willing to live with that rather than make the call a mess since
        // these methods are geared toward performance.
        return rand.nextFloat() * num * 2 - num;
    }

    public static float plusOrMinusF(double num) {
        // Shares the problem mentioned in note above for plusOrMinusF(float)
        return (float) (rand.nextFloat() * num * 2 - num);
    }

    public static double plusOrMinusD(double num) {
        // Shares the problem mentioned in note above for plusOrMinusF(float)
        return rand.nextDouble() * num * 2 - num;
    }

    public static int plusOrMinusI(int num) {
        // This method does not share the problem mentioned in the comment for the plusOrMinusX() calls that
        // take floats and doubles since we can do a bit of simple math to keep the number in range (and
        // because it is more important when working with int ranges that are possibly very small to be
        // accurate -- we want plusOrMinus(2) to return [-1 0 1], not [-2 -1 0 1])

        if (num < 2) {
            return 0;
        }

        return rand.nextInt(num * 2 - 1) - num + 1;
    }
}

