package com.example.ui.editor

/**
 * Ribbon tab selection representing Word toolbar panels.
 */
enum class RibbonTab {
    HOME,
    INSERT,
    DRAW,
    LAYOUT,
    REVIEW,
    VIEW
}

/**
 * View presentation mode: Paginated print preview vs continuous mobile scroll.
 */
enum class EditorViewMode {
    PAGINATED_A4,
    CONTINUOUS_MOBILE,
    WEB_LAYOUT
}

typealias EditorViewModelFactory = EditorViewModel.Factory
