package com.apero.app.poc_ml_docscan.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.nonEmptyListOf
import arrow.core.toNonEmptyListOrNull
import com.apero.app.poc_ml_docscan.edit.model.FilterMode
import com.apero.app.poc_ml_docscan.edit.model.PagePreviewUiModel
import com.apero.app.poc_ml_docscan.image_processing.model.ResizeTransformation
import com.apero.app.poc_ml_docscan.model.PdfPageId
import com.apero.app.poc_ml_docscan.scan.common.model.InternalImage
import com.apero.app.poc_ml_docscan.scan.common.model.rotation
import com.apero.app.poc_ml_docscan.utils.RotateTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import timber.log.Timber

class EditViewModel:ViewModel() {
    private val _editUiState =
        MutableStateFlow(DocumentEditUiState())
    val editUiState = _editUiState.asStateFlow()
    init {
//        val currentPagePreviewState = editUiState.map { it.pagePreview }
//            .filterNotNull()
//            .distinctUntilChangedBy { Pair(it.pageId, it.internalImage) }
//
//        val currentFilterPreviewState = editUiState
//            .map { state -> state.listFilterPreview.find { it.isChecked } }
//            .filterNotNull()
//            .distinctUntilChanged()
//
//        val currentToolTypeState = editUiState.map { it.currentToolType }
//
//        combine(
//            currentPagePreviewState,
//            currentFilterPreviewState,
//            currentToolTypeState
//                .filter { it == EditToolType.FILTER }
//                .distinctUntilChanged()
//        ) { pagePreview, currentFilter, _ ->
//            if (pagePreview.pageId == currentFilter.pageId)
//                pagePreview to currentFilter
//            else null
//        }
//            .filterNotNull()
//            .distinctUntilChangedBy { (pagePreview, currentFilter) ->
//                Pair(pagePreview.internalImage, currentFilter.filterMode)
//            }
//            .onEach { (pagePreview, currentFilter) ->
//                Timber.d("updateImageFromFilter: pagePreview\$filterSelected: ${pagePreview.filterSelected},currentFilter:${currentFilter}")
////                updateImageFromFilter(pagePreview, currentFilter.filterMode)
//            }.launchIn(viewModelScope)
//
//        combine(
//            currentPagePreviewState,
//            currentToolTypeState
//                .filter { it == EditToolType.FILTER }
//                .distinctUntilChanged()
//        ) { pagePreview, _ ->
//            pagePreview
//        }
//            .distinctUntilChangedBy { pagePreview -> pagePreview.pageId to pagePreview.internalImage }
//            .onEach { pagePreview ->
//                Timber.d("updateFilterListFromPage: pagePreview\$filterSelected:${pagePreview.filterSelected}")
////                updateFilterListFromPage(pagePreview)
//            }.launchIn(viewModelScope)
    }
    fun fillPagePreview(pages: List<InternalImage>) {
        val listPagePreview = pages.mapIndexed { index, internalImage ->
            PagePreviewUiModel(
                internalImage,
                null,
                PdfPageId(index.toString()),
                FilterMode.NO_SHADOW,
                rotateDegrees = 0f
            )
        }
        val currentPageIndex = if (pages.isEmpty()) -1 else 0
        _editUiState.updateAndGet {
            it.copy(
                listPagePreview = listPagePreview,
                currentPageIndex = currentPageIndex,
            )
        }
    }

    fun nextPage() {
        _editUiState.update { it.updateNextPage() }
    }

    fun previousPage() {
        _editUiState.update { it.updatePrevPage() }
    }

