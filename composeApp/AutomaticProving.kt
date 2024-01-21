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

class AutomaticProving(private val maxChoiceDepth: Int, root: Node, useTrigger: Boolean) {
    private val root: Node = root
    private var history: History? = null
    private var triggerLock: Any? = null
    private var trigger = false

    constructor(maxChoiceDepth: Int, root: Node) : this(maxChoiceDepth, root, false)

    init {
        if (useTrigger) this.triggerLock = Any()
    }

    fun run() {
        history = History(root)
        history.add(runBasicSteps(root))
        history.add(runAutoClose(root))

        try {
            while (true) {
                waitTrigger()

                if (Thread.interrupted()) {
                    throw InterruptedException()
                }

                var goal: Node = nextOpenGoal()
                    ?: // mark successful
                    return

                val choiceDepth = countGamma(goal)

                if (choiceDepth > maxChoiceDepth) {
                    history.rollBack()
                } else {
                    val choicePoint: ChoicePoint = createChoicepoint(goal)
                    history.add(choicePoint)
                }

                var choicePoint: ChoicePoint = history.peek() as ChoicePoint
                if (choicePoint == null) {
                    // mark unsuccessfull
                    history.undoAll()
                    return
                }

                var item: ChoiceItem = choicePoint.take()
                while (item == null) {
                    history.take()

                    history.rollBack()
                    choicePoint = history.peek() as ChoicePoint

                    if (choicePoint == null) {
                        // mark unsuccessfull
                        history.undoAll()
                        return
                    }

                    item = choicePoint.take()
                }

                goal = choicePoint.getGoal()
                assert(goal.isLeaf())
                assert(goal === root || goal.hasAsAncestor(root))
                assert(root.toTree().equals(choicePoint.tree)) { root.toTree() }
                history.add(item.apply(root))

                history.add(runBasicSteps(goal))
                // TODO reicht hier ggf. goal?
                history.add(runAutoClose(root))
            }
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
        history.undoAll()
    }

    private fun countGamma(goal: Node): Int {
        val reason: Node = goal.getReason()
        var `val` = if (reason != null && reason.getFormula().getType() === Type.GAMMA) 1
        else 0

        val parent: Node = goal.getParent()
        if (parent != null) `val` += countGamma(parent)

        return `val`
    }

    private fun nextOpenGoal(): Node? {
        var smallestLeaf: Node? = null
        for (node in root) {
            if (node.isLeaf() && (smallestLeaf == null || node.getNumber() < smallestLeaf.getNumber())) smallestLeaf =
                node
        }
        return smallestLeaf
    }

    private fun createChoicepoint(leaf: Node): ChoicePoint {
        val choicePoint: ChoicePoint = ChoicePoint(leaf)
        // first close then gamma ...
        addClose(choicePoint, leaf)
        addGamma(choicePoint, leaf)
        choicePoint.tree = root.toTree()
        choicePoint.sort()
        return choicePoint
    }

    // choose those gamma formulas more likely that are less expanded on that tree.
    private fun addGamma(choicePoint: ChoicePoint, leaf: Node) {
        var n: Node = leaf
        while (n != null) {
            val f: Formula = n.getFormula()
            if (f.getType() === Type.GAMMA) {
                val gamma: Gamma = Gamma(leaf, n)
                //				if(gamma.instCount == 0) {
                choicePoint.add(gamma)
                //			}
            }
            n = n.getParent()
        }
    }

    private fun addClose(choicePoint: ChoicePoint, leaf: Node) {
        var n: Node = leaf
        while (n != null) {
            var m: Node = n.getParent()
            while (m != null) {
                if (Close.canUnify(m, n)) {
                    choicePoint.add(Close(m, n))
                    //					System.out.println(" CLOSE " + m + n);
                }
                m = m.getParent()
            }
            n = n.getParent()
        }
    }

    fun runBasicSteps(below: Node?): NewNodes {
        var before: Int
        val newNodes: NewNodes = NewNodes()
        do {
            before = Node.getCounter()
            newNodes.addAll(below!!.automaticApplication(Type.ALPHA))
            newNodes.addAll(below!!.automaticApplication(Type.DELTA))
            newNodes.addAll(below!!.automaticApplication(Type.NEGNEG))
            newNodes.addAll(below!!.automaticApplication(Type.BETA))
        } while (Node.getCounter() > before)
        return newNodes
    }

    private fun runAutoClose(below: Node): CompoundItem {
        val coll: CompoundItem = CompoundItem()
        for (n in below) {
            var p: Node = n.getParent()
            while (p != null) {
                if (n.getFormula()!!.closesWith(p.getFormula())) {
                    val histItem: HistoryItem.Close = Close(n, p)
                    if (n.setClosed(histItem.getNumber()));
                    coll.add(histItem)
                }
                p = p.getParent()
            }
        }
        return coll
    }

    fun trigger() {
        if (triggerLock != null) synchronized(triggerLock) {
            trigger = true
            triggerLock.notify()
        }
    }

    @Throws(InterruptedException::class)
    private fun waitTrigger() {
        if (triggerLock != null) {
            synchronized(triggerLock) {
                while (!trigger) triggerLock.wait()
                trigger = false
            }
        }
    }

    fun addHistoryTo(hist: History) {
        for (hi in history) {
            hist.add(hi)
        }
    }
}
