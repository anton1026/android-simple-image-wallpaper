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

    	WallpaperService service = null;
    	
        private final Handler handler = new Handler();
        private final Runnable drawRunner = new Runnable() {
            @Override
            public void run() {
                draw();
            }

        };
        
        int width;
        int height;

        ImageFileWallpaper wallpaper;

        private boolean visible = true;

        public Paint background;

        public SimpleWallpaperEngine() {
            background = Utils.createPaint(0, 0, 0);
            SharedPreferences prefs = getPrefs();
            prefs.registerOnSharedPreferenceChangeListener(this);
        }

        public Context getBaseContext() {
            return DelegatingWallpaperService.this.getBaseContext();
        }
        
        public synchronized void onSharedPreferenceChanged(SharedPreferences shared, String key) {
            cleanupWallpaper();
        }

        public SharedPreferences getPrefs() {
            return PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
//        	System.out.println("visibility:" + this.visible + ", " + visible);
            this.visible = visible;
            if (visible) {
            	if(wallpaper == null) {
            		drawAsap();
            	}
//                handler.post(drawRunner);
            } else {
                handler.removeCallbacks(drawRunner);
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
//        	System.out.println("Simple Image Wallpaper: onSurfaceDestroyed()");
            super.onSurfaceDestroyed(holder);
            this.visible = false;
            cleanupWallpaper();
           	handler.removeCallbacks(drawRunner);
        }
        
        @Override
		public void onDestroy() {
//        	System.out.println("Simple Image Wallpaper: onDestroy()");
			super.onDestroy();
            this.visible = false;
            cleanupWallpaper();
           	handler.removeCallbacks(drawRunner);
            SharedPreferences prefs = getPrefs();
            if(prefs != null)
            	prefs.unregisterOnSharedPreferenceChangeListener(this);
		}

		public void cleanupWallpaper() {
            if (wallpaper != null) {
                try {
                    ImageFileWallpaper oldWallpaper = wallpaper;
                    wallpaper = null;
                    oldWallpaper.cleanup();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public synchronized void refreshWallpaper() {
        	
            if (wallpaper != null) {
                drawAsap();
            }
            else {
	            cleanupWallpaper();
	
	            if (visible) {
	//                try {
	                    wallpaper = new ImageFileWallpaper(DelegatingWallpaperService.this, this, width, height);
	//                } catch (Exception e) {
	//                    e.printStackTrace();
	//                }
	                
	                drawAsap();
	            }
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

            if (width > 0 && height > 0 && width != this.width || height != this.height) {
//            	System.out.println("" + this.width + ", " + this.height + " " + width + ", " + height);
                this.width = width;
                this.height = height;
                refreshWallpaper();
            }
        }

        public void drawAsap() {
//        	System.out.println("drawAsap()");
            handler.removeCallbacks(drawRunner);
            if (visible) {
                handler.post(drawRunner);
            }
        }

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                if (wallpaper == null) {
                    refreshWallpaper();
                }

                if (wallpaper != null) {
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
        }
    }
}
