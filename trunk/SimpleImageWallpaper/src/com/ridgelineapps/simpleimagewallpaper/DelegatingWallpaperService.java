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

        WallpaperBase wallpaper;

        int longSide;
        int shortSide;

        int width;
        int height;

        private boolean visible = true;

        public Paint background;

        public SimpleWallpaperEngine() {
            background = Utils.createPaint(0, 0, 0);
            // TODO: anti-alias?

            SharedPreferences prefs = getPrefs();
            prefs.registerOnSharedPreferenceChangeListener(this);
        }

        public Context getBaseContext() {
            return DelegatingWallpaperService.this.getBaseContext();
        }
        
        public synchronized void onSharedPreferenceChanged(SharedPreferences shared, String key) {
            //TODO: causes wallpaper to be re-inited() as preferences are tweaked in code, do another way that doesn't require ignore hack here...
            
            if(wallpaper != null) {
                wallpaper.prefsChanged();
            }
            
            // TODO: better way to do, since this probably causes all of the start logic in the first draw(?)
            cleanupWallpaper();
            // refreshWallpaper(true);
        }

        public SharedPreferences getPrefs() {
            return PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible) {
                handler.post(drawRunner);
            } else {
                handler.removeCallbacks(drawRunner);
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
        	System.out.println("Simple Image Wallpaper: onSurfaceDestroyed()");
            super.onSurfaceDestroyed(holder);
            this.visible = false;
            cleanupWallpaper();
           	handler.removeCallbacks(drawRunner);
        }
        
        @Override
		public void onDestroy() {
        	System.out.println("Simple Image Wallpaper: onDestroy()");
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
                    WallpaperBase oldWallpaper = wallpaper;
                    wallpaper = null;
                    oldWallpaper.cleanup();
                    oldWallpaper.engine = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @SuppressWarnings("rawtypes")
        public synchronized void refreshWallpaper(boolean reload) {
        	
            if (!reload && wallpaper != null) {
                wallpaper.init(width, height, longSide, shortSide, false);
                wallpaper.drawPaused = false;
                drawAsap();
                return;
            }
            
            cleanupWallpaper();

            if (visible) {
                try {
                    wallpaper = new ImageFileWallpaper(DelegatingWallpaperService.this);
                    wallpaper.engine = this;
                    wallpaper.init(width, height, longSide, shortSide, true);

                } catch (Exception e) {
                    e.printStackTrace();
                    // TODO: what to do?
                }
                
                drawAsap();
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

            if (width > 0 && height > 0 && width != this.width || height != this.height) {
                this.width = width;
                this.height = height;
                longSide = Math.max(width, height);
                shortSide = Math.min(width, height);
                refreshWallpaper(false);
            }
        }

        public void drawAsap() {
            handler.removeCallbacks(drawRunner);
            if (visible) {
                handler.post(drawRunner);
            }
        }

//        @Override
//        public void onTouchEvent(MotionEvent event) {
//            super.onTouchEvent(event);
//
//            if (touchEnabled) {
//                if (wallpaper != null) {
//                    wallpaper.touched(event);
//                }
//            }
//        }

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                // TODO: check for "isReady" since we possibly could call the wallpaper before it is inited
                if (wallpaper == null) {
                    refreshWallpaper(false);
                }

                if (wallpaper != null) {
                    if (wallpaper.preDraw()) {
                        
                        //TODO: simple profile to make sure draw is not being called when it shouldn't be on wp that are 'static'
                        canvas = holder.lockCanvas();
                        if (canvas != null) {
                            wallpaper.draw(canvas);
                        }
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
