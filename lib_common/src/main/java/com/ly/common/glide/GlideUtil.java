package com.ly.common.glide;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.ly.common.R;

/**
 * Created by ly on 2021/3/24 16:37
 */
public class GlideUtil {

    public static void load(Context context, String url, @NonNull ImageView image) {
        GlideApp.with(context)
                .load(url)
                .transition(DrawableTransitionOptions.withCrossFade(5000))
                .apply(getOptions())
                .into(image);
    }

    private static RequestOptions getOptions() {
        return new RequestOptions().centerCrop()
                .error(R.color.red_tips);
    }
}
