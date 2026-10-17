package com.example.ui.editor.components.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.editor.EditorViewModel

/**
 * Microsoft Word Mobile Shapes Submenu.
 * Displays categorized shape collections with gray header bars.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BottomRibbonInsertShapesSubmenu(
    viewModel: EditorViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        "Lines" to listOf(
            WordShapeItem("LINE", "Line", "Lines"),
            WordShapeItem("ARROW_LINE", "Arrow", "Lines"),
            WordShapeItem("DOUBLE_ARROW", "Double Arrow", "Lines"),
            WordShapeItem("ELBOW", "Elbow Connector", "Lines"),
            WordShapeItem("CURVE", "Curved Connector", "Lines")
        ),
        "Rectangles" to listOf(
            WordShapeItem("RECTANGLE", "Rectangle", "Rectangles"),
            WordShapeItem("ROUNDED_RECT", "Rounded Rectangle", "Rectangles"),
            WordShapeItem("SNIP_CORNER", "Single Corner Snipped", "Rectangles"),
            WordShapeItem("SNIP_TOP", "Top Corners Snipped", "Rectangles"),
            WordShapeItem("SNIP_DIAG", "Diagonal Corners Snipped", "Rectangles"),
            WordShapeItem("ROUND_CORNER", "Single Corner Rounded", "Rectangles")
        ),
        "Basic Shapes" to listOf(
            WordShapeItem("OVAL", "Oval", "Basic Shapes"),
            WordShapeItem("TRIANGLE", "Isosceles Triangle", "Basic Shapes"),
            WordShapeItem("RIGHT_TRIANGLE", "Right Triangle", "Basic Shapes"),
            WordShapeItem("PARALLELOGRAM", "Parallelogram", "Basic Shapes"),
            WordShapeItem("TRAPEZOID", "Trapezoid", "Basic Shapes"),
            WordShapeItem("DIAMOND", "Diamond", "Basic Shapes"),
            WordShapeItem("PENTAGON", "Pentagon", "Basic Shapes"),
            WordShapeItem("HEXAGON", "Hexagon", "Basic Shapes"),
            WordShapeItem("OCTAGON", "Octagon", "Basic Shapes"),
            WordShapeItem("CROSS", "Cross", "Basic Shapes"),
            WordShapeItem("CYLINDER", "Cylinder", "Basic Shapes"),
            WordShapeItem("CUBE", "Cube", "Basic Shapes"),
            WordShapeItem("HEART", "Heart", "Basic Shapes"),
            WordShapeItem("LIGHTNING", "Lightning", "Basic Shapes"),
            WordShapeItem("SUN", "Sun", "Basic Shapes"),
            WordShapeItem("MOON", "Moon", "Basic Shapes"),
            WordShapeItem("CLOUD", "Cloud", "Basic Shapes")
        ),
        "Block Arrows" to listOf(
            WordShapeItem("ARROW_RIGHT", "Right Arrow", "Block Arrows"),
            WordShapeItem("ARROW_LEFT", "Left Arrow", "Block Arrows"),
            WordShapeItem("ARROW_UP", "Up Arrow", "Block Arrows"),
            WordShapeItem("ARROW_DOWN", "Down Arrow", "Block Arrows"),
            WordShapeItem("ARROW_LEFTRIGHT", "Left-Right Arrow", "Block Arrows"),
            WordShapeItem("ARROW_UPDOWN", "Up-Down Arrow", "Block Arrows"),
            WordShapeItem("ARROW_QUAD", "Quad Arrow", "Block Arrows"),
            WordShapeItem("ARROW_UTURN", "U-Turn Arrow", "Block Arrows")
        ),
        "Equation Shapes" to listOf(
            WordShapeItem("PLUS", "Plus", "Equation Shapes"),
            WordShapeItem("MINUS", "Minus", "Equation Shapes"),
            WordShapeItem("MULTIPLY", "Multiply", "Equation Shapes"),
            WordShapeItem("DIVIDE", "Divide", "Equation Shapes"),
            WordShapeItem("EQUAL", "Equal", "Equation Shapes"),
            WordShapeItem("NOT_EQUAL", "Not Equal", "Equation Shapes")
        ),
        "Flowchart" to listOf(
            WordShapeItem("FLOW_PROCESS", "Process", "Flowchart"),
            WordShapeItem("FLOW_TERM", "Alternate Process", "Flowchart"),
            WordShapeItem("FLOW_DECISION", "Decision", "Flowchart"),
            WordShapeItem("FLOW_DATA", "Data", "Flowchart"),
            WordShapeItem("FLOW_CONN", "Connector", "Flowchart")
        ),
        "Stars and Banners" to listOf(
            WordShapeItem("STAR_4", "4-Point Star", "Stars and Banners"),
            WordShapeItem("STAR", "5-Point Star", "Stars and Banners"),
            WordShapeItem("STAR_6", "6-Point Star", "Stars and Banners"),
            WordShapeItem("STAR_8", "8-Point Star", "Stars and Banners"),
            WordShapeItem("STAR_12", "12-Point Star", "Stars and Banners"),
            WordShapeItem("EXPLOSION", "Explosion", "Stars and Banners"),
            WordShapeItem("RIBBON_UP", "Up Ribbon", "Stars and Banners"),
            WordShapeItem("RIBBON_DOWN", "Down Ribbon", "Stars and Banners")
        ),
        "Callouts" to listOf(
            WordShapeItem("CALLOUT_RECT", "Rectangular Callout", "Callouts"),
            WordShapeItem("CALLOUT_ROUND", "Rounded Callout", "Callouts"),
            WordShapeItem("CALLOUT_OVAL", "Oval Callout", "Callouts"),
            WordShapeItem("CALLOUT_CLOUD", "Cloud Callout", "Callouts")
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        categories.forEach { (catName, shapes) ->
            // Grey Category Header Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEEEEEE))
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Text(
                    text = catName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666)
                )
            }

            // Grid / Flow of Shapes
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                shapes.forEach { shape ->
                    WordShapeThumbnail(
                        shape = shape,
                        onClick = {
                            viewModel.insertShape(
                                shapeType = shape.type,
                                title = shape.label,
                                content = "",
                                bgHex = "#41719C",
                                borderHex = "#1B365D"
                            )
                            viewModel.showStatusMessage("${shape.label} shape inserted")
                            onBack()
                        }
                    )
                }
            }
        }
    }
}
