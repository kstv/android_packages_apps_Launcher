/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.content.res.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

public class WallpaperChooser extends Activity implements AdapterView.OnItemSelectedListener,
        OnClickListener {

    private static final Integer[] THUMB_IDS = {
	    R.drawable.wallpaper_skate_small,
        R.drawable.wallpaper_prash_nexus_surf_small,
	    R.drawable.wallpaper_cyan_small,
        R.drawable.wallpaper_cyan_green_small,
        R.drawable.wallpaper_donut_small,
	    R.drawable.wallpaper_glass_small,
	    R.drawable.wallpaper_hazey_small,
	    R.drawable.wallpaper_frog_small,
	    R.drawable.wallpaper_turtle_small,
	    R.drawable.wallpaper_0008_small,
	    R.drawable.wallpaper_0013_small,
	    R.drawable.wallpaper_bossa_small,
	    R.drawable.wallpaper_clone_small,
	    R.drawable.wallpaper_curves_small,
	    R.drawable.wallpaper_deep_small,
	    R.drawable.wallpaper_linked_small,
	    R.drawable.wallpaper_pacific_small,
	    R.drawable.wallpaper_reactive_small,
	    R.drawable.wallpaper_resonance_small,
	    R.drawable.wallpaper_siren_small,
	    R.drawable.wallpaper_stack_small,
	    R.drawable.wallpaper_swell_small,
	    R.drawable.wallpaper_tangy_small,
	    R.drawable.wallpaper_track_small,
	    R.drawable.wallpaper_vibe_small
    };

    private static final Integer[] IMAGE_IDS = {
	    R.drawable.wallpaper_skate,
        R.drawable.wallpaper_prash_nexus_surf,
	    R.drawable.wallpaper_cyan,
        R.drawable.wallpaper_cyan_green,
        R.drawable.wallpaper_donut,
	    R.drawable.wallpaper_glass,
	    R.drawable.wallpaper_hazey,
	    R.drawable.wallpaper_frog,
	    R.drawable.wallpaper_turtle,
	    R.drawable.wallpaper_0008,
	    R.drawable.wallpaper_0013,
	    R.drawable.wallpaper_bossa,
	    R.drawable.wallpaper_clone,
	    R.drawable.wallpaper_curves,
	    R.drawable.wallpaper_deep,
	    R.drawable.wallpaper_linked,
	    R.drawable.wallpaper_pacific,
	    R.drawable.wallpaper_reactive,
	    R.drawable.wallpaper_resonance,
	    R.drawable.wallpaper_siren,
	    R.drawable.wallpaper_stack,
	    R.drawable.wallpaper_swell,
	    R.drawable.wallpaper_tangy,
	    R.drawable.wallpaper_track,
	    R.drawable.wallpaper_vibe
    };

    private Gallery mGallery;
    private ImageView mImageView;
    private boolean mIsWallpaperSet;

    private BitmapFactory.Options mOptions;
    private Bitmap mBitmap;

    private ArrayList<Integer> mThumbs;
    private ArrayList<Integer> mImages;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        findWallpapers();

        setContentView(R.layout.wallpaper_chooser);

        mOptions = new BitmapFactory.Options();
        mOptions.inDither = false;
        mOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

        mGallery = (Gallery) findViewById(R.id.gallery);
        mGallery.setAdapter(new ImageAdapter(this));
        mGallery.setOnItemSelectedListener(this);
        mGallery.setCallbackDuringFling(false);

        Button b = (Button) findViewById(R.id.set);
        b.setOnClickListener(this);

        mImageView = (ImageView) findViewById(R.id.wallpaper);
    }

    private void findWallpapers() {
        mThumbs = new ArrayList<Integer>(THUMB_IDS.length + 4);
        Collections.addAll(mThumbs, THUMB_IDS);

        mImages = new ArrayList<Integer>(IMAGE_IDS.length + 4);
        Collections.addAll(mImages, IMAGE_IDS);

        final Resources resources = getResources();
        final String[] extras = resources.getStringArray(R.array.extra_wallpapers);
        final String packageName = getApplication().getPackageName();

        for (String extra : extras) {
            int res = resources.getIdentifier(extra, "drawable", packageName);
            if (res != 0) {
                final int thumbRes = resources.getIdentifier(extra + "_small",
                        "drawable", packageName);

                if (thumbRes != 0) {
                    mThumbs.add(thumbRes);
                    mImages.add(res);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsWallpaperSet = false;
    }

    public void onItemSelected(AdapterView parent, View v, int position, long id) {
        final ImageView view = mImageView;
        Bitmap b = BitmapFactory.decodeResource(getResources(), mImages.get(position), mOptions);
        view.setImageBitmap(b);

        // Help the GC
        if (mBitmap != null) {
            mBitmap.recycle();
        }
        mBitmap = b;

        final Drawable drawable = view.getDrawable();
        drawable.setFilterBitmap(true);
        drawable.setDither(true);
    }

    /*
     * When using touch if you tap an image it triggers both the onItemClick and
     * the onTouchEvent causing the wallpaper to be set twice. Ensure we only
     * set the wallpaper once.
     */
    private void selectWallpaper(int position) {
        if (mIsWallpaperSet) {
            return;
        }

        mIsWallpaperSet = true;
        try {
            InputStream stream = getResources().openRawResource(mImages.get(position));
            setWallpaper(stream);
            setResult(RESULT_OK);
            finish();
        } catch (IOException e) {
            Log.e(Launcher.LOG_TAG, "Failed to set wallpaper: " + e);
        }
    }

    public void onNothingSelected(AdapterView parent) {
    }

    private class ImageAdapter extends BaseAdapter {
        private LayoutInflater mLayoutInflater;

        ImageAdapter(WallpaperChooser context) {
            mLayoutInflater = context.getLayoutInflater();
        }

        public int getCount() {
            return mThumbs.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView image;

            if (convertView == null) {
                image = (ImageView) mLayoutInflater.inflate(R.layout.wallpaper_item, parent, false);
            } else {
                image = (ImageView) convertView;
            }

            image.setImageResource(mThumbs.get(position));
            image.getDrawable().setDither(true);
            return image;
        }
    }

    public void onClick(View v) {
        selectWallpaper(mGallery.getSelectedItemPosition());
    }
}
