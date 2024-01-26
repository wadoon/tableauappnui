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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import de.ukd.ilkd.tableau.History
import de.ukd.ilkd.tableau.Node

var FONT_SIZE: Int = 12
var ALLOW_UNIFICATION: Boolean = false


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TableauPane(root: Node, history: MutableState<History?>) {
    val textMeasurer = rememberTextMeasurer()
    val style = TextStyle(color = Color.Black, fontSize = 12.sp)

    val measureTextWidth = { text: String ->
        val widthInPixels = textMeasurer.measure(text, style)
        widthInPixels.size.width.toFloat() to widthInPixels.size.height.toFloat()
        /*with(LocalDensity.current) {
            val width = widthInPixels.size.width//.toDp()
            val height = widthInPixels.size.height//.toDp()
            width.toFloat() to height.toFloat()
        }*/
    }




    Canvas(modifier = Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            println("test")
            for (node in root) {
                val r = Rect(node.pos, node.bound)
                println(node)
                println(r)
                if (it in r) {
                    println(node)
                }
            }
        })
    }, "Tableau") {
        val nodeUi = NodeUI(root, measureTextWidth, textMeasurer)
        nodeUi.layout()
        //setSize(optimalSize)
        //setMinimumSize(getSize())
        //setPreferredSize(getSize())

        nodeUi.paint(this)
    }

    /*
    private var root: Node? = null
    private val comments: JLabel? = comments
    private var draggedNode: Node? = null
    private var history: History = History()
    private var nodeUI: NodeUI? = null
    private var automaticProveThread: Thread? = null
    private var automaticProve: AutomaticProving? = null

    val optimalSize: Dimension?
    /**
     * get the size which would be ideal for this component, i.e. the space
     * needed by the tree itself
     *
     * @return the ideal dimensions
     */
    get() {
        if (root == null) return null

        val width: Int = nodeUI!!.getWidth() + 40
        val height: Int = (root.getMaxDepth() + 2) * nodeUI.getHeight()
        return Dimension(width, height)
    }

    /**
     * paint it by painting the nodes
     */

    fun paintComponent(g: Graphics) {
        val g2: Graphics2D = g as Graphics2D
        g2.translate(20, 20)
        if (root != null) nodeUI!!.paint(g2)
    }

    fun refresh() {
        if (root != null) {
            lay()
            repaint()
        }
    }

    @Throws(InstantiationClashException::class)
    fun instantiate(`var`: String, f: Formula?) {
        if (root != null) {
            root.instantiate(`var`, f)
            val histItem: HistoryItem.Instantiation = Instantiation(`var`, f)
            //            histItem.tree = root.toTree();
            history.add(histItem)
            comments.setText(histItem.toString())
        }
    }

    fun undo() {
        val item: HistoryItem = history.undo()
        if (item != null) comments.setText("Undone: $item")
        else comments.setText("Nothing to undo")
        refresh()
    }

    private fun nodeDraggedExpand(from: Node, to: Node) {
        val expandedFormula: Formula = from.getFormula()
        val type: Type = expandedFormula.getType()

        if (!to.isLeaf()) {
            comments.setText("The target node is not a leaf")
            return
        }

        if (type === Type.NONE) {
            comments.setText("This formula cannot be expanded any more")
            return
        }

        val newnodes: NewNodes = to.expandAsSucc(from)

        comments.setText("A $type-Formula has been expanded. $newnodes")
        history.add(newnodes)
        lay()
    }

    private fun nodeDraggedClose(n1: Node, n2: Node) {
        if (n1.getFormula()!!.closesWith(n2.getFormula())) {
            val histItem: HistoryItem.Close = Close(n1, n2)

            if (n1.hasAsAncestor(n2)) {
                n1.setClosed(histItem.getNumber())
            } else if (n2.hasAsAncestor(n1)) {
                n2.setClosed(histItem.getNumber())
            } else {
                comments.setText("The two nodes are not on one branch!")
                return
            }

            comments.setText(histItem.toString())
            history.add(histItem)
        } else {
            if (ALLOW_UNIFICATION) {
                if (tryUnification(n1.getFormula(), n2.getFormula())) {
                    comments
                        .setText("Formulas have been unified and can now be closed")
                    refresh()
                    return
                }
            }
            comments.setText("These formulas do not match to close")
        }
        lay()
    }

    private fun tryUnification(f1: Formula?, f2: Formula?): Boolean {
        val inst: Substitution = Substitution()
        if (Formula.unify(f1, NotFormula(f2), inst)
            || Formula.unify(NotFormula(f1), f2, inst)
        ) {
            System.out.println("Unification by $inst")
            for (`var` in inst.getDomain()) {
                try {
                    instantiate(`var`, Instantiation(`var`, inst.get(`var`)))
                    history.add(Instantiation(`var`, inst.get(`var`)))
                } catch (e: InstantiationClashException) {
                    // if this instantiation results in a collision, unification
                    // is impossible
                    return false
                }
            }
            return true
        }
        return false
    }

    fun mouseClicked(e: MouseEvent) {
        if (e.getClickCount() >= 2 && SwingUtilities.isLeftMouseButton(e)
            && e.isControlDown()
        ) {
            val p: Point = e.getPoint()
            p.translate(-20, -20)
            val n: Node = nodeUI!!.contains(p)
            System.out.println(n)
            if (n != null) {
                try {
                    val number: Int = Integer.parseInt(
                        JOptionPane
                            .showInputDialog("Number of the node to expand: ")
                    )
                    val m: Node = root!!.getNode(number)
                    if (m != null) nodeDraggedExpand(m, n)
                } catch (ex: Exception) {
                    comments.setText(ex.toString())
                }
            }
        } else if (e.getClickCount() >= 2 && SwingUtilities.isRightMouseButton(e) && e.isControlDown()) {
            val p: Point = e.getPoint()
            p.translate(-20, -20)
            val n: Node = nodeUI!!.contains(p)
            System.out.println(n)
            if (n != null) {
                try {
                    val number: Int = Integer
                        .parseInt(
                            JOptionPane
                                .showInputDialog("Number of the node to close/unify: ")
                        )
                    val m: Node = root!!.getNode(number)
                    if (m != null) nodeDraggedClose(m, n)
                } catch (ex: Exception) {
                    comments.setText(ex.toString())
                }
            }
        } else if (e.getClickCount() >= 2) {
            val p: Point = e.getPoint()
            p.translate(-20, -20)
            val n: Node = nodeUI!!.contains(p)
            System.out.println(n)
            if (n != null) nodeDraggedExpand(n, n)
        }
        refresh()
    }

    fun mouseEntered(e: MouseEvent?) {
    }

    fun mouseExited(e: MouseEvent?) {
    }

    fun mousePressed(e: MouseEvent) {
        if (root == null) return

        val p: Point = e.getPoint()
        p.translate(-20, -20)

        val n: Node = nodeUI!!.contains(p)
        System.out.println(n)
        draggedNode = n
        if (n != null) if (SwingUtilities.isLeftMouseButton(e)) nodeUI!!.setDragged(n, DragMode.EXPAND)
        else nodeUI!!.setDragged(n, DragMode.CLOSE)
        repaint()
    }

    fun mouseReleased(e: MouseEvent) {
        if (root == null) return

        val p: Point = e.getPoint()
        p.translate(-20, -20)
        val n: Node = nodeUI!!.contains(p)

        // System.out.println(n);
        if (draggedNode != null) {
            if (n != null && draggedNode !== n) {
                if (nodeUI.getDragMode() === DragMode.EXPAND) nodeDraggedExpand(draggedNode, n)
                else nodeDraggedClose(draggedNode, n)
            }
            nodeUI!!.setDragged(null, DragMode.NONE)
        }
        draggedNode = null

        repaint()
    }

    fun mouseDragged(e: MouseEvent?) {
    }

    fun mouseMoved(e: MouseEvent) {
        val p: Point = e.getPoint()
        p.translate(-20, -20)
        if (root == null) return
        val n: Node = nodeUI!!.contains(p)
        if (n != null) {
            setToolTipText(n.getToolTipText())
        } else {
            setToolTipText(null)
        }
    }

    /**
     * do all Alpha, delta, negneg and beta steps possible automatically (Only
     * available if not an applet)
     */
    fun automaticProve() {
        if (root == null) return

        if (automaticProveThread == null) {
            // currently no proof: start one
            // XXX
            val depth: Int =
                Integer.parseInt(JOptionPane.showInputDialog("How many Gamma-Instances are allowed on a branch?"))
            automaticProve = AutomaticProving(depth, root, false)
            automaticProveThread = Thread(automaticProve, "Automatic Proving")
            automaticProveThread.start()

            // TODO Make other buttons unavailable and mouse listening
            // XXX TMP:
            val action: ActionListener = object : ActionListener() {
                private val leader = charArrayOf('·', 'o', 'O', 'o')
                private var cnt = 0
                fun actionPerformed(e: ActionEvent) {
                    nodeUI!!.calcSize()
                    refresh()
                    comments.setText(leader[cnt++ % 4].toString() + " Hit AP-Button again to stop prove")
                    automaticProve.trigger()
                    if (!automaticProveThread.isAlive()) {
                        (e.getSource() as Timer).stop()
                        comments.setText("AP finished")
                        automaticProve.addHistoryTo(history)
                        automaticProveThread = null
                    }
                }
            }
            Timer(1000, action).start()
        } else {
            // interrupt existing proof
            automaticProveThread.interrupt()
            comments.setText("AP interrupted")
        }
    }

    /*
	 * print this component.
	 *
	 * centre graphic on page vertically and horizontally.
	 *
	 * It is scaled down, if it does not fit onto the page.
	 *
	 * The scale is reduced because 72 dpi is far too much!
	 *
	 */
    @Throws(PrinterException::class)
    fun print(graphics: Graphics, pageFormat: PageFormat, pageIndex: Int): Int {
        // TODO a little more sophisticated please

        if (pageIndex == 0) {
            // centre on page
            val ph: Double = pageFormat.getImageableHeight()
            val pw: Double = pageFormat.getImageableWidth()
            val gdim: Dimension? = optimalSize
            var scalefactor: Double = Math.min(.7, ph / gdim.getHeight())
            scalefactor = Math.min(scalefactor, pw / gdim.getWidth())

            val x = (pageFormat.getImageableX() + (pw - gdim.width * scalefactor) / 2) as Int
            val y = (pageFormat.getImageableY() + (ph - gdim.height * scalefactor) / 2) as Int

            graphics.translate(x, y)
            (graphics as Graphics2D).scale(scalefactor, scalefactor)

            paint(graphics)
            return PAGE_EXISTS
        } else {
            return NO_SUCH_PAGE
        }
    }

    /**
     * generate latex code for this tableau.
     * make sourrounding begin and end tikzpicture and delegate to root
     * @param sb string builder to write to
     */
    fun toLatex(sb: StringBuilder, absolute: Boolean) {
        if (absolute) {
            sb.append("\\begin{tikzpicture}[x=1pt, y=1pt]\n")
            root!!.toLatex(sb, ChildMode.ABSOLUTE)
        } else {
            sb.append("\\begin{tikzpicture}\n")
            root!!.toLatex(sb, ChildMode.NONE)
        }
        sb.append(";\n\\end{tikzpicture}\n")
    }
     */
}

