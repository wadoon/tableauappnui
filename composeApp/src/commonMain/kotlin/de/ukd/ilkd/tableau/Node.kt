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
package de.ukd.ilkd.tableau

import de.ukd.ilkd.tableau.history.NewNodes
import org.jetbrains.skia.Point
import kotlin.math.max

data class Rectangle(var x: Float = 0F, var y: Float = 0F, var width: Float = 0F, var height: Float = 0F) {
    fun contains(p: Point): Boolean {
        return false
    }
}

/**
 * Nodes the vertices within the tableau tree. They are used for graphics
 * (layout, painting) and for algorithms like closing ...
 *
 * @author MU
 */
class Node(
    /** depth in the tree, root has 0.  */
    val depth: Int, formula: Formula?, parent: Node?, reason: Node?
) : Iterable<Node?>, Comparable<Node> {
    /** this information is not used in this class but stored for NodeUI  */
    var bound: Rectangle = Rectangle()

    /** the contained formula  */
    private var formula: Formula?

    /** a list of direct successors  */
    private val succs = mutableListOf<Node>()

    /** the node directly above, null for the first node  */
    private var parent: Node?

    /** the node that causes my existence (or null if initially there)  */
    val reason: Node?

    /** "serial number"  */
    val number: Int

    /** This node has been closed if this is positive.
     * If it is 0, it is open.
     * The number (other than 0) refers to the number of the close operation
     * We store it to be able to undo it if needed.  */
    private var closedBy = 0

    init {
        this.formula = formula
        this.parent = parent
        this.reason = reason
        this.number = ++counter
    }

    /**
     * expand the given Node according to the rules and
     * place the children underneath me. I must be a leaf for that.
     *
     * @return a HistoryItem that contains the newly created nodes.
     */
    fun expandAsSucc(node: Node): NewNodes {
        require(isLeaf)
        require(node === this || hasAsAncestor(node))
        require(!isClosed)
        val expandedFormula: Formula? = node.getFormula()
        val newnodes = NewNodes()

        val expansion = expandedFormula!!.applyRule(false)
        if (expansion != null) {
            for (branch in expansion) {
                var target = this
                for (f in branch) {
                    target = target.addSucc(f, node)
                    newnodes.add(target)
                }
            }
        }

        return newnodes
    }

    /**
     * add a new successor. It has my node as parent.
     *
     * @param f
     * formula to embed
     * @param reason
     * reason to set in the new node
     * @return newly created node
     */
    fun addSucc(f: Formula?, reason: Node?): Node {
        val n = Node(depth + 1, f, this, reason)
        succs.add(n)
        return n
    }


    val text: String
        /**
         * get the label.
         *
         * This depends on the setting of "tablet.ancestor". If this is set to true
         * nodes will be labelled
         *
         * "Number[Ancestornumber] : formula"
         *
         * but otherwise only
         *
         * "Number : formula"
         *
         * @return new string
         */
        get() = if (SHOW_ANCESTORS && reason != null) number.toString() + "[" + reason.number + "]" + ": " + formula
        else number.toString() + ": " + formula


    override fun toString(): String =
        ("""Node[#$number, parent=${if (parent == null) "null" else "#" + parent!!.number}, depth=$depth, form=$formula${if (closedBy > 0) ", closedby=$closedBy" else ""}, #succ=${succs.size}""") + "]"

    // debug
    fun toTree(): String = buildString {
        for (i in 0 until depth) {
            append(" ")
        }
        append(this)
        append("\n")
        for (n in succs) {
            append(n.toTree())
        }
    }

    /**
     * mark all leafs below this node as closed
     * @param by this is the internal number of the close operatio, > 0
     * @return true iff at least one node has not been closed before and is closed now
     */
    fun setClosed(by: Int): Boolean {
        require(by > 0)
        if (isLeaf) {
            if (!isClosed) {
                closedBy = by
                return true
            } else {
                return false
            }
        } else {
            var oneClosed = false
            for (n in succs) {
                oneClosed = oneClosed or n.setClosed(by)
            }
            return oneClosed
        }
    }

    /**
     * mark all leafs below this node as open if they are closed by a particular
     * closing operation. may happen during rollbacks
     *
     * @param by
     * this is the internal number of the close operatio, > 0
     */
    fun unsetClosed(by: Int) {
        if (closedBy == by) closedBy = 0
        for (n in succs) {
            n.unsetClosed(by)
        }
    }

    val maxDepth: Int
        /**
         * get the maximum depth from this node on starting w/ the stored depth.
         */
        get() {
            var d = if (isClosed) depth + 1 else depth
            for (n in succs) {
                d = max(d, n.maxDepth)
            }
            return d
        }

    val toolTipText: String
        /**
         * tool tip tells about the formula
         *
         * @return
         */
        get() = buildString {
            append(("<html>This is a " + formula?.type) + "-formula<br>")
            if (reason != null) append(
                ("resulting from #" + reason.number + " (" + reason.formula?.type) + ")"
            )
            else append("and an initial formula")
            append("</html>")
        }

    val isLeaf: Boolean
        /**
         * per def: a node is a leaf if it has no children and is not closed
         */
        get() = succs.isEmpty() && !isClosed

    fun instantiate(`var`: String, f: Formula) {
        formula = formula!!.instantiate(`var`, f)
        for (n in succs) {
            n.instantiate(`var`, f)
        }
    }

    fun uninstantiate(freevar: String) {
        // no longer needed?
//        closed = false;
        formula = formula!!.uninstantiate(freevar)
        for (n in succs) {
            n.uninstantiate(freevar)
        }
    }

    /**
     * remove myself from my parent if I have one!
     * and remove my link to parent too
     */
    fun remove() {
        if (parent != null) parent!!.succs.remove(this)
        parent = null
    }

    fun getFormula(): Formula? {
        return formula
    }

    fun getParent(): Node {
        return parent!!
    }

    /**
     * is n an ancestor for me? is it a parent, or parent's parent ...
     */
    fun hasAsAncestor(n: Node): Boolean {
        return parent === n || (parent != null && parent!!.hasAsAncestor(n))
    }

    /**
     * try automatic proving, i.e. saturated application of a certain type.
     * @return
     */
    fun automaticApplication(type: Type): NewNodes {
        val newNodes = NewNodes()
        if (formula?.type === type) {
            expandIfNotAlreadyExpanded(this, newNodes)
        }
        for (n in succs) {
            newNodes.addAll(n.automaticApplication(type))
        }
        return newNodes
    }

    /**
     * check whether a node has already been expanded (using the reason field).
     * If not and it is a leaf: Expand here. If expanded: return. Else: descend
     *
     * @param node
     * Node to expand
     * @param newNodes storage for newly created nodes
     * @return the newly created nodes by this step
     */
    private fun expandIfNotAlreadyExpanded(node: Node, newNodes: NewNodes) {
        if (reason !== node) {
            if (isLeaf) {
                val expanded = expandAsSucc(node)
                newNodes.addAll(expanded)
            } else {
                for (n in succs) {
                    n.expandIfNotAlreadyExpanded(node, newNodes)
                }
            }
        } else {
            // node has already been expanded
        }
    }

    val isClosed: Boolean
        get() = closedBy != 0

    /**
     * find a node with a given number
     *
     * @param n
     * number to look
     * @return null if not found in this tree, otherwise a node with number n
     */
    fun getNode(n: Int): Node? {
        if (number == n) return this
        for (s in succs) {
            val ret = s.getNode(n)
            if (ret != null) return ret
        }
        return null
    }

    // TODO ... put into a visitor or somewhere else ...
    enum class ChildMode {
        LEFT, RIGHT, SINGLE, NONE, ABSOLUTE
    }

    fun toLatex(sb: StringBuilder, childMode: ChildMode) {
        indent(sb, 1)
        sb.append("\\node (n")
        sb.append(number).append(") ")

        when (childMode) {
            ChildMode.LEFT -> sb.append("[below left of=n").append(parent!!.number).append("] ")
            ChildMode.RIGHT -> sb.append("[below right of=n").append(parent!!.number).append("] ")
            ChildMode.SINGLE -> sb.append("[below of=n").append(parent!!.number).append("] ")
            ChildMode.ABSOLUTE -> sb.append(
                " at (${bound.x + bound.width / 2.0},-${bound.y + bound.height / 2.0}) "
            )

            ChildMode.NONE -> TODO()
        }
        sb.append("{ $").append(formula!!.toLatex()).append("$ \\tableauLabel{").append(number)
        if (reason != null) {
            sb.append("""${"[$" + reason.formula!!.type.latex}$(${reason.number})]""")
        }
        sb.append("}}")
        if (parent != null) {
            sb.append(" edge (n").append(parent!!.number).append(")")
        }
        sb.append(";\n")

        if (childMode == ChildMode.ABSOLUTE) {
            for (n in succs) {
                n.toLatex(sb, ChildMode.ABSOLUTE)
            }
        } else {
            when (succs.size) {
                1 -> succs[0].toLatex(sb, ChildMode.SINGLE)
                2 -> {
                    succs[0].toLatex(sb, ChildMode.LEFT)
                    succs[1].toLatex(sb, ChildMode.RIGHT)
                }
            }
        }

        if (isClosed) {
            indent(sb, 1)
            sb.append("\\draw (n$number)+(0,-20) node (c${number}) {\\tableauClose};\n")
        }
    }

    fun getSuccs(): List<Node> = succs

    fun visit(visitor: NodeVisitor) {
        val visitKids: Boolean = visitor.visit(this)
        if (visitKids) {
            for (node in succs) {
                node.visit(visitor)
            }
        }
    }

    private class Itr(start: Node) : Iterator<Node> {
        private val stack = mutableListOf<Node>()

        init {
            stack.add(start)
        }

        override fun hasNext(): Boolean = stack.isNotEmpty()

        override fun next(): Node {
            val first: Node = stack.removeFirst()
            stack.addAll(0, first.succs)
            return first
        }
    }

    override fun iterator(): Iterator<Node> = Itr(this)

    /**
     * If nodes need to be compared, they are ordered by their number
     * @param o node to compare to, not null
     * @return equal to `o.number - this.number`
     */
    override fun compareTo(o: Node): Int {
        return o.number - number
    }

    companion object {
        /**
         * get the number of created nodes
         *
         * @return the static value
         */
        /** to create serial numbers  */
        var counter: Int = 0
            private set

        private fun indent(sb: StringBuilder, depth: Int) {
            for (i in 0 until depth) {
                sb.append("  ")
            }
        }

        fun resetCounter() {
            counter = 0
        }
    }
}