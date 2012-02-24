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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;

public class Utils {
    public static final Random rand = new Random(System.currentTimeMillis());

    public static final int ALPHA_LIGHTEN = 100;
    public static final int COLOR_LIGHTEN = 100;

    public static final String[] COMMON_COLOR_KEYS = new String[] {

    "Black", "DarkGray", "LightGray", "OffWhite", "White", "Blue", };

    public static final int[][] COMMON_COLORS = new int[][] { 
        new int[] { 0, 0, 0 }, 
        new int[] { 60, 60, 60 }, 
        new int[] { 150, 150, 150 },
        new int[] { 172, 165, 136 },
        new int[] { 255, 255, 255 },
        new int[] { 40, 40, 120 },
    };

    public static int[] getCommonColorRgb(String key) {
        for (int i = 0; i < COMMON_COLOR_KEYS.length; i++) {
            if (COMMON_COLOR_KEYS[i].equals(key)) {
                return COMMON_COLORS[i];
            }
        }

        return COMMON_COLORS[0];
    }

    public static String findValue(String page, String prefix, String suffix) {
        int start = page.indexOf(prefix);
        if (start != -1) {
            start += prefix.length();
            int end = page.indexOf(suffix, start);
            if (end != -1) {
                return page.substring(start, end);
            }
        }

        return null;
    }

    public static int findValueIndexAfterText(String page, String text) {
        int start = page.indexOf(text);
        if (start != -1) {
            start += text.length();
            return start;
        }

        return -1;
    }

    public static String randValue(String page, String prefix, String suffix) {
        int start = page.indexOf(prefix);
        ArrayList<String> choices = new ArrayList<String>();
        while (start != -1) {
            start += prefix.length();
            int end = page.indexOf(suffix, start);
            if (end != -1) {
                String s = page.substring(start, end);
                choices.add(s);
                start = end;
                start = page.indexOf(prefix, start);
            } else {
                start = -1;
            }
        }

        if (choices.size() == 0) {
            return null;
        }
        return choices.get((int) (Math.random() * choices.size()));
    }

    public static Bitmap loadBitmap(Context context, Uri imageURI, int width, int height, boolean fill, boolean rotateIfNecessary) throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        InputStream is = context.getContentResolver().openInputStream(imageURI);
        Bitmap bmp = BitmapFactory.decodeStream(is, null, o);
        try {
            is.close();
        } catch (Exception e) {
            // TODO: put all in logs
            e.printStackTrace();
        }
        
        int imageWidth = o.outWidth;
        int imageHeight = o.outHeight;
        
        int longSide = Math.max(width, height);
    	int imageLongSide = Math.max(o.outWidth, o.outHeight);
        int shortSide = Math.min(width, height);
        int imageShortSide = o.outHeight;
         
        int scale=1;
        // Option 1
//        if (o.outHeight > longSide || o.outWidth > longSide) {
//            scale = (int) Math.pow(2, (int) Math.round(Math.log(longSide / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
//        }
        // Option 2
        while(true) {
        	if(rotateIfNecessary) {
	        	if(imageLongSide / 2 < longSide) {
	        		break;
	        	}
	        	
	        	if(imageShortSide / 2 < shortSide) {
	        		break;
	        	}
        	}
        	else {
	        	if(imageWidth / 2 < width) {
	        		break;
	        	}
	        	
	        	if(imageHeight / 2 < height) {
	        		break;
	        	}
        	}

        	imageLongSide /= 2;
        	imageShortSide /= 2;
        	imageWidth /= 2;
        	imageHeight /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
//        o2.inPreferQualityOverSpeed = true;
        o2.inPurgeable = true;
        o2.inInputShareable = false;

        int retries = 0;
        boolean success = false;
        while(!success) {
            try {
                // TODO: don't load stream twice?
                is = context.getContentResolver().openInputStream(imageURI);
        //        if(bmp != null) {
        //        	bmp.recycle();
        //        }
                
        //        System.out.println("s:" + scale + " o:" + o.outWidth + ", " + o.outHeight + " **************************** decoding:" + imageURI);
                
                bmp = BitmapFactory.decodeStream(is, null, o2);
                success = true;
            }
            catch(OutOfMemoryError e) {
                e.printStackTrace();
                scale *= 2;
                o2.inSampleSize = scale; 
                if(retries++ >= 3) {
                	throw e;
                }
            }
            finally
            {
                try {
                    is.close();
                } catch (Exception e) {
                    // TODO: put all in logs
                    e.printStackTrace();
                }
            }
        }
        return bmp;
    }

    public static Paint createPaint(int alpha, int r, int g, int b) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setARGB(alpha, r, g, b);
        return paint;
    }

    public static Paint createPaint(int r, int g, int b) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setARGB(255, r, g, b);
        return paint;
    }

    public static Paint createPaint(Paint p, int colorChange, int alphaChange) {
        int a = Color.alpha(p.getColor()) + alphaChange;
        int r = Color.red(p.getColor()) + colorChange;
        int g = Color.green(p.getColor()) + colorChange;
        int b = Color.blue(p.getColor()) + colorChange;
        if (a < 0)
            a = 0;
        if (a > 255)
            a = 255;
        if (r < 0)
            r = 0;
        if (r > 255)
            r = 255;
        if (g < 0)
            g = 0;
        if (g > 255)
            g = 255;
        if (b < 0)
            b = 0;
        if (b > 255)
            b = 255;
        return createPaint(a, r, g, b);
    }

    // public static Paint createPaint(int alpha, int r, int g, int b, boolean antiAlias) {
    // Paint paint = new Paint();
    // paint.setAntiAlias(antiAlias);
    // paint.setARGB(alpha, r, g, b);
    // return paint;
    // }
    //
    // public static Paint createPaint(int r, int g, int b, boolean antiAlias) {
    // Paint paint = new Paint();
    // paint.setAntiAlias(antiAlias);
    // paint.setARGB(255, r, g, b);
    // return paint;
    // }

    public static Paint getDebugPaint() {
        return Utils.createPaint(Rand.rangeI(100, 200), Rand.rangeI(100, 200), Rand.rangeI(100, 200));
    }
}
