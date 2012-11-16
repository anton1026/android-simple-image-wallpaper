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

import android.app.Service;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

public class ImageFileWallpaper {
    public static final boolean debug = false;
    public static final boolean pro = true;

    WallpaperService service;
    DelegatingWallpaperService.SimpleWallpaperEngine engine;
    
    String currentFileUri = "";
    Bitmap image;

    Bitmap imagePortrait;
    String currentPortraitFileUri = "";
    public boolean portraitDifferent;

    public boolean fillPortrait = false;
    public boolean fillLandscape = false;
    public boolean rotate = false;
    
    Paint bitmapPaint;
    
    boolean orientationSet = false;
    int currentOrientation;
    int lastOrientation;
    
    float quality = 1.0f;
    
    boolean imageLoaded = false;
    boolean portraitImageLoaded = false;

    Integer density = null;
    
    public ImageFileWallpaper(WallpaperService service, DelegatingWallpaperService.SimpleWallpaperEngine engine) {
    	this.service = service;
    	this.engine = engine;
        bitmapPaint = new Paint();
        bitmapPaint.setFilterBitmap(true);
        bitmapPaint.setDither(true);
        
        try {
           DisplayMetrics metrics = service.getBaseContext().getResources().getDisplayMetrics();
           density = metrics.densityDpi;
        }
        catch(Throwable e) {
           e.printStackTrace();
        }
    }
    
    public void prefsChanged() {
        
        SharedPreferences prefs = engine.getPrefs();
        fillLandscape = prefs.getBoolean("image_file_fill_screen", true);
        boolean oldRotate = rotate;
        rotate = prefs.getBoolean("image_file_rotate", false);
        
        engine.hideWhenScreenIsLocked = prefs.getBoolean("image_file_hide_if_locked", false);
        
//        int qualityPref = 10;
//        try {
//           String qualityStr = prefs.getString("quality", "10");
//           if(qualityStr != null) {
//              qualityPref = Integer.parseInt(qualityStr);
//           }
//         } catch (NumberFormatException e) {
//            e.printStackTrace();
//        }        
//        quality = 0.1f * qualityPref;
        
        String fileUri = prefs.getString("full_image_uri", "");
        portraitDifferent = prefs.getBoolean("portrait_image_set", false);
        String portraitFileUri = prefs.getString("portrait_full_image_uri", "");
        
        if(pro && portraitDifferent) {
           fillPortrait = prefs.getBoolean("image_file_fill_screen_portrait", true);
        }
        else {
           fillPortrait = fillLandscape;
        }
        
        if(fileUri != currentFileUri || oldRotate != rotate || !imageLoaded) {
        	currentFileUri = fileUri;
        	imageLoaded = false;
        }
        
        if(!imageLoaded) {
        	loadImage();
        }
        
        if(portraitFileUri != currentPortraitFileUri || oldRotate != rotate || !portraitImageLoaded) {
        	currentPortraitFileUri= portraitFileUri;
        	portraitImageLoaded = false;
        }
        
        if(!portraitImageLoaded) {
        	loadPortraitImage();
        }
    }
    
