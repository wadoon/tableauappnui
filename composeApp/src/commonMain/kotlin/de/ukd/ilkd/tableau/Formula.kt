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

sealed class Formula {
    abstract val type: Type

    abstract val children: List<Formula>

    abstract fun clone(): Formula

    /**
     * apply the tableau rules to this formula.
     *
     * There is only one rule per formula (here). The result is returned in a
     * matrix of formulas: It is an array of formula array, i.e. an array over
     * branches.
     *
     * If the formula is encountered in a negated context the argument is set to
     * true, and the corresponding rule must be applied
     *
     * @param negated
     * true iff formula in negated context
     * @return a list of branches, null if nothing can be extended.
     */
    abstract fun applyRule(negated: Boolean): List<List<Formula?>>


    /**
     * to check if two formulas are compatible (i.e. can be unified) this string
     * describes the top-level without telling about the embedded formulas.
     *
     * @return a string characterising the formula
     */
    abstract val prefix: String

    abstract fun instantiate(v: String, f: Formula): Formula
    abstract fun uninstantiate(freevar: String): Formula

    /*
    {
        val fnew: Formula = clone()
        fnew.children = children.map { it.instantiate(v, f) }
        return fnew
    }

        val fnew = clone()
        fnew.children = children.map { it.uninstantiate(freevar) }
        return fnew
    }
     */

    fun collectFreeVariables(list: Collection<IdFormula?>?) {
        for (form in children) {
            form.collectFreeVariables(list)
        }
    }

    override fun equals(other: Any?): Boolean =
        // TODO think of something more elaborate
        toString() == other.toString()

    // this is needed for insertion into hashtables:

    // TODO think of something more elaborate
    override fun hashCode(): Int = toString().hashCode()

    fun getSubFormula(i: Int): Formula = children[i]
    fun countSubFormulae(): Int = children.size

    /**
     * is `f` the complement to this formula.
     * Note: doesnt matter which is negated
     *
     * @param f a formula, not null
     * @return `f` and `this` are complementary
     */
    fun closesWith(f: Formula?): Boolean {
        return ((f is NotFormula && equals(f.children[0])) || (this is NotFormula && f!! == children[0]))
    }

    /**
     * test whether an identifier (used as bound variable) appears within this formula
     * not within the scope of a quantifier. needed for collision detection.
     *
     * @param id identifier to check
     * @return true if there is an identifier referencing to id that is not in the
     * scope of a quantifier for id.
     */
    open fun containsFreeIdentifier(id: String?): Boolean {
        for (f in children) {
            if (f.containsFreeIdentifier(id)) {
                return true
            }
        }
        return false
    }

    protected open val isFreeVariable: Boolean
        get() = false

    abstract fun toLatex(): String

    companion object {
        /**
         * unify two formulas.
         *
         * store all instantiations that are needed in a mapping.
         *
         * @param f1
         * formula 1 to unify
         * @param f2
         * formula 2 to unify
         * @param instantiations
         * an existing map to store instantiations to
         * @return true iff the formulas are unifiable.
         */
        fun unify(
            f1: Formula?, f2: Formula?, instantiations: Substitution
        ): Boolean {
            if (f1 is Instantiation) return unify(f1.children[0], f2, instantiations)
            if (f2 is Instantiation) return unify(f1, f2.children[0], instantiations)

            if (f1!! == f2) return true

            if (f1.isFreeVariable) {
                val identifier = (f1 as IdFormula?)!!.prefix
                val formula = instantiations.get(identifier)
                val f2 = instantiations.applyTo(f2!!)

                if (formula != null) {
                    return unify(formula, f2, instantiations)
                }

                val list: Set<IdFormula?> = HashSet()
                f2!!.collectFreeVariables(list)
                // X1 and f(X1) cannot be unified!
                if (list.contains(f1)) return false
                instantiations.add(identifier, f2)
                return true
            }

            if (f2!!.isFreeVariable) {
                // write this code only once ...
                return unify(f2, f1, instantiations)
            }

            if (f1.prefix != f2.prefix) return false

            for (i in f1.children.indices) {
                if (!unify(f1.children[i], f2.children[i], instantiations)) return false
            }

            return true
        }
    }
}

