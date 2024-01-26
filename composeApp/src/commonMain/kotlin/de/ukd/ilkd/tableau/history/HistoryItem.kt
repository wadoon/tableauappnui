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
package de.ukd.ilkd.tableau.history

import de.ukd.ilkd.tableau.ChoiceItem
import de.ukd.ilkd.tableau.Formula
import de.ukd.ilkd.tableau.Node

/**
 * sth that can be put on the history stack
 *
 * @author mattias
 * @see History
 */
interface HistoryItem {
    fun undo(root: Node)

    val isNotEmpty: Boolean
}


class Instantiation(private val variable: String, private val instantiation: Formula) : HistoryItem {

    override fun undo(root: Node) {
        root.uninstantiate(variable)
    }


    override fun toString(): String = "Instantiate $variable = $instantiation"

    override val isNotEmpty: Boolean
        get() = true
}

class Close(positiveNode: Node, negativeNode: Node) : HistoryItem {
    private val node1: Node = positiveNode
    private val node2: Node = negativeNode

    // internal number to identify nodes closed by this item.
    val number: Int

    init {
        this.number = ++counter
    }

    override fun undo(root: Node) {
        // only run it once -- on the one higher in the tree.
        if (node1.number < node2.number) {
            node1.unsetClosed(number)
        } else {
            node2.unsetClosed(number)
        }
    }

    override fun toString(): String = "Branch(es) closed with #${node1.number} and #${node2.number}"

    override val isNotEmpty: Boolean
        get() = true

    companion object {
        // the counter of Objects of this type
        private var counter = 1
    }
}

class NewNodes(val nodes: MutableList<Node> = mutableListOf()) : MutableList<Node> by nodes, HistoryItem {
    override fun undo(root: Node) {
        for (node in this) {
            node.remove()
        }
    }

    override fun toString(): String {
        var s = "Add nodes:"
        for (n in this) {
            s += "${n.number} "
        }
        return s
    }

    override val isNotEmpty: Boolean
        get() = isNotEmpty()
}

class ChoicePoint(private val goal: Node) : HistoryItem {
    private val choices = mutableListOf<ChoiceItem>()

    var tree: String? = null

    fun take(): ChoiceItem? {
        if (choices.isEmpty()) return null
        else {
            val first: ChoiceItem = choices.removeAt(0)
            choices.remove(first)
            return first
        }
    }

    fun add(e: ChoiceItem) {
        choices.add(e)
    }

    override fun undo(root: Node) =
        throw UnsupportedOperationException("This may not be called on this pseudo element")


    override fun toString(): String = "ChoicePoint[$choices]"

    fun getGoal(): Node = goal

    fun sort() {
        choices.sort()
    }

    override val isNotEmpty: Boolean
        get() = true
}

class CompoundItem(val historyItems: MutableList<HistoryItem> = mutableListOf())
    : MutableList<HistoryItem> by historyItems,
    HistoryItem {

    override fun undo(root: Node) {
        for (i in size - 1 downTo 0) {
            get(i).undo(root)
        }
    }

    override val isNotEmpty: Boolean
        get() = historyItems.isNotEmpty()
}
