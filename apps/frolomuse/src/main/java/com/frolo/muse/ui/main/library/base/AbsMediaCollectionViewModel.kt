package com.frolo.muse.ui.main.library.base

import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.frolo.arch.support.*
import com.frolo.muse.router.AppRouter
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetMediaUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logShortcutCreated
import com.frolo.muse.memory.MemoryWatcher
import com.frolo.muse.model.event.DeletionConfirmation
import com.frolo.muse.model.event.DeletionType
import com.frolo.muse.model.event.MultipleDeletionConfirmation
import com.frolo.muse.model.menu.ContextualMenu
import com.frolo.muse.model.menu.OptionsMenu
import com.frolo.muse.model.menu.SortOrderMenu
import com.frolo.music.model.SortOrder
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import com.frolo.music.model.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import org.jetbrains.annotations.TestOnly
import org.reactivestreams.Subscription


abstract class AbsMediaCollectionViewModel<E: Media> constructor(
    private val permissionChecker: PermissionChecker,
    private val getMediaUseCase: GetMediaUseCase<E>,
    private val getMediaMenuUseCase: GetMediaMenuUseCase<E>,
    private val clickMediaUseCase: ClickMediaUseCase<E>,
    private val playMediaUseCase: PlayMediaUseCase<E>,
    private val shareMediaUseCase: ShareMediaUseCase<E>,
    private val deleteMediaUseCase: DeleteMediaUseCase<E>,
    private val getIsFavouriteUseCase: GetIsFavouriteUseCase<E>,
    private val changeFavouriteUseCase: ChangeFavouriteUseCase<E>,
    private val createShortcutUseCase: CreateShortcutUseCase<E>,
    private val schedulerProvider: SchedulerProvider,
    private val appRouter: AppRouter,
    private val eventLogger: EventLogger
): BaseViewModel(eventLogger), MemoryWatcher {

    // Private flags

    /**
     * The following flag indicates if the view model got activated to fetch the media list.
     * It's controlled by the [onActive] method,
     * that should be called when any UI controller, that owns this view model, gets active.
     */
    private var _activated = false
    private var _mediaListFetched = false

    // Subscriptions and disposables
    private var mediaListSubscription: Subscription? = null
    private var lastContextualDisposable: Disposable? = null

    // Permission
    private val _askReadPermissionEvent = EventLiveData<Unit>()
    //val askReadPermissionEvent: LiveData<Unit> = _askReadPermissionEvent

    // Common
    private val _deletedItemsEvent: MutableLiveData<List<E>> = SingleLiveEvent()
    val deletedItemsEvent: LiveData<List<E>> = _deletedItemsEvent

    // Sort order menu
    private val _openSortOrderMenuEvent: MutableLiveData<SortOrderMenu> = SingleLiveEvent()
    val openSortOrderMenuEvent: LiveData<SortOrderMenu> = _openSortOrderMenuEvent

    // Media collection (data, loading, placeholder)
    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _mediaList: MutableLiveData<List<E>> = MutableLiveData()
    val mediaList: LiveData<List<E>> get() = _mediaList

    val mediaItemCount: LiveData<Int> by lazy {
        mediaList.map(initialValue = 0) { list -> list?.count() ?: 0 }
    }

    val placeholderVisible: LiveData<Boolean> by lazy {
        combine(isLoading, mediaList) { loading: Boolean?, list: List<*>? ->
            list.isNullOrEmpty() && loading != true
        }
    }

    // Options menu
    private val _openOptionsMenuEvent: MutableLiveData<OptionsMenu<E>> = SingleLiveEvent()
    val openOptionsMenuEvent: LiveData<OptionsMenu<E>> = _openOptionsMenuEvent

    private val _closeOptionsMenuEvent: MutableLiveData<OptionsMenu<E>> = SingleLiveEvent()
    val closeOptionsMenuEvent: LiveData<OptionsMenu<E>> = _closeOptionsMenuEvent

    private val _optionsMenuItemFavourite: MutableLiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(_openOptionsMenuEvent) { optionsMenu ->
            value = optionsMenu.isFavourite
        }
    }
    val optionsMenuItemFavourite: LiveData<Boolean> = _optionsMenuItemFavourite

    private val _isProcessingOption: MutableLiveData<Boolean> = MutableLiveData()
    val isProcessingOption: LiveData<Boolean> = _isProcessingOption

    private val _addedNextToQueue: MutableLiveData<Unit> = SingleLiveEvent()
    val addedNextToQueue: LiveData<Unit> = _addedNextToQueue

    private val _addedToQueue: MutableLiveData<Unit> = SingleLiveEvent()
    val addedToQueue: LiveData<Unit> = _addedToQueue

    private val _confirmShortcutCreationEvent = SingleLiveEvent<E>()
    val confirmShortcutCreationEvent: LiveData<E> get() = _confirmShortcutCreationEvent

    // Contextual
    private val _openContextualMenuEvent: MutableLiveData<ContextualMenu<E>> = SingleLiveEvent()
    val openContextualMenuEvent: LiveData<ContextualMenu<E>> = _openContextualMenuEvent

    private val _selectedItems: MutableLiveData<Set<E>> by lazy {
        MediatorLiveData<Set<E>>().apply {
            addSource(mediaList) { list ->
                // clean selected items every time the media list updates?
                value = emptySet()
            }
            addSource(openContextualMenuEvent) { contextualMenu ->
                value = linkedSetOf(contextualMenu.targetItem)
            }
        }
    }
    val selectedItems: LiveData<Set<E>> get() = _selectedItems

    val selectedItemsCount: LiveData<Int> = Transformations.map(selectedItems) { selectedItems ->
        selectedItems.count()
    }

    val isInContextualMode: LiveData<Boolean> = Transformations.map(selectedItems) { items ->
        items != null && items.isNotEmpty()
    }

    private val _isProcessingContextual: MutableLiveData<Boolean> = MutableLiveData()
    val isProcessingContextual: LiveData<Boolean> = _isProcessingContextual

    // Deletion confirmation
    private val _confirmDeletionEvent: MutableLiveData<DeletionConfirmation<E>> = SingleLiveEvent()
    val confirmDeletionEvent: LiveData<DeletionConfirmation<E>> = _confirmDeletionEvent

    private val _confirmMultipleDeletionEvent: MutableLiveData<MultipleDeletionConfirmation<E>> = SingleLiveEvent()
    val confirmMultipleDeletionEvent: LiveData<MultipleDeletionConfirmation<E>> = _confirmMultipleDeletionEvent

    init {
        _isLoading.value = false
        _selectedItems.value = emptySet()
        _isProcessingOption.value = false
        _isProcessingContextual.value = false
    }

    private fun findAssociatedMediaItem(): Media? = (this as? AssociatedWithMediaItem)?.associatedMediaItem

    protected fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    protected fun submitMediaList(list: List<E>) {
        _mediaList.value = list
    }

    protected fun askReadPermission() {
        _askReadPermissionEvent.call()
    }

    /********************************
     ******* PERMISSION EVENTS ******
     *******************************/

    /**
     * Called when the Read-External-Storage permission,
     * that was requested by THIS UI component, is granted.
     */
    fun onReadPermissionGranted() {
        doFetch()
    }

    /**
     * Called when the Read-External-Storage permission,
     * that was requested by a UI component OTHER THAN THIS, is granted.
     */
    fun onReadPermissionGrantedSomewhere() {
        if (_activated && !_mediaListFetched) {
            doFetch()
        }
    }

    /********************************
     *********** FETCHING ***********
     *******************************/

    fun onSortOrderOptionSelected() {
        getMediaUseCase.getSortOrderMenu()
                .observeOn(schedulerProvider.main())
                .subscribeFor { sortOrderMenu ->
                    _openSortOrderMenuEvent.value = sortOrderMenu
                }
    }

    fun onSortOrderSelected(sortOrder: SortOrder) {
        getMediaUseCase.applySortOrder(sortOrder)
                .observeOn(schedulerProvider.main())
                .subscribeFor {  }
    }

    fun onSortOrderReversedChanged(reversed: Boolean) {
        getMediaUseCase.applySortOrderReversed(reversed)
                .observeOn(schedulerProvider.main())
                .subscribeFor {  }
    }

    //region Media list subscription
    protected fun requireSubscription() {
        doFetch()
    }

    protected fun cancelSubscription() {
        mediaListSubscription?.cancel()
    }

    /**
     * Convenient method for fetching media collection.
     * This properly dispatches subscription's events to other live data members.
     */
    private fun doFetch() {
        if (!permissionChecker.isQueryMediaContentPermissionGranted) {
            askReadPermission()
            return
        }

        getMediaUseCase.getMediaList()
            .observeOn(schedulerProvider.computation())
            .doOnNext { list ->
                openOptionsMenuEvent.value?.also { safeMenu ->
                    if (!list.contains(safeMenu.item)) {
                        _closeOptionsMenuEvent.postValue(safeMenu)
                    }
                }
            }
            .observeOn(schedulerProvider.main())
            .doOnSubscribe { subscription ->
                mediaListSubscription?.cancel()
                mediaListSubscription = subscription
                if (_mediaList.value.isNullOrEmpty()) {
                    _isLoading.value = true
                }
            }
            .doOnNext {
                _mediaListFetched = true
                _isLoading.value = false
            }
            .doOnTerminate {
                _isLoading.value = false
            }
            .subscribe(
                { list ->
                    _mediaList.value = list
                },
                { err ->
                    if (err is SecurityException) {
                        // TODO: do we need to ask permission?
                        askReadPermission()
                    } else {
                        logError(err)
                    }

                    if (_mediaList.value == null) {
                        _mediaList.value = emptyList()
                    }
                }
            )
            .save()
    }
    //endregion

    /********************************
     ******* LIFECYCLE EVENTS *******
     *******************************/

    @CallSuper
    internal open fun onStart() {
        if (_activated.not() || _mediaListFetched.not()) {
            _activated = true
            doFetch()
        }
    }

    @TestOnly
    internal fun onActive() {
        onStart()
    }

    @CallSuper
    internal open fun onStop() {
    }

    fun onContextualMenuDestroyed() {
        if (isInContextualMode.value == true) {
            _selectedItems.value = emptySet()
        }
    }

    /********************************
     ********** NAVIGATION **********
     *******************************/

    fun onBackArrowClicked() {
        handleNavigateUp()
    }

    fun onBackPressed() {
        handleBackPress()
    }

    protected open fun handleNavigateUp() {
        appRouter.goBack()
    }

    protected open fun handleBackPress() {
        appRouter.goBack()
    }

    /*******************************
     *********** CLICKS ************
     ******************************/

    protected open fun handleItemClick(item: E) {
        val list = _mediaList.value ?: emptyList()
        clickMediaUseCase.click(item, list, findAssociatedMediaItem())
                .observeOn(schedulerProvider.main())
                .subscribeFor {
                }
    }

    fun onItemClicked(item: E) {
        val contextualModeEnabled = isInContextualMode.value ?: false
        if (contextualModeEnabled) {
            onItemLongClicked(item)
        } else {
            handleItemClick(item)
        }
    }

    fun onItemLongClicked(item: E) {
        if (isInContextualMode.value == true) {
            val selectedItems = _selectedItems.value ?: linkedSetOf()
            val operator = Single.fromCallable {
                if (selectedItems.contains(item)) {
                    selectedItems - item
                } else {
                    selectedItems + item
                }
            }
            operator
                    .subscribeOn(schedulerProvider.computation())
                    .observeOn(schedulerProvider.main())
                    .subscribeFor { items: Set<E> -> _selectedItems.value = items }
        } else {
            getMediaMenuUseCase.getContextualMenu(item)
                    .observeOn(schedulerProvider.main())
                    .subscribeFor { menu ->
                        _openContextualMenuEvent.value = menu
                    }
        }
    }

    /*******************************
     ********* CONTEXTUAL **********
     ******************************/

    fun onSelectAllContextualOptionSelected() {
        val mediaList = mediaList.value ?: emptyList()
        Single.fromCallable { mediaList.toSet() }
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.main())
                .subscribeFor { mediaSet: Set<E> -> _selectedItems.value = mediaSet }
    }

    fun onScanFilesContextualOptionSelected() {
        val selectedItems = selectedItems.value ?: return
        doScanFiles(selectedItems)
                .observeOn(schedulerProvider.main())
                .doOnSubscribe { disposable ->
                    lastContextualDisposable = disposable
                    _selectedItems.value = emptySet()
                    _isProcessingContextual.value = true
                }
                .doFinally { _isProcessingContextual.value = false }
                .subscribeFor {  }
    }

    protected open fun doScanFiles(items: Set<E>): Completable {
        return Completable.error(UnsupportedOperationException())
    }

    fun onHideContextualOptionSelected() {
        val selectedItems = selectedItems.value ?: return
        doHide(selectedItems)
                .observeOn(schedulerProvider.main())
                .doOnSubscribe { disposable ->
                    lastContextualDisposable = disposable
                    _selectedItems.value = emptySet()
                    _isProcessingContextual.value = true
                }
                .doFinally { _isProcessingContextual.value = false }
                .subscribeFor {  }
    }

    protected open fun doHide(items: Set<E>): Completable {
        return Completable.error(UnsupportedOperationException())
    }

    fun onPlayContextualOptionSelected() {
        val selectedItems = selectedItems.value ?: return
        playMediaUseCase.play(selectedItems, findAssociatedMediaItem())
                .observeOn(schedulerProvider.main())
                .doOnSubscribe { disposable ->
                    lastContextualDisposable = disposable
                    _selectedItems.value = emptySet()
                    _isProcessingContextual.value = true
                }
                .doFinally { _isProcessingContextual.value = false }
                .subscribeFor {
                }
    }

    fun onPlayNextContextualOptionSelected() {
        val selectedItems = selectedItems.value ?: return
        playMediaUseCase.playNext(selectedItems)
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .doOnSubscribe { disposable ->
                    lastContextualDisposable = disposable
                    _selectedItems.value = emptySet()
                    _isProcessingContextual.value = true
                }
                .doFinally { _isProcessingContextual.value = false }
                .subscribeFor {
                    _addedNextToQueue.value = Unit
                }
    }

    fun onAddToQueueContextualOptionSelected() {
        val selectedItems = selectedItems.value ?: return
        playMediaUseCase.addToQueue(selectedItems)
                .observeOn(schedulerProvider.main())
                .doOnSubscribe { disposable ->
                    lastContextualDisposable = disposable
                    _selectedItems.value = emptySet()
                    _isProcessingContextual.value = true
                }
                .doFinally { _isProcessingContextual.value = false }
                .subscribeFor {
                    _addedToQueue.value = Unit
                }
    }

    fun onDeleteContextualOptionSelected() {
        val items = _selectedItems.value ?: return
        _confirmMultipleDeletionEvent.value = MultipleDeletionConfirmation(
            mediaItems = items.toList(),
            associatedMediaItem = findAssociatedMediaItem()
        )
    }

    fun onShareContextualOptionSelected() {
        val selectedItems = selectedItems.value ?: return
        shareMediaUseCase.share(selectedItems)
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .doOnSubscribe { disposable ->
                    lastContextualDisposable = disposable
                    _selectedItems.value = emptySet()
                    _isProcessingContextual.value = true
                }
                .doFinally { _isProcessingContextual.value = false }
                .subscribeFor {
                }
    }

    fun onAddToPlaylistContextualOptionSelected() {
        val selectedItems = selectedItems.value ?: return
        _selectedItems.value = emptySet()
        appRouter.addMediaItemsToPlaylist(ArrayList(selectedItems))
    }

    fun onContextualMenuClosed() {
        _selectedItems.value = emptySet()
    }

    fun onContextualDialogClosed() {
        lastContextualDisposable?.dispose()
        _isProcessingContextual.value = false
    }

    /******************************************
     *********** ITEM OPTIONS MENU ************
     *****************************************/

    fun onOptionsMenuClicked(item: E) {
        if (isInContextualMode.value == true) {
            // Options menu can be opened only if the model is NOT in contextual mode
            return
        }

        getMediaMenuUseCase
                .getOptionsMenu(item)
                .observeOn(schedulerProvider.main())
                .subscribeFor { optionsMenu ->
                    _openOptionsMenuEvent.value = optionsMenu
                }
    }

    fun onLikeOptionClicked() {
        val event = _openOptionsMenuEvent.value ?: return
        changeFavouriteUseCase.changeFavourite(event.item)
                .andThen(getIsFavouriteUseCase.isFavourite(event.item))
                .firstOrError()
                .observeOn(schedulerProvider.main())
                .subscribeFor { isFavourite ->
                    _optionsMenuItemFavourite.value = isFavourite
                }
    }

    fun onSetAsDefaultOptionSelected() {
        val event = _openOptionsMenuEvent.value ?: return
        _closeOptionsMenuEvent.value = event
        doSetAsDefault(event.item).subscribeFor {  }
    }

    protected open fun doSetAsDefault(item: E): Completable {
        return Completable.error(UnsupportedOperationException())
    }

    fun onHideOptionSelected() {
        val event = _openOptionsMenuEvent.value ?: return
        _closeOptionsMenuEvent.value = event
        doHide(event.item).subscribeFor {  }
    }

    protected open fun doHide(item: E): Completable {
        return Completable.error(UnsupportedOperationException())
    }

    fun onScanFilesOptionSelected() {
        val event = _openOptionsMenuEvent.value ?: return
        _closeOptionsMenuEvent.value = event
        doScanFiles(event.item).subscribeFor {  }
    }

    protected open fun doScanFiles(item: E): Completable {
        return Completable.error(UnsupportedOperationException())
    }

    fun onShareOptionSelected() {
        val event = _openOptionsMenuEvent.value ?: return
        _closeOptionsMenuEvent.value = event
        shareMediaUseCase.share(event.item)
                .observeOn(schedulerProvider.main())
                .subscribeFor {
                }
    }

    fun onDeleteOptionSelected() {
        val event = _openOptionsMenuEvent.value ?: return
        _closeOptionsMenuEvent.value = event
        _confirmDeletionEvent.value = DeletionConfirmation(
            mediaItem = event.item,
            associatedMediaItem = findAssociatedMediaItem()
        )
    }

    fun onPlayOptionSelected() {
        val event = _openOptionsMenuEvent.value ?: return
        _closeOptionsMenuEvent.value = event
        playMediaUseCase.play(event.item, findAssociatedMediaItem())
                .observeOn(schedulerProvider.main())
                .doOnSubscribe { _isProcessingOption.value = true }
                .doFinally { _isProcessingOption.value = false }
                .subscribeFor { }
    }

    fun onViewLyricsOptionSelected() {
        val event = _openOptionsMenuEvent.value ?: return
        _closeOptionsMenuEvent.value = event
        val item = event.item
        if (item is Song) {
            appRouter.viewLyrics(item)
        }
    }

    fun onViewAlbumOptionSelected() {
        val event = _openOptionsMenuEvent.value ?: return
        _closeOptionsMenuEvent.value = event
        val item = event.item
        if (item is Song) {
            val album = Album(item.albumId, item.album, item.artist, 1)
            appRouter.openAlbum(album)
        }
    }

    fun onViewArtistOptionSelected() {
        val event = _openOptionsMenuEvent.value ?: return
        _closeOptionsMenuEvent.value = event
        val item = event.item
        if (item is Song) {
            val artist = Artist(item.artistId, item.artist, 1, 1)
            appRouter.openArtist(artist)
        }
    }

    fun onViewGenreOptionSelected() {
        val event = _openOptionsMenuEvent.value ?: return
        _closeOptionsMenuEvent.value = event
        val item = event.item
        if (item is Song) {
            // Hot to view a genre???
        }
    }

    fun onEditOptionSelected() {
        val event = _openOptionsMenuEvent.value ?: return
        _closeOptionsMenuEvent.value = event
        val item = event.item
        when(item.kind) {
            Media.SONG -> appRouter.editSong(item as Song)
            Media.ALBUM -> appRouter.editAlbum(item as Album)
            Media.PLAYLIST -> appRouter.editPlaylist(item as Playlist)
            else -> { } // should not happen
        }
    }

    fun onPlayNextOptionSelected() {
        val event = _openOptionsMenuEvent.value ?: return
        _closeOptionsMenuEvent.value = event
        playMediaUseCase.playNext(event.item)
                .observeOn(schedulerProvider.main())
                .doOnSubscribe { _isProcessingOption.value = true }
                .doFinally { _isProcessingOption.value = false }
                .subscribeFor {
                    _addedNextToQueue.value = Unit
                }
    }

    fun onAddToQueueOptionSelected() {
        val event = _openOptionsMenuEvent.value ?: return
        _closeOptionsMenuEvent.value = event
        playMediaUseCase.addToQueue(event.item)
                .observeOn(schedulerProvider.main())
                .doOnSubscribe { _isProcessingOption.value = true }
                .doFinally { _isProcessingOption.value = false }
                .subscribeFor {
                    _addedToQueue.value = Unit
                }
    }

    fun onAddToPlaylistOptionSelected() {
        val event = _openOptionsMenuEvent.value ?: return
        _closeOptionsMenuEvent.value = event
        val item = event.item
        Single.fromCallable { ArrayList(listOf(item)) }
                .observeOn(schedulerProvider.main())
                .subscribeFor { items ->
                    appRouter.addMediaItemsToPlaylist(items)
                }
    }

    fun onCreateShortcutOptionSelected() {
        val event = _openOptionsMenuEvent.value ?: return
        _closeOptionsMenuEvent.value = event
        val item = event.item
        _confirmShortcutCreationEvent.value = item
    }

    fun onCreateShortcutOptionConfirmed(item: E) {
        createShortcutUseCase.createShortcut(item)
                .observeOn(schedulerProvider.main())
                .doOnComplete { eventLogger.logShortcutCreated(item.kind) }
                .subscribeFor { }
    }

    fun onConfirmedDeletion(item: E, type: DeletionType) {
        return deleteMediaUseCase.delete(item, type)
                .observeOn(schedulerProvider.main())
                .doOnSubscribe { _isProcessingOption.value = true }
                .doFinally { _isProcessingOption.value = false }
                .subscribeFor { _deletedItemsEvent.value = listOf(item) }
    }

    fun onConfirmedMultipleDeletion(items: List<E>, type: DeletionType) {
        return deleteMediaUseCase.delete(items, type)
                .observeOn(schedulerProvider.main())
                .doOnSubscribe { _isProcessingContextual.value = true }
                .doFinally { _isProcessingContextual.value = false }
                .subscribeFor {
                    _selectedItems.value = emptySet()
                    _deletedItemsEvent.value = items
                }
    }

    fun onRemoveFromCurrentQueueOptionSelected() {
        val event = _openOptionsMenuEvent.value ?: return
        _closeOptionsMenuEvent.value = event
        val item = event.item
        getMediaMenuUseCase.removeFromCurrentQueue(item)
                .observeOn(schedulerProvider.main())
                .subscribeFor { }
    }

    protected fun closeOptionsMenu() {
        val event = _openOptionsMenuEvent.value ?: return
        _closeOptionsMenuEvent.value = event
    }

    override fun noteLowMemory() {
        if (!mediaList.hasActiveObservers()) {
            cancelSubscription()
            _mediaList.value = null
            _mediaListFetched = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaListSubscription?.cancel()
        lastContextualDisposable?.dispose()
    }
}