    public void draw(Canvas canvas) {
        if(debug)
            System.out.println("Simple Image Wallpaper draw, width:" + canvas.getWidth() + " height:" + canvas.getHeight());
        
        if(canvas.getWidth() != engine.width || canvas.getHeight() != engine.height) {
            engine.postAgain = true;
            return;
        }
        
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        canvas.drawRect(0, 0, width, height, engine.background);


        int orientationNow = ((WindowManager) service.getApplication().getSystemService(Service.WINDOW_SERVICE)).getDefaultDisplay().getOrientation();
        if(!orientationSet) {
        	currentOrientation = orientationNow;
        	lastOrientation = orientationNow;
        	orientationSet = true;
        }
        
        if(orientationNow != currentOrientation) {
        	lastOrientation = currentOrientation;
        	currentOrientation = orientationNow;
        }
        
        if(debug)
            System.out.println("locked:" + engine.screenLocked + ", hide:" + engine.hideWhenScreenIsLocked);
        
        Bitmap bmp;
        if (portraitDifferent && width < height) {
           bmp = imagePortrait;
        } else {
           bmp = image;
        }
        if (bmp != null && (!engine.screenLocked || !engine.hideWhenScreenIsLocked)) {
            // canvas.drawBitmap(bmp, 0, 0, engine.background);
            float scaleWidth = (float) width / bmp.getWidth();
            float scaleHeight = (float) height / bmp.getHeight();

            float scale;
            int orientationType = service.getBaseContext().getResources().getConfiguration().orientation;
            
            if ((orientationType == Configuration.ORIENTATION_PORTRAIT && fillPortrait) ||
                (orientationType == Configuration.ORIENTATION_LANDSCAPE && fillLandscape)) {
                scale = Math.max(scaleWidth, scaleHeight);
            } else {
                scale = Math.min(scaleWidth, scaleHeight);
            }

            int destWidth = (int) (bmp.getWidth() * scale);
            int destHeight = (int) (bmp.getHeight() * scale);

            int x = 0;
            int y = 0;

            x = (width - destWidth) / 2;
            y = (height - destHeight) / 2;

            Rect dest = new Rect(x, y, x + destWidth, y + destHeight);

            boolean rotated = false;
            if(rotate) {
                if((width < height && destWidth > destHeight) || (width > height && destHeight > destWidth)) {
                    rotated = true;
                    int rWidth = height;
                    int rHeight = width;

                    scaleWidth = (float) rWidth / bmp.getWidth();
                    scaleHeight = (float) rHeight / bmp.getHeight();
        
                    if ((orientationType == Configuration.ORIENTATION_PORTRAIT && fillPortrait) ||
                        (orientationType == Configuration.ORIENTATION_LANDSCAPE && fillLandscape)) {
                        scale = Math.max(scaleWidth, scaleHeight);
                    } else {
                        scale = Math.min(scaleWidth, scaleHeight);
                    }
         
                    destWidth = (int) (bmp.getWidth() * scale);
                    destHeight = (int) (bmp.getHeight() * scale);
        
                    if((lastOrientation == Surface.ROTATION_0 && currentOrientation == Surface.ROTATION_90) ||
                       (lastOrientation == Surface.ROTATION_180 && currentOrientation == Surface.ROTATION_270) ||
                       (lastOrientation == Surface.ROTATION_90 && currentOrientation == Surface.ROTATION_180) ||
                       (lastOrientation == Surface.ROTATION_270 && currentOrientation == Surface.ROTATION_0)
                       ) {
                        canvas.rotate(270);

                        y = (rHeight - destHeight) / 2;
                        x = -rWidth + ((rWidth - destWidth) / 2);
                        dest = new Rect(x, y, x + destWidth, y + destHeight);
                        canvas.drawBitmap(bmp, null, dest, bitmapPaint);
                        
                        canvas.rotate(-270);
                    }
                    else {
                        canvas.rotate(90);

                        y = -rHeight + ((rHeight - destHeight) / 2);
                        x = (rWidth - destWidth) / 2;
                        dest = new Rect(x, y, x + destWidth, y + destHeight);
                        canvas.drawBitmap(bmp, null, dest, bitmapPaint);
                        
                        canvas.rotate(-90);
                    }
                }
            }
            
            if(!rotated) {
                canvas.drawBitmap(bmp, null, dest, bitmapPaint);
            }
        } 
    }
    
   public void loadImage() {
      // System.out.println("loadImage");
      synchronized (this) {
         Utils.recycleBitmap(image);
         image = null;
         try {
            if (!currentFileUri.trim().equals("")) {
               image = Utils.loadBitmap(engine.getBaseContext(), Uri.parse(currentFileUri), engine.width, engine.height, rotate, density, quality);
            }
            imageLoaded = true;
         } catch (Throwable e) {
            imageLoaded = false;
            Log.e("ImageFileWallpaper", "Exception during loadImage", e);
         }
      }
   }

   public void loadPortraitImage() {
      synchronized (this) {
         Utils.recycleBitmap(imagePortrait);
         imagePortrait = null;
         try {
            if (!currentPortraitFileUri.trim().equals("")) {
               imagePortrait = Utils.loadBitmap(engine.getBaseContext(), Uri.parse(currentPortraitFileUri), engine.width, engine.height, rotate, density, quality);
            }
            portraitImageLoaded = true;
         } catch (Throwable e) {
            portraitImageLoaded = false;
            Log.e("ImageFileWallpaper", "Exception during loadPortraitImage", e);
         }
      }
   }

    public void cleanup() {
        Utils.recycleBitmap(image);
        Utils.recycleBitmap(imagePortrait);
        currentPortraitFileUri = "";
        currentFileUri = "";
        image = null;
        imagePortrait = null;
        engine = null;
        service = null;
        imageLoaded = false;
        portraitImageLoaded = false;
    }
}
