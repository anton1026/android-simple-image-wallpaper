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

import java.io.FileNotFoundException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;

public class Utils {
    public static Bitmap loadBitmap(Context context, Uri imageURI, int width, int height, boolean rotateIfNecessary, Integer density, float quality) throws FileNotFoundException {
       System.gc();
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        if(density != null) {
           // ?
           o.inScreenDensity = density;
           o.inTargetDensity = density;
        }
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
        int imageShortSide = Math.min(o.outWidth, o.outHeight);
         
        int scale=1;
        // Option 1
//        if (o.outHeight > longSide || o.outWidth > longSide) {
//            scale = (int) Math.pow(2, (int) Math.round(Math.log(longSide / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
//        }
        // Option 2
//        while(true) {
//        	if(rotateIfNecessary) {
//	        	if(imageLongSide / 2 < longSide) {
//	        		break;
//	        	}
//	        	
//	        	if(imageShortSide / 2 < shortSide) {
//	        		break;
//	        	}
//        	}
//        	else {
//	        	if(imageWidth / 2 < width) {
//	        		break;
//	        	}
//	        	
//	        	if(imageHeight / 2 < height) {
//	        		break;
//	        	}
//        	}
//
//        	imageLongSide /= 2;
//        	imageShortSide /= 2;
//        	imageWidth /= 2;
//        	imageHeight /= 2;
//            scale *= 2;
//        }
        // Option 3
        if(rotateIfNecessary) {
          if (imageLongSide > imageShortSide) {
              scale = Math.round((float) imageShortSide / (float) shortSide);
          } else {
              scale = Math.round((float) imageLongSide / (float) longSide);
          }           
        }
        else {
           if (imageWidth > imageHeight) {
              scale = Math.round((float) imageHeight / (float) height);
          } else {
              scale = Math.round((float) imageWidth / (float) width);
          }
        }
        
//        scale *= quality;

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        o2.inDither = true;
//        o2.inPreferQualityOverSpeed = true;
//        o2.inPurgeable = true;
//        o2.inInputShareable = false;
        if(density != null) {
           // ?
           o2.inScreenDensity = density;
           o2.inTargetDensity = density;
        }

        int retries = 0;
        boolean success = false;
        while(!success) {
            try {
               try {
                  if (bmp != null && !bmp.isRecycled()) {
                     bmp.recycle();
                     bmp = null;
                  }
               } catch (Exception e) {
                  // TODO: put all in logs
                  e.printStackTrace();
               }
               
               System.gc();
                // TODO: don't load stream twice?
                is = context.getContentResolver().openInputStream(imageURI);
                
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

    public static Paint createPaint(int r, int g, int b) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setARGB(255, r, g, b);
        return paint;
    }
}
