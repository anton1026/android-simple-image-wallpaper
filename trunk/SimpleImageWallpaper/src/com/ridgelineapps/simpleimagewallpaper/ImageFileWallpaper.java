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

public class ImageFileWallpaper extends WallpaperBase {

    String fileUri;
    Bitmap image;

    Bitmap imagePortrait;
    String fileUriPortrait;
    public boolean portraitDifferent;

    public boolean fill = false;
    public boolean rotate = false;
    
    Paint bitmapPaint;
    
    WallpaperService service;
    
    boolean orientationSet = false;
    int currentOrientation;
    int lastOrientation;

    public ImageFileWallpaper(WallpaperService service) {
    	this.service = service;
    }
    
    @Override
    public void draw(Canvas canvas) {
        try {
            Bitmap bmp;
            int width = this.width;
            int height = this.height;
    
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
            
            if (bmp != null) {
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
        finally {
            drawPaused = true;
        }
    }

    // TODO: Check that we are not painting more than once or listening to any events...
    @Override
    public void init(int width, int height, int longSide, int shortSide, boolean reload) {
        super.init(width, height, longSide, shortSide, reload);

        bitmapPaint = new Paint();
        bitmapPaint.setFilterBitmap(true);
        bitmapPaint.setDither(true);
        
        if (reload) {
            SharedPreferences prefs = engine.getPrefs();
            fill = prefs.getBoolean("image_file_fill_screen", true);
            rotate = prefs.getBoolean("image_file_rotate", false);
            
            fileUri = prefs.getString("full_image_uri", "");

            if (!fileUri.trim().equals("")) {
                try {
                    // image = Utils.scaledBitmapFromURIWithMinimumSize(engine.getBaseContext(),
                    // Uri.parse(fileUri), width, height, false);
                    image = null;
                    image = Utils.loadBitmap(engine.getBaseContext(), Uri.parse(fileUri), width, height, fill, rotate);

                } catch (FileNotFoundException e) {
                    Log.e("ImageFileWallpaper", "0", e);
                }
            }

            portraitDifferent = prefs.getBoolean("portrait_image_set", false);

            if (!portraitDifferent) {
                imagePortrait = image;
            } else {
                fileUriPortrait = prefs.getString("portrait_full_image_uri", "");

                if (!fileUriPortrait.trim().equals("")) {
                    try {
                        imagePortrait = null;
                        imagePortrait = Utils.loadBitmap(engine.getBaseContext(), Uri.parse(fileUriPortrait), width, height, fill, rotate);

                    } catch (FileNotFoundException e) {
                        Log.e("ImageFileWallpaper", "1", e);
                    }
                }
            }
        }
    }

    @Override
    public void cleanup() {
    	super.cleanup();
        if (image != null && !image.isRecycled()) {
            image.recycle();
        }

        if (imagePortrait != null && !imagePortrait.isRecycled()) {
            imagePortrait.recycle();
        }
        
        image = null;
        imagePortrait = null;
        
        // TODO: clean up resources like paints?
    }
}
