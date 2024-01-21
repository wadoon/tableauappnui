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

class Substitution {
    private val map: MutableMap<String, Formula> = mutableMapOf()

    val domain: Set<String>
        get() = map.keys

    fun get(variable: String): Formula? = map[variable]

    fun add(variable: String, term: Formula) {
        for (v in domain) {
            val org: Formula? = get(v)
            val newTerm: Formula = org!!.instantiate(variable, term)
            map[v] = newTerm
        }
        map[variable!!] = term!!
    }

    fun applyTo(f: Formula): Formula? {
        var formula: Formula? = f
        for (v in domain) {
            formula = formula!!.instantiate(v, get(v)!!)
        }
        return formula
    }


    override fun toString(): String {
        return "Subst$map"
    }
}
