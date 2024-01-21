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

import de.ukd.ilkd.tableau.history.CompoundItem
import de.ukd.ilkd.tableau.history.HistoryItem
import kotlin.math.min


interface ChoiceItem : Comparable<ChoiceItem?> {
    fun apply(root: Node): HistoryItem
}

//	public Node getGoal();
class Gamma(var target: Node, var gamma: Node) : ChoiceItem {
    // how often has this gamma formula been instantiated on the branch?
    var instCount: Int = 0

    init {
        var n: Node = target
        while (n != null) {
            if (n.reason === gamma) instCount++
            n = n.getParent()
        }
    }

    override fun apply(root: Node): HistoryItem {
        return target.expandAsSucc(gamma)
    }

    override fun toString() = "Gamma[${gamma.number} on ${target.number}]"

    override fun compareTo(o: ChoiceItem?): Int {
        if (o is Gamma) {
            val g = o
            return if (instCount == g.instCount) {
                g.gamma.number - gamma.number
            } else {
                instCount - g.instCount
            }
        }
        // first close than gamma
        return 1
    }
}

class Close(node1: Node, node2: Node) : ChoiceItem {
    private val node1: Node
    private val node2: Node
    private val minIndex: Int

    init {
        require(canUnify(node1, node2))
        this.node1 = node1
        this.node2 = node2
        this.minIndex = min(node1.number, node2.number)
    }

    override fun apply(root: Node): HistoryItem {
        require(node1.hasAsAncestor(node2) || node2.hasAsAncestor(node1))
        val f1: Formula = node1.getFormula()!!
        val f2: Formula = node2.getFormula()!!
        val ret = CompoundItem()
        val inst: Substitution = Substitution()

        val canUnify = (Formula.unify(f1, NotFormula(f2), inst)
                || Formula.unify(NotFormula(f1), f2, inst))

        check(canUnify) { "Unification impossible" }

        println("Unification by $inst")
        for (variable in inst.domain) {
            try {
                val instantiation =
                    de.ukd.ilkd.tableau.history.Instantiation(variable, inst.get(variable)!!)
                //					instantiation.tree = root.toTree();
                ret.add(instantiation)

                root.instantiate(variable, Instantiation(variable, inst.get(variable)!!))
            } catch (e: InstantiationClashException) {
                throw IllegalStateException("Unification impossible")
            }
        }

        require(node1.getFormula()!!.closesWith(node2.getFormula()))
        val closeItem = de.ukd.ilkd.tableau.history.Close(node1, node2)

        // Now, after the instantiation;
        if (node1.hasAsAncestor(node2)) {
            node1.setClosed(closeItem.number)
        } else if (node2.hasAsAncestor(node1)) {
            node2.setClosed(closeItem.number)
        } else {
            throw IllegalStateException("Two nodes are not on one branch")
        }

        ret.add(closeItem)

        return ret
    }

    override fun toString(): String {
        return (("CloseItem[" + node1.number) + " and " + node2.number) + "]"
    }

    override fun compareTo(o: ChoiceItem?): Int {
        if (o is Close) {
            return minIndex - o.minIndex
        }
        // first close than gamma
        return -1
    }

    companion object {
        fun canUnify(node1: Node, node2: Node): Boolean {
            val f1: Formula = node1.getFormula()!!
            val f2: Formula = node2.getFormula()!!
            val inst: Substitution = Substitution()
            return (Formula.unify(f1, NotFormula(f2), inst)
                    || Formula.unify(NotFormula(f1), f2, inst))
        }
    }
}
