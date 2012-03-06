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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class DelegatingWallpaperService extends WallpaperService {
    @Override
    public Engine onCreateEngine() {
        return new SimpleWallpaperEngine();
    }

    public class SimpleWallpaperEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {
        private final Handler handler = new Handler();
        private final Runnable drawRunner = new Runnable() {
            @Override
            public void run() {
                SurfaceHolder holder = getSurfaceHolder();
                Canvas canvas = null;
                try {
                    if(!wallpaper.imageLoaded) {
                    	wallpaper.loadImage();
                    	wallpaper.loadPortraitImage();
                    }
                    
                    if (visible) {
                        canvas = holder.lockCanvas();
                        if (canvas != null) {
                            wallpaper.draw(canvas);
                        }
                    }
                } finally {
                    if (canvas != null) {
                        holder.unlockCanvasAndPost(canvas);
                    }
                }
                handler.removeCallbacks(drawRunner);
                
            	if(!wallpaper.imageLoaded) {
                    handler.postDelayed(drawRunner, retryDelay * 1000L);
                    retryDelay *= 2;
            	}
            	else {
            		retryDelay = 1;
            	}
            }
        };
        
        int retryDelay = 1;
        
        int width;
        int height;

        ImageFileWallpaper wallpaper = new ImageFileWallpaper(DelegatingWallpaperService.this, this);
        private boolean visible = false;

        public Paint background;

        public SimpleWallpaperEngine() {
            background = Utils.createPaint(0, 0, 0);
            getPrefs().registerOnSharedPreferenceChangeListener(this);
        }

        public Context getBaseContext() {
            return DelegatingWallpaperService.this.getBaseContext();
        }
        
        public synchronized void onSharedPreferenceChanged(SharedPreferences shared, String key) {
        	wallpaper.prefsChanged();
        }

        public SharedPreferences getPrefs() {
            return PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
//        	System.out.println("visibility:" + this.visible + ", " + visible);
            this.visible = visible;
            removeAndPost();
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
//        	System.out.println("Simple Image Wallpaper: onSurfaceDestroyed()");
            super.onSurfaceDestroyed(holder);
            this.visible = false;
           	handler.removeCallbacks(drawRunner);
        }
        
        @Override
		public void onDestroy() {
//        	System.out.println("Simple Image Wallpaper: onDestroy()");
            visible = false;
           	wallpaper.cleanup();
           	handler.removeCallbacks(drawRunner);
            if(getPrefs() != null)
                getPrefs().unregisterOnSharedPreferenceChangeListener(this);
            super.onDestroy();
		}

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

//            if (width > 0 && height > 0 && width != this.width || height != this.height) {
//            	System.out.println("" + this.width + ", " + this.height + " " + width + ", " + height);
                this.width = width;
                this.height = height;
                removeAndPost();
//            }
        }

        public void removeAndPost() {
//        	System.out.println("drawAsap()");
            handler.removeCallbacks(drawRunner);
            if (visible) {
                handler.post(drawRunner);
            }
        }
    }
}
