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

import de.ukd.ilkd.tableau.history.ChoicePoint
import de.ukd.ilkd.tableau.history.HistoryItem

/**
 * A proof history.
 *
 * It records all steps that have been made and
 * allows manual and automatic rollback.
 *
 * Instantiations are also recorded to allow for ATP.
 *
 * @author mattias ulbrich
 */
class History(private var root: Node) : Iterable<HistoryItem?> {
    private val historyStack = mutableListOf<HistoryItem>()

    fun undo(): HistoryItem? {
        var historyItem: HistoryItem? = null
        while (!historyStack.isEmpty() && historyItem == null) {
            historyItem = take()
            if (historyItem is ChoicePoint) historyItem = null
        }

        if (historyItem == null) return null

        historyItem.undo(root)
        //		System.out.println("-- UNDO -- " + historyItem);
        return historyItem
    }

    fun undoAll() {
        while (undo() != null);
    }

    fun add(item: HistoryItem) {
        if (item.isNotEmpty) {
//			System.out.println("-- DO -- " + item);
            historyStack.add(item)
        }
    }

    fun rollBack() {
        while (historyStack.isNotEmpty()) {
            if (peek() is ChoicePoint) break

            val item: HistoryItem = take()
            item.undo(root)
            //			System.out.println("-- UNDO --" + item);
//			System.out.println(root.toTree());
        }
    }

    fun peek(): HistoryItem? = historyStack.lastOrNull()
    fun take(): HistoryItem = historyStack.removeLast()
    override fun iterator(): Iterator<HistoryItem> = historyStack.iterator()
    override fun toString(): String = historyStack.toString()
}