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

class StackMap<K, V> {
    private val keys = mutableListOf<K>()
    private val values = mutableListOf<V>()

    fun clear() {
        keys.clear()
        values.clear()
    }

    fun containsKey(key: K) = keys.contains(key)
    fun containsValue(value: V) = values.contains(value)

    fun get(key: K): V? {
        for (i in keys.size - 1 downTo 0) {
            if (key!! == keys[i]) return values[i]
        }
        return null
    }

    val isEmpty: Boolean
        get() = keys.isEmpty()

    fun push(key: K, value: V) {
        keys.add(key)
        values.add(value)
    }

    fun size(): Int = keys.size

    fun pop() {
        val s = size() - 1
        keys.removeAt(s)
        values.removeAt(s)
    }
}
