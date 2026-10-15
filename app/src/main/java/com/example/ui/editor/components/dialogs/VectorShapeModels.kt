package com.example.ui.editor.components.dialogs

data class ShapeDefinition(val type: String, val label: String, val category: String)

object VectorShapePresets {
    val shapes = listOf(
        ShapeDefinition("RECTANGLE", "Rectangle", "Geometric"),
        ShapeDefinition("ROUNDED_RECT", "Rounded Rect", "Geometric"),
        ShapeDefinition("OVAL", "Oval / Circle", "Geometric"),
        ShapeDefinition("STAR", "5-Star", "Geometric"),
        ShapeDefinition("DIAMOND", "Diamond", "Geometric"),
        ShapeDefinition("ARROW_RIGHT", "Right Arrow", "Geometric"),
        ShapeDefinition("ARROW_LEFT", "Left Arrow", "Geometric"),
        ShapeDefinition("CALLOUT", "Speech Bubble", "Geometric"),
        ShapeDefinition("NOTE", "Note Box", "Callout"),
        ShapeDefinition("WARNING", "Warning", "Callout"),
        ShapeDefinition("SUCCESS", "Tip / Key", "Callout"),
        ShapeDefinition("QUOTE", "Quote Box", "Callout")
    )

    val colorThemes = listOf(
        Triple("#EFF6FF", "#3B82F6", "Blue"),
        Triple("#ECFDF5", "#10B981", "Emerald"),
        Triple("#FEF3C7", "#F59E0B", "Amber"),
        Triple("#FEF2F2", "#EF4444", "Crimson"),
        Triple("#F5F3FF", "#8B5CF6", "Purple"),
        Triple("#F0FDF4", "#059669", "Mint"),
        Triple("#F8FAFC", "#475569", "Slate"),
        Triple("#FFFFFF", "#1E293B", "Clean White")
    )
}
