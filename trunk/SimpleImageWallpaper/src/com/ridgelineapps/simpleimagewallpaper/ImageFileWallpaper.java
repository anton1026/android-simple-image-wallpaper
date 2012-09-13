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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

public class ImageFileWallpaper {
    public static final boolean debug = false;

    WallpaperService service;
    DelegatingWallpaperService.SimpleWallpaperEngine engine;
    
    String currentFileUri = "";
    Bitmap image;

    Bitmap imagePortrait;
    String currentPortraitFileUri = "";
    public boolean portraitDifferent;

    public boolean fill = false;
    public boolean rotate = false;
    
    Paint bitmapPaint;
    
    boolean orientationSet = false;
    int currentOrientation;
    int lastOrientation;
    
    boolean imageLoaded = false;

    public ImageFileWallpaper(WallpaperService service, DelegatingWallpaperService.SimpleWallpaperEngine engine) {
    	this.service = service;
    	this.engine = engine;
        bitmapPaint = new Paint();
        bitmapPaint.setFilterBitmap(true);
        bitmapPaint.setDither(true);
    }
    
    public void prefsChanged() {
        
        SharedPreferences prefs = engine.getPrefs();
        fill = prefs.getBoolean("image_file_fill_screen", true);
        boolean oldRotate = rotate;
        rotate = prefs.getBoolean("image_file_rotate", false);
        
        String fileUri = prefs.getString("full_image_uri", "");
        portraitDifferent = prefs.getBoolean("portrait_image_set", false);
        String portraitFileUri = prefs.getString("portrait_full_image_uri", "");
        
        if(fileUri != currentFileUri || oldRotate != rotate) {
        	currentFileUri = fileUri;
        	loadImage();
        }
        
        if(portraitFileUri != currentPortraitFileUri || oldRotate != rotate) {
        	currentPortraitFileUri= portraitFileUri;
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
        
        Bitmap bmp;
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        if (portraitDifferent && width < height) {
            bmp = imagePortrait;
        } else {
            bmp = image;
        }
        
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
        if (bmp != null && (!engine.screenLocked || !engine.hideWhenScreenIsLocked)) {
            // canvas.drawBitmap(bmp, 0, 0, engine.background);
            float scaleWidth = (float) width / bmp.getWidth();
            float scaleHeight = (float) height / bmp.getHeight();

            float scale;

            if (fill) {
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

            canvas.drawRect(0, 0, width, height, engine.background);
            boolean rotated = false;
            if(rotate) {
                if((width < height && destWidth > destHeight) || (width > height && destHeight > destWidth)) {
                    rotated = true;
                    int rWidth = height;
                    int rHeight = width;

                    scaleWidth = (float) rWidth / bmp.getWidth();
                    scaleHeight = (float) rHeight / bmp.getHeight();
        
                    if (fill) {
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
        } else {
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), engine.background);
        }
    }
    
    public void loadImage() {
//        System.out.println("loadImage");
        if (!currentFileUri.trim().equals("")) {
            try {
                image = Utils.loadBitmap(engine.getBaseContext(), Uri.parse(currentFileUri), engine.width, engine.height, rotate);
                imageLoaded = true;

            } catch (Throwable e) {
                Log.e("ImageFileWallpaper", "0", e);
            }
        }
        else {
            if (image != null && !image.isRecycled()) {
                image.recycle();
                image = null;
            }
        	imageLoaded = true;
        }
    }
    
    public void loadPortraitImage() {
        if (!currentPortraitFileUri.trim().equals("")) {
            try {
                imagePortrait = Utils.loadBitmap(engine.getBaseContext(), Uri.parse(currentPortraitFileUri), engine.width, engine.height, rotate);
            } catch (Throwable e) {
                Log.e("ImageFileWallpaper", "1", e);
            }
        }
        else {
            if (imagePortrait != null && !imagePortrait.isRecycled()) {
                imagePortrait.recycle();
                imagePortrait = null;
            }
        }
    }

    public void cleanup() {
        if (image != null && !image.isRecycled()) {
            image.recycle();
        }

        if (imagePortrait != null && !imagePortrait.isRecycled()) {
            imagePortrait.recycle();
        }
        
        currentPortraitFileUri = "";
        currentFileUri = "";
        image = null;
        imagePortrait = null;
        engine = null;
        service = null;
        imageLoaded = false;
    }
}