    fun updateCurrentPage(position: Int) {
        _editUiState.update { it.updatePage(position) }
    }
//    fun updateFilter(filter: FilterUiModel) {
//        _editUiState.update { state -> state.updateFilter(filter) }
//    }
//
//    fun toggleFilterApplyForAll() {
//        _editUiState.update { it.applyForAllFilter(it.isApplyFilterForAll.not()) }
//    }
//
//    private var jobUploadFilterListFromPage: Job? = null
//    private fun updateFilterListFromPage(pagePreview: PagePreviewUiModel) {
//        jobUploadFilterListFromPage?.cancel()
//        jobUploadFilterListFromPage = null
//        _editUiState.updateAndGet {
//            it.copy(listFilterPreview = pagePreview.generateListFilter())
//        }.let {
//            viewModelScope.launch {
//                FilterMode.entries.map { filter ->
//                    launch(Dispatchers.IO) {
//                        val filterData = useCaseApplyImageTransformations(
//                            pagePreview.internalImage, true, listOfNotNull(
//                                RotateTransformation(pagePreview.internalImage.rotation.toFloat()),
//                                ResizeTransformation.MaxLength(0.5f),
//                                filter.mapToTransformation()
//                            ).toNonEmptyListOrNull() ?: nonEmptyListOf(
//                                RotateTransformation(pagePreview.internalImage.rotation.toFloat()),
//                                ResizeTransformation.MaxLength(0.5f)
//                            )
//                        ).getOrNull()
//                        yield()
//                        _editUiState.updateAndGet { state ->
//                            state.copy(listFilterPreview = state.listFilterPreview.map {
//                                val imagePreview = if (it.filterMode == filter) (filterData
//                                    ?: it.imagePreview) else it.imagePreview
//                                it.copy(imagePreview = imagePreview)
//                            })
//                        }.let { state ->
//                            if (state.pagePreview?.imagePreview == null && state.pagePreview?.filterSelected == filter) {
//                                _editUiState.update { editState ->
//                                    editState.copy(listPagePreview = editState.listPagePreview.map { pagePreview ->
//                                        if (pagePreview == state.pagePreview) {
//                                            pagePreview.copy(imagePreview = filterData)
//                                        } else pagePreview
//                                    }
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }.let {
//            jobUploadFilterListFromPage = it
//        }
//    }
//
//    private var jobUploadImageFromFilter: Job? = null
//    private fun updateImageFromFilter(pagePreview: PagePreviewUiModel, filterSelected: FilterMode) {
//        jobUploadImageFromFilter?.cancel()
//        jobUploadImageFromFilter = null
//        viewModelScope.launch {
//            launch(Dispatchers.IO) {
//                val previewWithFilter = useCaseApplyImageTransformations(
//                    pagePreview.internalImage, true, listOfNotNull(
//                        RotateTransformation(pagePreview.internalImage.rotation.toFloat()),
//                        ResizeTransformation.MaxLength(2f),
//                        filterSelected.mapToTransformation()
//                    ).toNonEmptyListOrNull() ?: nonEmptyListOf(
//                        RotateTransformation(pagePreview.internalImage.rotation.toFloat()),
//                        ResizeTransformation.MaxLength(2f)
//                    )
//                ).getOrNull()
//                yield()
//                _editUiState.update { state ->
//                    state.copy(listPagePreview = state.listPagePreview.mapIndexed { index, pagePreviewUiModel ->
//                        if (index == state.currentPageIndex) {
//                            pagePreviewUiModel.copy(
//                                imagePreview = previewWithFilter ?: pagePreviewUiModel.internalImage
//                            )
//                        } else pagePreviewUiModel
//                    })
//                }
//            }
//        }.let {
//            jobUploadImageFromFilter = it
//        }
//    }

//    private fun PagePreviewUiModel.generateListFilter(): List<FilterUiModel> {
//        return FilterMode.entries.map {
//            FilterUiModel(
//                filterMode = it,
//                imagePreview = internalImage,
//                pageId = pageId,
//                isChecked = filterSelected == it
//            )
//        }
//    }
//
//    fun toggleUpdateToolType(toolType: EditToolType) {
//        _editUiState.updateAndGet {
//            it.copy(currentToolType = if (it.currentToolType == toolType) null else toolType)
//        }
//    }

    fun toggleRotate() {
        _editUiState.update { it.toggleRotate() }
    }
}
data class DocumentEditUiState(
    val listPagePreview: List<PagePreviewUiModel> = emptyList(),
    val currentPageIndex: Int = -1,
//    val listFilterPreview: List<FilterUiModel> = emptyList(),
//    val currentToolType: EditToolType? = null,
    val isApplyFilterForAll: Boolean = false,
) {
    val isEnablePrevPage: Boolean get() = currentPageIndex > 0
    val isEnableNextPage: Boolean get() = currentPageIndex < listPagePreview.size - 1
    val pagePreview get() = listPagePreview.getOrNull(currentPageIndex)

    fun updateNextPage(): DocumentEditUiState {
        return updatePage(currentPageIndex.inc())
    }

    fun updatePrevPage(): DocumentEditUiState {
        return updatePage(currentPageIndex.dec())
    }

    fun updatePage(page: Int): DocumentEditUiState {
        val currentPagePreview = page.coerceIn(0, listPagePreview.size)
        return copy(currentPageIndex = currentPagePreview)
    }

//    fun updateFilter(filter: FilterUiModel): DocumentEditUiState {
//        val state = this
//        return state.copy(listFilterPreview = state.listFilterPreview.map {
//            it.copy(isChecked = it.filterMode == filter.filterMode)
//        }, listPagePreview = state.listPagePreview.map {
//            if (it.pageId == filter.pageId) {
//                it.copy(
//                    filterSelected = filter.filterMode,
//                    imagePreview = it.imagePreview ?: filter.imagePreview
//                )
//            } else it.copy(filterSelected = if (isApplyFilterForAll) filter.filterMode else it.filterSelected)
//        })
//    }

//    fun applyForAllFilter(isApplyForAll: Boolean): DocumentEditUiState {
//        val currentFilter = listFilterPreview.find { it.isChecked }?.filterMode
//        return let {
//            if (isApplyForAll && currentFilter != null) {
//                copy(listPagePreview = listPagePreview.map { it.copy(filterSelected = currentFilter) })
//            } else this
//        }.copy(isApplyFilterForAll = isApplyForAll)
//    }

    fun toggleRotate(): DocumentEditUiState {
        return copy(listPagePreview = listPagePreview.mapIndexed { index, pagePreview ->
            if (index == currentPageIndex) {
                pagePreview.copy(rotateDegrees = pagePreview.rotateDegrees.plus(90f))
            } else {
                pagePreview
            }
        })
    }
}
