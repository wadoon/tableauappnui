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
package de.ukd.ilkd.tableau.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import de.ukd.ilkd.tableau.Node
import de.ukd.ilkd.tableau.Type.*
import org.jetbrains.skia.Point
import kotlin.math.max

class NodeUI(val root: Node, val measureTextWidth: (s: String) -> Pair<Float, Float>, val textMeasurer: TextMeasurer) {

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

    val height = measureTextWidth("X").second + DOUBLED_MARGIN + LEVEL_DISTANCE

    fun translate(node: Node, deltax: Float) {
        for (n in node)
            n.bound = n.bound.copy(width = n.bound.width + deltax)
    }

    fun calcSize() {
        for (node in root) {
            val formString: String = node.text
            val (w, h) = measureTextWidth(formString)
            node.bound = Size(w + DOUBLED_MARGIN, h + DOUBLED_MARGIN)
        }
    }

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

        node.pos = Offset(xoffset + (maxWidth - mywidth) / 2F, height * node.depth)
        return xoffset + maxWidth
    }

    fun layout(): Float {
        calcSize()
        return layout(root, 0F)
    }

    fun contains(node: Node, p: Point): Node? {
        for (n in node) {
            //TODO if (n.bound.contains(p)) return n
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
    fun contains(p: Point): Node? {
        return contains(root, p)
    }

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
            node.pos.x + node.bound.width / 2,
            node.pos.y + node.bound.height / 2
        )
    }

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

        g.drawRect(nextColor, Offset(node.pos.x, node.pos.y), Size(node.bound.width, node.bound.height))

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

        //g.drawRect(dragMode, Offset(node.bound.x, node.bound.y), Size(node.bound.width, node.bound.height))

        //val descent = getDescent("p") as Int
        g.drawText(
            textMeasurer,
            text = node.text,
            size = node.bound,
            topLeft = node.pos
            //maxLines = 1,
            /*topLeft = Offset(
                node.bound.x + MARGIN,
                node.bound.y + node.bound.height - MARGIN
            ),*/
        )

        for (n in node.getSuccs()) {
            paint(n, g)
        }
    }

    fun paint(g: DrawScope) {
        paint(root, g)
    }


    /**
     * get the width over all nodes
     *
     * @return the width of the tree beginning here
     */
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
