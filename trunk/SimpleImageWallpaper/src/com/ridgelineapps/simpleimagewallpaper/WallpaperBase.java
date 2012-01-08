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

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

public class WallpaperBase {

    // public Paint background;
    // public boolean drawBackground;

    public boolean drawPaused = false;
    public boolean drawnAtLeastOnce = false;
    
    // protected long lastStep = 0L;
    // public static int drawInterval = 1000 / 20;
    // public static int smallStepInterval = drawInterval;
    // public static int largeStepInterval = 500;
    // protected int step = 0;

    public DelegatingWallpaperService.SimpleWallpaperEngine engine;
    
    public int width;
    public int height;
    public int longSide;
    public int shortSide;
    
    protected long blackoutDuration = 2000;
    //TODO: don't do blackout around handling tap..
    protected boolean blackout;
    protected long blackoutStartTime;
    protected boolean blackoutOnMove = false;
    protected boolean allowsBlackout = false;
    protected Paint blackoutPaint;
    
    
    public int drawInterval = -1;
//    public boolean firstDraw = true;

    //TODO: user drawPaused instead
    public boolean ready = true;

    public WallpaperBase() {
        blackoutPaint = Utils.createPaint(120, 0, 0, 0);
    }
    
    public void prefsChanged() {
        
    }
    
    public boolean doubleTap() {
        return true;
    }
    
    public boolean preDraw() {
        if (blackout) {
            if (System.currentTimeMillis() - blackoutStartTime > blackoutDuration) {
                blackout = false;
                drawPaused = false;
            }
        }
        
        return !drawPaused && ready;
    }
    
    public void draw(Canvas canvas) {
    }

    public void init(int width, int height, int longSide, int shortSide, boolean reload) {
        this.width = width;
        this.height = height;
        this.longSide = longSide;
        this.shortSide = shortSide;

        SharedPreferences prefs = engine.getPrefs();
        blackoutOnMove = prefs.getBoolean("darken_on_touch", false);
        try {
            String durationStr = prefs.getString("darken_duration", "2");
            int sec = Integer.parseInt(durationStr);
            blackoutDuration = sec * 1000;
        }
        catch(Exception e) {
            e.printStackTrace(); 
        }
        
        blackout = false;
        blackoutStartTime = 0; 
    }

    public void touched(MotionEvent event) {
        if(allowsBlackout) {
            if (blackoutOnMove && event != null) {
                blackoutStartTime = System.currentTimeMillis();
    
                if (!blackout) {
                    blackout = true;
                    drawPaused = false;
                    engine.drawAsap();
                }
            }
        }
    }

    public void randomize() {
    }

//    public void checkBigStep() {
//    }
//
//    public void checkSmallStep() {
//    }

    public void step() {
    }

    // public Paint getBackground() {
    // if (background == null) {
    // background = Utils.createPaint(0, 0, 0);
    // }
    //
    // return background;
    // }

    public void cleanup() {
        // TODO: clean up resources like paints?
    }
}
