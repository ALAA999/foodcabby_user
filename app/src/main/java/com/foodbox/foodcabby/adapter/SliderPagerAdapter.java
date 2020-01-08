package com.foodbox.foodcabby.adapter;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.foodbox.foodcabby.fragments.SliderDialogFragment;
import com.foodbox.foodcabby.R;
import com.foodbox.foodcabby.models.Image;

import java.util.List;

/**
 * Created  on 09-11-2017.
 */

public class SliderPagerAdapter extends PagerAdapter {
    private Activity activity;
    private List<Image> image_arraylist;
    private Boolean isClickable = false;
    private ImageView im_slider;

    public SliderPagerAdapter(Activity activity, List<Image> image_arraylist, Boolean isClickable) {
        this.activity = activity;
        this.image_arraylist = image_arraylist;
        this.isClickable = isClickable;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = layoutInflater.inflate(R.layout.layout_slider, container, false);
        im_slider = view.findViewById(R.id.im_slider);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_restaurant_place_holder)
                .error(R.drawable.ic_restaurant_place_holder)
                .priority(Priority.HIGH);

        Glide
                .with(activity.getApplicationContext())
                .asBitmap()
                .load(image_arraylist.get(position).getUrl())
                .listener(requestListener)
                .apply(options)
                .into(im_slider);



        if (isClickable) {
            im_slider.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager manager = activity.getFragmentManager();
                    SliderDialogFragment sliderDialogFragment = new SliderDialogFragment();
                    sliderDialogFragment.show(manager, "slider_dialog");
                    Log.e("im_slider",image_arraylist.get(0).getUrl());
                }
            });
        }
        container.addView(view);

        return view;
    }

    private RequestListener<Bitmap> requestListener = new RequestListener<Bitmap>() {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
            // todo log exception to central service or something like that
            Log.e("im_slider",e.getMessage());
            // important to return false so the error placeholder can be placed
            return false;
        }

        @Override
        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
            // everything worked out, so probably nothing to do
            Log.e("im_slider","onResourceReady");
            im_slider.setImageBitmap(resource);
            return false;
        }
    };

    @Override
    public int getCount() {
        return image_arraylist.size();
    }


    @Override
    public boolean isViewFromObject(View view, Object obj) {
        return view == obj;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = (View) object;
        container.removeView(view);
    }
}
