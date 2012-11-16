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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

public class Prefs extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    static final int SELECT_IMAGE = 1;
    static final int SELECT_PORTRAIT_IMAGE = 2;
    static final String FILE_URI_PREFIX = "file:///";
    
    private String selectedImagePath;
    private String filemanagerstring;
    
    SelectImagePreference selectImagePref;
    SelectPortraitImagePreference selectPortraitImagePref;

    public String getWallpaperName() {
        return "ImageFile";
    }

    public String getSetWallpaperKey() {
        return "set_wallpaper_image";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        
        SharedPreferences sharedPrefs = getPreferenceManager().getSharedPreferences(); // PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);


        selectImagePref = (SelectImagePreference) findPreference("image_file");
        selectImagePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                selectBackgroundImage();
                return true;
            }
        });
        
        selectPortraitImagePref = (SelectPortraitImagePreference) findPreference("image_file_portrait");
        selectPortraitImagePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                selectPortraitBackgroundImage();
                return true;
            }
        });
        
        // Call pref changed directly to clear out values that depend on portrait image being set, if necessary.
        onSharedPreferenceChanged(sharedPrefs, "portrait_image_set");
    }
    
   @Override
   protected void onDestroy() {
      if(selectImagePref != null) {
         selectImagePref.cleanup();
      }
      if(selectPortraitImagePref != null) {
         selectPortraitImagePref.cleanup();
      }
      super.onDestroy();
   }

   void selectBackgroundImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), SELECT_IMAGE);
        // Intent i = new Intent(
        // Intent.ACTION_PICK,
        // android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        // startActivityForResult(i, ACTIVITY_SELECT_IMAGE);
    }

    void selectPortraitBackgroundImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), SELECT_PORTRAIT_IMAGE);
        // Intent i = new Intent(
        // Intent.ACTION_PICK,
        // android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        // startActivityForResult(i, ACTIVITY_SELECT_IMAGE);
    }
    
    
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences shared, String key) {
        if(key.equals("portrait_image_set")) {
            if(!shared.getBoolean("portrait_image_set", false)) {
                SharedPreferences.Editor editor = shared.edit();
                editor.putString("portrait_full_image_uri", "");
                editor.putBoolean("image_file_fill_screen_portrait", false);
                editor.commit();
            }
        }
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_IMAGE || requestCode == SELECT_PORTRAIT_IMAGE) {
                Uri selectedImageUri = data.getData();

                filemanagerstring = selectedImageUri.getPath();
                selectedImagePath = getPath(selectedImageUri);

                String finalUri = (selectedImagePath != null) ? selectedImagePath : filemanagerstring;
                finalUri = FILE_URI_PREFIX + finalUri;

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor editor = prefs.edit();
                String key;
                if(requestCode == SELECT_IMAGE) {
                    key = "full_image_uri";
                }
                else {
                    key = "portrait_full_image_uri";
                }
                editor.putString(key, finalUri);
                editor.commit();
                if(requestCode == SELECT_PORTRAIT_IMAGE) {
                    selectPortraitImagePref.updateBackgroundImage(null);
                } else {
                    selectImagePref.updateBackgroundImage(null);
                }
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }
}