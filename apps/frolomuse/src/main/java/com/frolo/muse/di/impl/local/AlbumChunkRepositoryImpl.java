package com.frolo.muse.di.impl.local;

import com.frolo.muse.R;
import com.frolo.music.model.SortOrder;
import com.frolo.music.repository.AlbumChunkRepository;

import java.util.List;


public class AlbumChunkRepositoryImpl extends SongRepositoryImpl implements AlbumChunkRepository {

    private final static String[] SORT_ORDER_KEYS = {
        SongQuery.Sort.BY_DEFAULT,
        SongQuery.Sort.BY_TITLE,
        SongQuery.Sort.BY_ARTIST,
        SongQuery.Sort.BY_DURATION,
        SongQuery.Sort.BY_DATE_ADDED
    };

    static String getSortOrderOrDefault(String candidate) {
        return Preconditions.takeIfNotNullAndListedOrDefault(candidate, SORT_ORDER_KEYS, SongQuery.Sort.BY_DEFAULT);
    }

    public AlbumChunkRepositoryImpl(LibraryConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected List<SortOrder> blockingGetSortOrders() {
        return collectSortOrders(
            createSortOrder(SongQuery.Sort.BY_DEFAULT, R.string.sort_by_default),
            createSortOrder(SongQuery.Sort.BY_TITLE, R.string.sort_by_name),
            createSortOrder(SongQuery.Sort.BY_ARTIST, R.string.sort_by_artist),
            createSortOrder(SongQuery.Sort.BY_DURATION, R.string.sort_by_duration),
            createSortOrder(SongQuery.Sort.BY_DATE_ADDED, R.string.sort_by_date_added)
        );
    }

}
