/* This file is part of TableauApplet.
 *
 * It has been written by Mattias Ulbrich <ulbrich@kit.edu>, 
 * Karlsruhe Institute of Technology, Germany.
 *
 * TableauApplet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TableauApplet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TableauApplet.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.uka.ilkd.tableau.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import de.ukd.ilkd.tableau.Node
import de.ukd.ilkd.tableau.Type.*
import org.jetbrains.skia.Point
import kotlin.math.max

@Composable
fun NodeUI(root: Node) {
    val style = TextStyle(color = Color.Black, fontSize = 12.sp)
    val textMeasurer = rememberTextMeasurer()

    @Composable
    fun measureTextWidth(text: String, style: TextStyle): Pair<Float, Float> {
        val widthInPixels = textMeasurer.measure(text, style)
        with(LocalDensity.current) {
            val width = widthInPixels.size.width//.toDp()
            val height = widthInPixels.size.height//.toDp()
            return width.toFloat() to height.toFloat()
        }
    }

    /**
     * return the height of this node.
     *
     * inherited from parent, -1 if not yet calculated
     *
     * @return
     */
    /** height of one level, -1 means not yet calculated  */
    //var height: Float = -1F

    /** Am I dragged, and if so: how am i dragged  */
    var dragMode: DragMode = DragMode.NONE
    var draggedNode: Node? = null

    val height = measureTextWidth("X", style).second + DOUBLED_MARGIN + LEVEL_DISTANCE

    fun translate(node: Node, deltax: Float) {
        for (n in node) {
            n.bound.x += deltax
        }
    }

    @Composable
    fun calcSize() {
        for (node in root) {
            val formString: String = node.text
            val (w, h) = measureTextWidth(formString, style)
            node.bound.width = w + DOUBLED_MARGIN
            node.bound.height = h + DOUBLED_MARGIN
        }
    }

    @Composable
    fun layout(node: Node, xoffset: Float): Float {
        val mywidth = node.bound.width + MARGIN

        var growOffset = xoffset
        for (n in node.getSuccs()) {
            growOffset = layout(n, growOffset)
        }

        val theirWidth = growOffset - xoffset
        val maxWidth: Float =
            if (theirWidth < mywidth) {
                val trans = (mywidth - theirWidth) / 2
                for (n in node.getSuccs()) {
                    translate(n, trans)
                }
                mywidth
            } else {
                theirWidth
            }

        node.bound.y = height * node.depth
        node.bound.x = xoffset + (maxWidth - mywidth) / 2F

        return xoffset + maxWidth
    }

    @Composable
    fun layout(): Float {
        calcSize()
        return layout(root, 0F)
    }

    @Composable
    fun contains(node: Node, p: Point): Node? {
        for (n in node) {
            if (n.bound.contains(p)) return n
        }
        return null
    }

    /**
     * does this node contain a point?
     *
     * @param p
     * point to check
     * @return true iff the point lays within the boundaries.
     */
    @Composable
    fun contains(p: Point): Node? {
        return contains(root, p)
    }

    @Composable
    fun isLaidOut(n: Node): Boolean {
        return n.bound.width !== 0F
    }

    /**
     * calculate the dimensions (not the position)
     */
    /**
     * get the center of the bounding box
     *
     * @return a newly created Point.
     */
    fun getCenter(node: Node): Point {
        return Point(
            node.bound.x + node.bound.width / 2,
            node.bound.y + node.bound.height / 2
        )
    }

    @Composable
    fun paint(node: Node, g: DrawScope) {
        val center: Point = getCenter(node)
        for (n in node.getSuccs()) {
            if (isLaidOut(n)) {
                val nc: Point = getCenter(n)
                g.drawLine(
                    Color.Black,
                    start = Offset(center.x, center.y),
                    end = Offset(nc.x, nc.y)
                )
            }
        }

        if (!isLaidOut(node)) return

        if (node.isClosed) {
            g.drawLine(Color.Black, Offset(center.x, center.y), Offset(center.x, center.y + height))
            g.drawOval(
                Color.Black, Offset(center.x - MARGIN, center.y + height - MARGIN),
                size = Size(DOUBLED_MARGIN, DOUBLED_MARGIN)
            )

        }

        val nextColor =
            when (node.getFormula()!!.type) {
                ALPHA -> alphaColor
                BETA -> betaColor
                GAMMA -> (gammaColor)
                DELTA -> (deltaColor)
                NEGNEG -> (negnegColor)
                else -> (Color.White)
            }

        g.drawRect(nextColor, Offset(node.bound.x, node.bound.y), Size(node.bound.width, node.bound.height))

        //g.setColor(Color.black)
        val dragMode =
            if (draggedNode === node) {
                when (dragMode) {
                    DragMode.EXPAND -> (Color.Red)
                    DragMode.CLOSE -> (Color.Blue)
                    else -> Color.Black
                }
            } else {
                Color.Black
            }

        g.drawRect(dragMode, Offset(node.bound.x, node.bound.y), Size(node.bound.width, node.bound.height))

        //val descent = getDescent("p") as Int
        g.drawText(
            textMeasurer,
            text = node.text,
            maxLines = 1,
            topLeft = Offset(
                node.bound.x + MARGIN,
                node.bound.y + node.bound.height - MARGIN
            )
        )

        for (n in node.getSuccs()) {
            paint(n, g)
        }
    }

    @Composable
    fun paint(g: DrawScope) {
        paint(root, g)
    }


    /**
     * get the width over all nodes
     *
     * @return the width of the tree beginning here
     */
    @Composable
    fun getWidth(node: Node): Float {
        var width = node.bound.width + MARGIN

        var theirWidth = 0F
        for (n in node.getSuccs()) {
            theirWidth += getWidth(n)
        }
        width = max(width, theirWidth)

        return width
    }

    val width = getWidth(root)


    @Composable
    fun setDragged(node: Node?, dragMode: DragMode) {
        draggedNode = node
        //TODO dragMode = dragMode
    }
}

enum class DragMode {
    NONE, EXPAND, CLOSE
}

private val alphaColor = Color(128, 255, 128)
private val betaColor = Color(128, 128, 255)
private val gammaColor = Color(128, 255, 255)
private val deltaColor = Color(255, 255, 128)
private val negnegColor = Color(255, 128, 255)

private const val MARGIN = 10F
private const val DOUBLED_MARGIN = 2 * MARGIN
private const val LEVEL_DISTANCE = DOUBLED_MARGIN
