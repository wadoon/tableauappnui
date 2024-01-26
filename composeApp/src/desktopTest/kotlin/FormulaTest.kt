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

import de.ukd.ilkd.tableau.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FormulaTest {
    private var map: Substitution? = null

    fun unify(string: String): Boolean {
        val l = lex(string)
        for (token in l) {
            print(token.type.name + "  ")
            if (token.type != TokenTypes.EOF)
                println(token.image)
            else
                println("EOF")
        }
        val p = FormulaParser(l)
        p.fullMode = true
        val (a, b) = p.Formulae()
        val map = Substitution()
        println(a)
        println(b)
        val result: Boolean = Formula.unify(a, b, map)
        println(map)
        return result
    }

    @Test
    fun testSample() {
        FormulaParser(lex(SAMPLE))
    }

    @Test
    fun testUnify1() {
        assertTrue { unify("p(d,X9); p(X10, c)") }
        assertEquals("c", map!!.get("X9").toString())
        assertEquals("d", map!!.get("X10").toString())
    }

    @Test
    fun testUnify2() {
        assertFalse(unify("p(X1); p(f(X1))"))
    }

    @Test
    fun testUnify3() {
        assertFalse(unify("p(f(X1)); p(X1)"))
    }

    @Test
    fun testUnify4() {
        assertTrue(unify("r(X3,sk4(X3)); r(X19,X20)"))

        assertEquals("X19", map!!.get("X3").toString())
        assertEquals("sk4(X19)", map!!.get("X20").toString())
    }

    @Test
    fun testUnify5() {
        assertTrue(unify("r(X122,sk35(X122)); r(X139,sk35(X139))"))
    }

    @Test
    fun testUninstantiate() {
        val f: Formula = Instantiation("X1", IdFormula("X2"))
        val g: Formula = f.uninstantiate("X1")
        assertEquals(IdFormula("X1"), g)
    }


    @Test
    fun testClose() {
        val f = NotFormula(
            NotFormula(
                Application(
                    "f",
                    Application(
                        "g",
                        IdFormula("c")
                    )
                )
            )
        )
        val g: Formula = NotFormula(Application("f", Application("g", IdFormula("c"))))

        assertTrue(g.closesWith(f))
        assertTrue(f.closesWith(g))
    }
}