data class NotFormula(val sub: Formula) : Formula() {
    override val children: List<Formula>
        get() = listOf(sub)

    override fun clone(): Formula = copy()

    override fun applyRule(negated: Boolean): List<List<Formula?>> {
        // strip two negations
        return if (negated) {
            listOf(listOf(sub))
        } else {
            sub.applyRule(true)
        }
    }

    override val type: Type
        get() {
            if (sub is NotFormula) return Type.NEGNEG
            val type: Type = sub.type
            return type.opposite
        }


    override fun toString(): String = Constants.not + children[0]

    override val prefix: String
        get() = "~"

    override fun instantiate(v: String, f: Formula): Formula = NotFormula(sub.instantiate(v, f))
    override fun uninstantiate(freevar: String): Formula = NotFormula(sub.uninstantiate(freevar))
    override fun toLatex(): String = "\\lnot " + children[0].toLatex()
}

data class Instantiation(val instantiatedVariable: String, val instantiates: Formula) : Formula() {
    override fun applyRule(negated: Boolean) = children[0].applyRule(negated)

    override val type: Type
        get() = children[0].type

    override val children: List<Formula>
        get() = listOf(instantiates)

    override fun clone() = copy()

    override fun toString(): String = instantiates.toString()

    override fun toLatex(): String =
        // return subFormula[0].toLatex();
        IdFormula.identifierToLatex(instantiatedVariable) + "^*"

    override fun uninstantiate(freevar: String): Formula {
        return if (freevar == instantiatedVariable) {
            IdFormula(instantiatedVariable)
        } else {
            Instantiation(
                instantiatedVariable, instantiates.uninstantiate(freevar)
            )
        }
    }

    override val prefix: String
        get() = instantiates.prefix

    override fun instantiate(v: String, f: Formula): Formula =
        Instantiation(instantiatedVariable, instantiates.instantiate(v, f))
}

enum class Operator {
    AND, OR, IMPL, EQUIV
}

data class BinopFormula(val op: Operator, val left: Formula, val right: Formula) : Formula() {
    override val children: List<Formula>
        get() = listOf(left, right)

    override fun clone() = copy()

    override val type: Type
        get() = when (op) {
            Operator.EQUIV, Operator.AND -> Type.ALPHA
            else -> Type.BETA
        }

    override fun toString(): String = ("(" + left + Constants.getOp(op) + right) + ")"

    override fun toLatex(): String =
        ("(" + left.toLatex() + Constants.getOpLatex(op) + right.toLatex()) + ")"


    override fun applyRule(negated: Boolean): List<List<Formula?>> {
        val s1 = left
        val s2 = right
        return if (negated) {
            when (op) {
                Operator.IMPL -> listOf(listOf(s1, not(s2)))
                Operator.AND -> listOf(listOf(not(s1)), listOf(not(s2)))
                Operator.OR -> listOf(listOf(not(s1), not(s2)))
                Operator.EQUIV -> listOf(listOf(not(s1), s2), listOf(s1, not(s2)))
            }
        } else {
            when (op) {
                Operator.IMPL -> listOf(listOf(not(s1)), listOf(s2))
                Operator.AND -> listOf(listOf(s1, s2))
                Operator.OR -> listOf(listOf(s1), listOf(s2))
                Operator.EQUIV -> listOf(listOf(s1, s2), listOf(not(s1), not(s2)))
            }
        }
    }

    /*
     * little helper to make things easier to read.
     */
    private fun not(f: Formula): Formula = NotFormula(f)

    override val prefix: String
        get() = op.toString()

    override fun instantiate(v: String, f: Formula): Formula =
        copy(left = left.instantiate(v, f), right = right.instantiate(v, f))

    override fun uninstantiate(freevar: String): Formula =
        copy(left = left.uninstantiate(freevar), right = right.uninstantiate(freevar))
}

enum class Quantor {
    FORALL, EXISTS
}

data class QuantorFormula(val quantor: Quantor, val variable: String, val formula: Formula) : Formula() {
    override val type: Type
        get() = if (quantor == Quantor.EXISTS) Type.DELTA
        else Type.GAMMA

