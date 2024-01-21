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

/**
 * The type of a formula.
 *
 * Every type has an antitype. That is the type of a negated instance. Alpha<->Beta,
 * Gamma<->Delta, Negneg<->Negneg, and None<->None. The latter is not correct
 * since ~A-> NONE and ~~A->NEGNEG, but this does not matter.
 *
 *
 *  * alpha are "conjunctive"
 *  * beta are "disjunctive"
 *  * gamma are "universal"
 *  * delta are "existential"
 *  * negneg are technical
 *
 *
 * @author MU
 */
enum class Type {
    ALPHA("\\alpha"), BETA(ALPHA, "\\beta"), GAMMA("\\gamma"), DELTA(
        GAMMA,
        "\\delta"
    ),
    NONE("-"), NEGNEG("\\lnot\\lnot");

    constructor(latex: String) {
        this.latex = latex
        opposite = this
    }

    constructor(inv: Type, latex: String) {
        opposite = inv
        this.latex = latex
        inv.opposite = this
    }

    var opposite: Type
        private set

    // their latex counterpart. used when exporting latex
    var latex: String
        private set
}
