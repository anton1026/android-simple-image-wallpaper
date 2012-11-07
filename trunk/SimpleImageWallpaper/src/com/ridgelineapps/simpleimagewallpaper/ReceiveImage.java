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
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class ReceiveImage extends Activity  {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String action = intent.getAction();

        if (Intent.ACTION_SEND.equals(action))
        {
            if (extras.containsKey(Intent.EXTRA_STREAM))
            {
                try
                {
                    Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
                    String key = "full_image_uri";
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    
                    if(prefs.getBoolean("portrait_image_set", false)) {
                       try {
                          Point size = Utils.getBitmapSize(this, uri);
                          if(size.y > size.x){
                             key = "portrait_full_image_uri";
                          }
                       }
                       catch(Throwable t) {
                          t.printStackTrace();
                       }
                    }
                    
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(key, uri.toString());
                    editor.commit();
                } catch (Exception e)
                {
                    Log.e(this.getClass().getName(), e.toString());
                }
            } 
        }        
        
        finish();
    }
}