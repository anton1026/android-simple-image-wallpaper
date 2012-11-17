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

package com.ridgelineapps.simpleimagewallpaperdonate;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ridgelineapps.simpleimagewallpaperdonate.R;

public class SelectImagePreference extends Preference {
    ImageView imageView;
    String prefKey = "full_image_uri";
    Bitmap bitmap;

    public SelectImagePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onBindView(View view) {
        super.onBindView(view);
        imageView = (ImageView) view.findViewById(R.id.prefs_image_view);
        updateBackgroundImage(view);
        updateTextState(view);
    }
    
    private void updateTextState(View view) {
        TextView tv = (TextView) view.findViewById(R.id.prefs_image_text);
        if(tv != null) {
        	if(isEnabled()) {
        		tv.setTextColor(Color.WHITE);
        	} else {
        		tv.setTextColor(Color.GRAY);
        	}
        	
        	tv.setEnabled(isEnabled());
        }
    }

    void updateBackgroundImage(View view) {
    	if(view != null)
    		updateTextState(view);
        if (imageView != null) {
           imageView.setImageBitmap(null);
           Utils.recycleBitmap(bitmap);
           bitmap = null;
           
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            String imageURIString = prefs.getString(prefKey, null);
            if (imageURIString != null && !imageURIString.trim().equals("")) {
                Uri imageURI = Uri.parse(imageURIString);
                try {
                   System.gc();
                    bitmap = Utils.loadBitmap(getContext(), imageURI, Math.max(128, imageView.getWidth()), Math.max(128, imageView.getHeight()), false, null, 1.0f, null);
    	            if(bitmap != null) {
    	            	imageView.setImageBitmap(bitmap);
    	            }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    void cleanup() {
       try {
          Utils.recycleBitmap(bitmap);
          bitmap = null;
          if(imageView != null) {
             imageView.setImageBitmap(null);
          }
       }
       catch(Throwable t) {
          t.printStackTrace();
       }
    }
}
