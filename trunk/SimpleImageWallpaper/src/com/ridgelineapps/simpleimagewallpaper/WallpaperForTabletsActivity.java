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

import android.app.Activity;
import android.os.Bundle;

public class WallpaperForTabletsActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main);

//        Intent i = new Intent(WallpaperForTabletsActivity.this, ProcessingActivity.class);
//        startActivity(i);
        
//         Toast toast = Toast.makeText(this,
//         "Choose \"Wallpaper for Tablets\" from the list to start the Live Wallpaper.",
//         Toast.LENGTH_LONG);
//         toast.show();
//         Intent intent = new Intent();
//         intent.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
//         startActivity(intent);

        // TODO: can settings be launched if wp is active?
        //Intent i = new Intent(WallpaperForTabletsActivity.this, Prefs.class);
        //startActivity(i);

        // ((Button) findViewById(R.id.button1)).setOnClickListener(new
        // OnClickListener() {
        // @Override
        // public void onClick(View v) {
        // Intent i = new Intent(AbstractPatternsActivity.this, Prefs.class);
        // startActivity(i);
        // }
        // });
    }


}
