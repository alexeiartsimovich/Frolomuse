package com.frolo.muse;

import android.os.Build;

import androidx.annotation.AnyThread;


@AnyThread
public final class Features {

    /**
     * Checks if the album editor feature is available.
     * For now, this feature only works on Android P and lower (API <= 28)
     * due to media store limitations in newer OS versions.
     * @return true if the album editor feature is available.
     */
    public static boolean isAlbumEditorFeatureAvailable() {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.P;
    }

    /**
     * Checks if the plain old file explorer feature is available.
     * Starting with Android 11, the all file access is not permitted to apps by default.
     * Thus, we can only use media file models provided by the MediaStore.
     * On older versions on Android, everything works as before.
     * @return true if the plain old file explorer feature is available.
     */
    public static boolean isPlainOldFileExplorerFeatureAvailable() {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q;
    }

    /**
     * This feature is currently only available for debug builds.
     * @return true if the language chooser feature is available.
     */
    public static boolean isLanguageChooserFeatureAvailable() {
        return BuildConfig.DEBUG;
    }

    /**
     * New playlist storage based on a local database owned and managed by the application.
     * We faced a lot of problems with the playlist storage managed by {@link android.provider.MediaStore}
     * on Android 10 and above, so it was decided to create our own database.
     * The new database is managed by {@link com.frolo.muse.di.impl.local.PlaylistDatabaseManager}.
     * @return true if the new playlist storage feature is available.
     */
    public static boolean isAppPlaylistStorageFeatureAvailable() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    private Features() {
    }
}
