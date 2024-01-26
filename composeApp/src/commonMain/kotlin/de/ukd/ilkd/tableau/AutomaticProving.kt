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
package de.uka.ilkd.tablet

import de.ukd.ilkd.tableau.Gamma
import de.ukd.ilkd.tableau.History
import de.ukd.ilkd.tableau.Node
import de.ukd.ilkd.tableau.Type
import de.ukd.ilkd.tableau.history.ChoicePoint
import de.ukd.ilkd.tableau.history.Close
import de.ukd.ilkd.tableau.history.CompoundItem
import de.ukd.ilkd.tableau.history.NewNodes

class AutomaticProving(private val maxChoiceDepth: Int, val root: Node, useTrigger: Boolean) {
    private val history: History = History(root)
    private var triggerLock: Any? = null
    private var trigger = false

    constructor(maxChoiceDepth: Int, root: Node) : this(maxChoiceDepth, root, false)

    init {
        if (useTrigger) this.triggerLock = Any()
    }

    fun run() {
        history.add(runBasicSteps(root))
        history.add(runAutoClose(root))

        try {
            while (true) {
                var goal: Node = nextOpenGoal()
                    ?: // mark successful
                    return

                val choiceDepth = countGamma(goal)

                if (choiceDepth > maxChoiceDepth) {
                    history.rollBack()
                } else {
                    val choicePoint = createChoicepoint(goal)
                    history.add(choicePoint)
                }

                var choicePoint = history.peek() as? ChoicePoint
                if (choicePoint == null) {
                    // mark unsuccessfull
                    history.undoAll()
                    return
                }

                var item = choicePoint.take()
                while (item == null) {
                    history.take()
                    history.rollBack()
                    choicePoint = history.peek() as? ChoicePoint

                    if (choicePoint == null) {
                        // mark unsuccessfull
                        history.undoAll()
                        return
                    }

                    item = choicePoint.take()
                }

                if (choicePoint != null) {
                    goal = choicePoint.getGoal()
                    require(goal.isLeaf)
                    require(goal === root || goal.hasAsAncestor(root))
                    require(root.toTree() == choicePoint.tree) { root.toTree() }
                    history.add(item.apply(root))
                    history.add(runBasicSteps(goal))
                    // TODO reicht hier ggf. goal?
                    history.add(runAutoClose(root))
                }
            }
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
        history.undoAll()
    }

    private fun countGamma(goal: Node): Int {
        val reason = goal.reason
        var value = if (reason != null && reason.getFormula()?.type === Type.GAMMA) 1
        else 0

        val parent = goal.getParent()
        if (parent != null) value += countGamma(parent)

        return value
    }

    private fun nextOpenGoal(): Node? {
        var smallestLeaf: Node? = null
        for (node in root) {
            if (node.isLeaf && (smallestLeaf == null || node.number < smallestLeaf.number))
                smallestLeaf = node
        }
        return smallestLeaf
    }

    private fun createChoicepoint(leaf: Node): ChoicePoint {
        val choicePoint = ChoicePoint(leaf)
        // first close then gamma ...
        addClose(choicePoint, leaf)
        addGamma(choicePoint, leaf)
        choicePoint.tree = root.toTree()
        choicePoint.sort()
        return choicePoint
    }

    // choose those gamma formulas more likely that are less expanded on that tree.
    private fun addGamma(choicePoint: ChoicePoint, leaf: Node) {
        var n: Node? = leaf
        while (n != null) {
            val f = n.getFormula()!!
            if (f.type === Type.GAMMA) {
                val gamma = Gamma(leaf, n)
                //				if(gamma.instCount == 0) {
                choicePoint.add(gamma)
                //			}
            }
            n = n.getParent()
        }
    }

    private fun addClose(choicePoint: ChoicePoint, leaf: Node?) {
        var n = leaf
        while (n != null) {
            var m = n.getParent()
            while (m != null) {
                if (de.ukd.ilkd.tableau.Close.canUnify(m, n)) {
                    choicePoint.add(de.ukd.ilkd.tableau.Close(m, n))
                    //					System.out.println(" CLOSE " + m + n);
                }
                m = m.getParent()
            }
            n = n.getParent()
        }
    }

    fun runBasicSteps(below: Node?): NewNodes {
        var before: Int
        val newNodes = NewNodes()
        do {
            before = Node.counter
            newNodes.addAll(below!!.automaticApplication(Type.ALPHA))
            newNodes.addAll(below.automaticApplication(Type.DELTA))
            newNodes.addAll(below.automaticApplication(Type.NEGNEG))
            newNodes.addAll(below.automaticApplication(Type.BETA))
        } while (Node.counter > before)
        return newNodes
    }

    private fun runAutoClose(below: Node): CompoundItem {
        val coll = CompoundItem()
        for (n in below) {
            var p: Node? = n.getParent()
            while (p != null) {
                if (n.getFormula()!!.closesWith(p.getFormula())) {
                    val histItem = Close(n, p)
                    if (n.setClosed(histItem.number));
                    coll.add(histItem)
                }
                p = p.getParent()
            }
        }
        return coll
    }

    fun addHistoryTo(hist: History) {
        for (hi in history) {
            hist.add(hi)
        }
    }
}
