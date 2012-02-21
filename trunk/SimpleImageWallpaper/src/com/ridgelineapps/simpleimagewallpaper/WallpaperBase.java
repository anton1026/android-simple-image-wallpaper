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

import android.graphics.Canvas;

public class WallpaperBase {

    public boolean drawPaused = false;
    public boolean drawnAtLeastOnce = false;
    
    public DelegatingWallpaperService.SimpleWallpaperEngine engine;
    
    public int width;
    public int height;
    public int longSide;
    public int shortSide;
    
    //TODO: user drawPaused instead
    public boolean ready = true;

    public WallpaperBase() {
    }
    
    public void prefsChanged() {
        
    }
    
    public boolean preDraw() {
        return !drawPaused && ready;
    }
    
    public void draw(Canvas canvas) {
    }

    public void init(int width, int height, int longSide, int shortSide, boolean reload) {
        this.width = width;
        this.height = height;
        this.longSide = longSide;
        this.shortSide = shortSide;
    }

    public void cleanup() {
        // TODO: clean up resources like paints?
    }
}
