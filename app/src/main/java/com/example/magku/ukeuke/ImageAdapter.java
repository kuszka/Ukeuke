package com.example.magku.ukeuke;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Created by magku on 30.05.2017.
 *
 *
 */

public class ImageAdapter extends BaseAdapter {
    private Context mContext;

    public ImageAdapter(Context c) {
        mContext = c;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(220, 440));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(8, 8, 8, 8);
            imageView.setBackgroundColor(Color.WHITE);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }
    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }
    public int getCount() {
        return mThumbIds.length;
    }
    private Integer[] mThumbIds = {
            R.drawable.chc,R.drawable.chcm,R.drawable.chc7,R.drawable.chcm7,
            R.drawable.chd,R.drawable.chdm,R.drawable.chd7,R.drawable.chdm7,
            R.drawable.che,R.drawable.chem,R.drawable.che7,R.drawable.chem7,
            R.drawable.chf,R.drawable.chfm,R.drawable.chf7,R.drawable.chfm7,
            R.drawable.chg,R.drawable.chgm,R.drawable.chg7,R.drawable.chgm7,
            R.drawable.cha,R.drawable.cham,R.drawable.cha7,R.drawable.cham7,
            R.drawable.chb,R.drawable.chbm,R.drawable.chb7,R.drawable.chbm7
    };
}
