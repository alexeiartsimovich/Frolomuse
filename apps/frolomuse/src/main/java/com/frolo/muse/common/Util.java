package com.frolo.muse.common;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.MediaStore;

import com.frolo.player.AudioMetadata;
import com.frolo.player.AudioSource;
import com.frolo.player.AudioType;
import com.frolo.player.data.MediaStoreRow;
import com.frolo.music.model.Media;
import com.frolo.music.model.Song;
import com.frolo.music.model.SongType;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public final class Util {

    private static final Uri CONTENT_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    @NotNull
    static SongType toSongType(@NotNull AudioType type) {
        switch (type) {
            case MUSIC:         return SongType.MUSIC;
            case PODCAST:       return SongType.PODCAST;
            case RINGTONE:      return SongType.RINGTONE;
            case ALARM:         return SongType.ALARM;
            case NOTIFICATION:  return SongType.NOTIFICATION;
            case AUDIOBOOK:     return SongType.AUDIOBOOK;
            default:            return SongType.MUSIC;
        }
    }

    @NotNull
    static AudioType toAudioType(@NotNull SongType type) {
        switch (type) {
            case MUSIC:         return AudioType.MUSIC;
            case PODCAST:       return AudioType.PODCAST;
            case RINGTONE:      return AudioType.RINGTONE;
            case ALARM:         return AudioType.ALARM;
            case NOTIFICATION:  return AudioType.NOTIFICATION;
            case AUDIOBOOK:     return AudioType.AUDIOBOOK;
            default:            return AudioType.MUSIC;
        }
    }

    private static final class AudioSourceFromSongDelegate
            implements Song, Media, AudioSource, AudioMetadata, MediaStoreRow {

        private final Song mDelegate;

        AudioSourceFromSongDelegate(Song delegate) {
            mDelegate = delegate;
        }

        @NotNull
        @Override
        public AudioMetadata getMetadata() {
            return this;
        }

        @Override
        public AudioType getAudioType() {
            return toAudioType(mDelegate.getSongType());
        }

        @Override
        public SongType getSongType() {
            return mDelegate.getSongType();
        }

        @Override
        public String getSource() {
            return mDelegate.getSource();
        }

        @Override
        public String getTitle() {
            return mDelegate.getTitle();
        }

        @Override
        public long getArtistId() {
            return mDelegate.getArtistId();
        }

        @Override
        public String getArtist() {
            return mDelegate.getArtist();
        }

        @Override
        public long getAlbumId() {
            return mDelegate.getAlbumId();
        }

        @Override
        public String getAlbum() {
            return mDelegate.getAlbum();
        }

        @Override
        public String getGenre() {
            return mDelegate.getGenre();
        }

        @Override
        public int getDuration() {
            return mDelegate.getDuration();
        }

        @Override
        public int getYear() {
            return mDelegate.getYear();
        }

        @Override
        public int getTrackNumber() {
            return mDelegate.getTrackNumber();
        }

        @Override
        public long getId() {
            return mDelegate.getId();
        }

        @Override
        public int getKind() {
            return mDelegate.getKind();
        }

        @NotNull
        @Override
        public Uri getUri() {
            return ContentUris.withAppendedId(CONTENT_URI, mDelegate.getId());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null) return false;
            if (!(o instanceof AudioSourceFromSongDelegate)) return false;
            final AudioSourceFromSongDelegate other = (AudioSourceFromSongDelegate) o;
            return Objects.equals(mDelegate, other.mDelegate);
        }

    }

    private static final class SongFromAudioSourceDelegate
            implements Song, Media, AudioSource, AudioMetadata, MediaStoreRow {

        private final AudioSource mDelegate;

        SongFromAudioSourceDelegate(AudioSource delegate) {
            mDelegate = delegate;
        }

        @Override
        public AudioType getAudioType() {
            return mDelegate.getMetadata().getAudioType();
        }

        @Override
        public SongType getSongType() {
            return toSongType(mDelegate.getMetadata().getAudioType());
        }

        @NotNull
        @Override
        public AudioMetadata getMetadata() {
            return mDelegate.getMetadata();
        }

        @Override
        public String getSource() {
            return mDelegate.getSource();
        }

        @Override
        public String getTitle() {
            return mDelegate.getMetadata().getTitle();
        }

        @Override
        public long getArtistId() {
            return mDelegate.getMetadata().getArtistId();
        }

        @Override
        public String getArtist() {
            return mDelegate.getMetadata().getArtist();
        }

        @Override
        public long getAlbumId() {
            return mDelegate.getMetadata().getAlbumId();
        }

        @Override
        public String getAlbum() {
            return mDelegate.getMetadata().getAlbum();
        }

        @Override
        public String getGenre() {
            return mDelegate.getMetadata().getGenre();
        }

        @Override
        public int getDuration() {
            return mDelegate.getMetadata().getDuration();
        }

        @Override
        public int getYear() {
            return mDelegate.getMetadata().getYear();
        }

        @Override
        public int getTrackNumber() {
            return mDelegate.getMetadata().getTrackNumber();
        }

        @Override
        public long getId() {
            return mDelegate.getId();
        }

        @Override
        public int getKind() {
            return Song.SONG;
        }

        @NotNull
        @Override
        public Uri getUri() {
            return ContentUris.withAppendedId(CONTENT_URI, mDelegate.getId());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null) return false;
            if (!(o instanceof SongFromAudioSourceDelegate)) return false;
            final SongFromAudioSourceDelegate other = (SongFromAudioSourceDelegate) o;
            return Objects.equals(mDelegate, other.mDelegate);
        }
    }

    @NotNull
    public static AudioSource createAudioSource(@NotNull Song song) {
        return new AudioSourceFromSongDelegate(song);
    }

    @NotNull
    public static Song createSong(@NotNull AudioSource audioSource) {
        return new SongFromAudioSourceDelegate(audioSource);
    }

    public static List<AudioSource> createAudioSourceList(List<Song> songs) {
        if (songs == null || songs.isEmpty())
            return Collections.emptyList();

        final List<AudioSource> items = new ArrayList<>(songs.size());
        for (Song song : songs) {
            items.add(createAudioSource(song));
        }

        return items;
    }

    public static List<Song> createSongList(List<AudioSource> audioSources) {
        if (audioSources == null || audioSources.isEmpty())
            return Collections.emptyList();

        final List<Song> items = new ArrayList<>(audioSources.size());
        for (AudioSource audioSource : audioSources) {
            items.add(createSong(audioSource));
        }

        return items;
    }

    private Util() {
    }

}
