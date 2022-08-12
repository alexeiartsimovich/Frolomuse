package com.frolo.muse.player.service.observers;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.frolo.muse.R;
import com.frolo.threads.ThreadStrictMode;
import com.frolo.core.ui.glide.GlideAlbumArtHelper;
import com.frolo.core.ui.glide.SimpleRequestListener;
import com.frolo.music.model.Song;

import java.util.concurrent.Future;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


final class Arts {

    private static final Bitmap EMPTY_BITMAP =
            Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8);

    @Nullable
    private static volatile Bitmap sDefaultArt = null;

    private static float dp2px(@NonNull Context context, float dp) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    @Nullable
    static Bitmap getDefault() {
        return sDefaultArt;
    }

    @MainThread
    @NonNull
    static Single<Bitmap> getPlaybackArt(@NonNull Context context, @Nullable final Song song) {
        ThreadStrictMode.assertMain();

        final int targetArtWidth = (int) dp2px(context, 200f);
        final int targetArtHeight = (int) dp2px(context, 200f);
        final long albumId = song != null ? song.getAlbumId() : -1L;
        final Future<Bitmap> artFuture = GlideAlbumArtHelper.get()
                .makeRequestAsBitmap(Glide.with(context), albumId)
                .override(targetArtWidth, targetArtHeight)
                .submit();

        final Future<Bitmap> defaultArtFuture = Glide.with(context)
                .asBitmap()
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .load(R.drawable.art_playback_notification)
                .override(targetArtWidth, targetArtHeight)
                .listener(new SimpleRequestListener<Bitmap>() {
                    @Override
                    public void doWhenReady(final Bitmap resource) {
                        if (resource != null) sDefaultArt = resource;
                    }
                })
                .submit();

        return Single.fromFuture(artFuture, Schedulers.io())
                .onErrorResumeNext(Single.fromFuture(defaultArtFuture, Schedulers.io()))
                .onErrorReturnItem(EMPTY_BITMAP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Arts() {
    }

}
