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

import de.ukd.ilkd.tableau.Operator.*
import de.ukd.ilkd.tableau.Quantor.EXISTS
import de.ukd.ilkd.tableau.Quantor.FORALL

/**
 * This factory / formatter creates skolem and free variable symbols and
 * provides the characters for printing formulas. There is the switch
 * [.USE_UNICODE] which decides which characters are taken.
 *
 * @author MU
 */
object Constants {
    var USE_UNICODE: Boolean = true

    private var skolemCounter = 0

    private var freeVarCounter = 0

    /**
     * make a new unique skolem symbol
     * @param set a set of parameter formulas.
     * @return an IdFormula iff list empty, otherwise an application.
     */
    fun mkSkolem(set: Set<IdFormula?>): Formula {
        val skolem = "sk" + (++skolemCounter)
        return if (set.isEmpty()) {
            IdFormula(skolem)
        } else {
            Application(skolem, listOf())
        }
    }

    /**
     * create a new unique free variable
     * @return a newly created IdFormula X...
     */
    fun mkFreeVar(): Formula {
        return IdFormula("X" + (++freeVarCounter))
    }

    val not: String
        get() = if (USE_UNICODE) "" + 172.toChar()
        else "~"

    fun getOp(op: Operator): String {
        return if (USE_UNICODE) getOpUnicode(op)
        else getOpASCII(op)
    }

    fun getQuantor(q: Quantor): String {
        return if (USE_UNICODE) getQuantorUnicode(q)
        else getQuantorASCII(q)
    }

    fun getQuantorUnicode(q: Quantor): String =
        when (q) {
            EXISTS -> "" + 8707.toChar()
            FORALL -> "" + 8704.toChar()
        }

    fun getQuantorASCII(q: Quantor): String =
        when (q) {
            EXISTS -> "E "
            FORALL -> "A "
        }

    fun getQuantorLatex(q: Quantor): String =
        when (q) {
            EXISTS -> "\\exists "
            FORALL -> "\\forall "
        }

    fun getOpASCII(op: Operator) =
        when (op) {
            AND -> "&"
            OR -> "|"
            IMPL -> "->"
            EQUIV -> "<->"
        }

    fun getOpLatex(op: Operator): String =
        when (op) {
            AND -> " \\wedge "
            OR -> " \\vee "
            IMPL -> " \\rightarrow "
            EQUIV -> " \\leftrightarrow "
        }

    private fun getOpUnicode(op: Operator): String =
        when (op) {
            AND -> "" + 8743.toChar()
            OR -> "" + 8744.toChar()
            IMPL -> "" + 8594.toChar()
            EQUIV -> "" + 8596.toChar()
        }

    fun resetCounters() {
        skolemCounter = 0
        freeVarCounter = 0
    }
}



/**
 * The SAMPLE formula.
 */
const val SAMPLE = "~(A x. (p(x)->q(x)) -> (A x. p(x) -> A x. q(x)))"

/**
 * Version constant
 */
const val BUILD = "18"

/**
 * static flag whether to indicate ancestors in nodes
 */
var SHOW_ANCESTORS: Boolean = true

/**
 * static flag whether to allow automatic proofs
 */
var ALLOW_AUTORUN: Boolean = false


/**
 * static flag whether to allow counter example search
 */
const val ALLOW_MODELSEARCH: Boolean = false


/**
 * static flag whether whether to place tex elements
 * absolute rather than relative
 */
var ABSOLUTE_TEX_EXPORT: Boolean = true