    override val children: List<Formula>
        get() = listOf(formula)

    override fun clone(): Formula = copy()

    override fun toString(): String = "${Constants.getQuantor(quantor)}$variable.$formula"

    override fun toLatex(): String =
        "${Constants.getQuantorLatex(quantor) + variable}.${formula.toLatex()}"


    override fun applyRule(negated: Boolean): List<List<Formula?>> {
        val inst: Formula
        // cryptic for gamma&normal | delta&negated
        if ((type === Type.GAMMA) != negated) inst = Constants.mkFreeVar()
        else {
            val set: Set<IdFormula> = HashSet()
            children[0].collectFreeVariables(set)
            inst = Constants.mkSkolem(set)
        }

        var formula: Formula

        try {
            formula = children[0].instantiate(variable, inst)
        } catch (e: InstantiationClashException) {
            // The instantiation is either a free var or a skolem and should NOT provide such difficulties
            throw Error(e)
        }

        if (negated) formula = NotFormula(formula)

        return listOf(listOf(formula))
    }

    /**
     * Instantiation may only happen if the quantified variable does not
     * appear in the formula to be introduced.
     * This is a little over-restrictive, because it fails also if
     * var does not even appear in the quantified body.
     */
    override fun instantiate(variable: String, f: Formula): Formula =
        if (f.containsFreeIdentifier(this.variable))
            throw InstantiationClashException(this.variable, variable, f)
        else copy(formula = formula.instantiate(variable, f))

    override fun uninstantiate(freevar: String): Formula = copy(formula = formula.uninstantiate(freevar))

    /**
     * if id is the bound variable, it does not appear free in the body.
     */
    override fun containsFreeIdentifier(id: String?): Boolean {
        return if (variable == id) false
        else super.containsFreeIdentifier(id)
    }

    override val prefix: String
        get() = "$quantor $variable"
}

data class Application(val identifier: String, override val children: List<Formula>) : Formula() {
    constructor(identifier: String, vararg  children: Formula): this(identifier, children.toList())

    override fun applyRule(negated: Boolean): List<List<Formula?>> = listOf(listOf())

    override val type: Type
        get() = Type.NONE

    override fun clone(): Formula = copy()

    override fun toString(): String {
        var ret: String? = "$identifier("
        for (i in children.indices) {
            ret += children[i]
            if (i != children.size - 1) ret += ","
        }
        return ret.toString() + ")"
    }


    override fun toLatex(): String =
        "${IdFormula.identifierToLatex(identifier)}(${children.joinToString(", ") { it.toLatex() }})"

    override val prefix: String
        get() = "${identifier + "(" + children.size})"

    override fun instantiate(v: String, f: Formula): Application =
        copy(children = children.map { it.instantiate(v, f) })

    override fun uninstantiate(freevar: String) = copy(children = children.map { it.uninstantiate(freevar) })
}


data class IdFormula(override var prefix: String) : Formula() {
    override fun applyRule(negated: Boolean): List<List<Formula?>> = listOf(listOf())

    override fun uninstantiate(freevar: String): Formula = copy()
    override fun clone(): Formula = copy()

    override val type: Type
        get() = Type.NONE

    override val children: List<Formula>
        get() = listOf()


    override fun instantiate(v: String, f: Formula): Formula =
        if (prefix == v) f else this

    override fun toString(): String = prefix
    override fun toLatex(): String = identifierToLatex(prefix)

    fun collectFreeVariables(set: MutableList<IdFormula>) {
        if (isFreeVariable) set.add(this)
    }

    override val isFreeVariable: Boolean
        get() = "X[0-9]+".toRegex().matchEntire(prefix) != null

    override fun containsFreeIdentifier(id: String?): Boolean = prefix == id

    companion object {
        fun identifierToLatex(identifier: String): String {
            return if (identifier.startsWith("X")) ("X_{" + identifier.substring(1)) + "}"
            else if (identifier.startsWith("sk")) ("sk_{" + identifier.substring(2)) + "}"
            else identifier
        }
    }
}
