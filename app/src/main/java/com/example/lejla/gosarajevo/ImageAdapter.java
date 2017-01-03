package com.example.lejla.gosarajevo;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;


public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mThumbIds.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(200, 200));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(5, 0, 0, 0);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(mThumbIds[position]);
            return imageView;
        }

        // references to our images
        private Integer[] mThumbIds = {
                R.drawable.ic_bar,
                R.drawable.ic_shopping,
                R.drawable.ic_cinema,
                R.drawable.ic_play,
                R.drawable.ic_restaurant,
                R.drawable.ic_cafe,
                R.drawable.ic_hotel,
                R.drawable.ic_hospital,
                R.drawable.ic_gas,
                R.drawable.ic_taxi,
                R.drawable.ic_bus,
                R.drawable.ic_airport,
                R.drawable.ic_atm,
                R.drawable.ic_account_balance_black_24dp,
                R.drawable.ic_icon,
                R.drawable.ic_university,
        };
    }