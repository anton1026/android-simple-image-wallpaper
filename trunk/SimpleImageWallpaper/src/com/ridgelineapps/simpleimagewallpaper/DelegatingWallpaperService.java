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
                draw();
            }

        };

        WallpaperBase wallpaper;

        int longSide;
        int shortSide;

        int width;
        int height;

        private boolean visible = true;
//        private boolean touchEnabled = true;

//        boolean firstDraw = true;

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
            
            SharedPreferences prefs = getPrefs();

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
            super.onSurfaceDestroyed(holder);
            this.visible = false;
            cleanupWallpaper();
            handler.removeCallbacks(drawRunner);
        }

        public void cleanupWallpaper() {
//        	System.out.println("::cleanup:" + this + ", wp:" + wallpaper);
            if (wallpaper != null) {
                try {
                    WallpaperBase oldWallpaper = wallpaper;
                    wallpaper = null;
                    oldWallpaper.cleanup();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @SuppressWarnings("rawtypes")
        public synchronized void refreshWallpaper(boolean reload) {
//        	System.out.println("::refresh:" + this);
        	
            // TODO: how often to get these? is there way to only set them when changed easily?
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String wallpaperType = "ImageFile";
            
            if (!reload && wallpaper != null) {
                if(wallpaperType.equals("ImageFile") || wallpaperType.equals("PhotoSite")) {
                    wallpaper.init(width, height, longSide, shortSide, false);
                    wallpaper.drawPaused = false;
                    drawAsap();
                    return;
                }
            }
            
            cleanupWallpaper();

            if (visible) {
                Class c = null;

                c = ImageFileWallpaper.class;
                
                
                
                try {
                    wallpaper = (WallpaperBase) c.newInstance();
                    wallpaper.engine = this;
//                    System.out.println("calling init for:" + wallpaper);
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

//                drawAsap();
//                firstDraw = true;
                // TODO: don't refresh, just update
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
            // if (wallpaper == null && width != 0 && height != 0) {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                // if (firstDraw) {
                // firstDraw = false;
                // try {
                // canvas = holder.lockCanvas();
                // if (canvas != null) {
                // canvas.drawRect(0, 0, width, height, background);
                // }
                // } finally {
                // try {
                // if (canvas != null) {
                // holder.unlockCanvasAndPost(canvas);
                // }
                // } finally {
                // canvas = null;
                // }
                // }
                // }

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

                        // wallpaper.touched(null);
                    }
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
            // }
            handler.removeCallbacks(drawRunner);
            if (visible) {
                if (wallpaper != null && wallpaper.drawInterval >= 0) {
                    handler.postDelayed(drawRunner, wallpaper.drawInterval);
                }
                // else {
                // handler.postDelayed(drawRunner, 1000);
                // }
            }
        }
    }
}
