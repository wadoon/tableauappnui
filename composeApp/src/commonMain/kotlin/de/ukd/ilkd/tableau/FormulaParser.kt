package de.ukd.ilkd.tableau

enum class TokenTypes(val regex: Regex) {
    Whitespace("[ \t\n\r]".toRegex()),
    Comment("#(~[\n\r])*".toRegex()),
    SKOLEM("sk[0-9]+".toRegex()),
    FREEVAR("X[0-9]+".toRegex()),
    FORALL("A".toRegex()),
    EXISTS("E".toRegex()),
    AND("&".toRegex()),
    LEFTPAREN("\\(".toRegex()),
    RIGHTPAREN("\\)".toRegex()),
    COMMA(",".toRegex()),
    SEMICOLON(";".toRegex()),
    OR("\\|".toRegex()),
    NOT("~".toRegex()),
    IMPLIES("->".toRegex()),
    EQUIV("<->".toRegex()),
    DOT("\\.".toRegex()),
    EQUALS("=".toRegex()),
    ID("([a-zA-Z0-9])+".toRegex()),
    OTHER("[\u0000-\uFFFF]".toRegex()),
    EOF("$".toRegex())
}

data class Token(val type: TokenTypes, val input: String, val region: IntRange) {
    val image: String
        get() = input.substring(region)
}

fun lex(s: String): List<Token> {
    val toks = mutableListOf<Token>()
    val types = TokenTypes.entries
    var pos = 0
    outer@ while (pos < s.length) {
        for (c in types) {
            val m = c.regex.matchAt(s, pos)
            if (m != null) {
                toks.add(Token(c, s, m.range))
                pos += m.value.length
                continue@outer
            }
        }
    }
    toks.add(Token(TokenTypes.EOF, s, s.length..s.length))
    return toks
}

/**
 *
 * @author Alexander Weigl
 * @version 1 (21.01.24)
 */
class FormulaParser(private val stream: List<Token>) {
    var fullMode: Boolean = true
    private var pos = 0

    val current
        get() = stream[pos]

    fun eof(): Boolean = pos >= stream.size
    fun lookahead(tt: TokenTypes, vararg more: TokenTypes): Boolean {
        val current = stream[pos]
        val a = current.type == tt
        return if (more.isEmpty() || a) a
        else more.any { current.type == it }
    }

    fun consume(tt: TokenTypes): Token {
        val c = current
        require(lookahead(tt))
        next()
        skipWhitespace()
        return c
    }

    fun lookaheadAndConsume(tt: TokenTypes) =
        if (lookahead(tt)) {
            consume(tt)
            true
        } else false

    private fun next() =
        if (lookahead(TokenTypes.EOF)) pos else ++pos

    private fun skipWhitespace() {
        while (lookahead(TokenTypes.Whitespace))
            next()
    }

    fun Formulae(): List<Formula> {
        val list = mutableListOf<Formula>()
        do {
            val f = EquivalenceFormula()
            list.add(f)
            if (eof()) break
            if (lookahead(TokenTypes.SEMICOLON)) {
                consume(TokenTypes.SEMICOLON)
                continue
            } else break
        } while (true)
        return list
    }

    fun BaseFormula(): Formula {
        if (lookahead(TokenTypes.NOT)) {
            return NegatedFormula()
        }

        if (lookahead(TokenTypes.FORALL, TokenTypes.EXISTS)) {
            return QuantifiedFormula()
        }

        if (lookahead(TokenTypes.LEFTPAREN)) {
            consume(TokenTypes.LEFTPAREN)
            val f = EquivalenceFormula()
            consume(TokenTypes.RIGHTPAREN)
            return f
        }

        return PredicateOrTerm()
    }

    fun QuantifiedFormula(): Formula {
        val q = when {
            lookahead(TokenTypes.FORALL) -> {
                consume(TokenTypes.FORALL)
                Quantor.FORALL
            }

            lookahead(TokenTypes.EXISTS) -> {
                consume(TokenTypes.EXISTS)
                Quantor.EXISTS
            }

            else -> {
                error("Unexpected token: ${current.type}")
            }
        }

        val variable = consume(TokenTypes.ID)
        consume(TokenTypes.DOT)
        val f = BaseFormula()
        return QuantorFormula(q, variable.image, f)
    }

    fun NegatedFormula(): Formula {
        consume(TokenTypes.NOT)
        val f = BaseFormula()
        return NotFormula(f)
    }

    fun PredicateOrTerm(): Formula {
        if (lookahead(TokenTypes.FREEVAR)) {
            val id = consume(TokenTypes.FREEVAR)
            return IdFormula(id.image)
        }

        val formulas = mutableListOf<Formula>()
        val id =
            when {
                lookahead(TokenTypes.ID) -> consume(TokenTypes.ID)
                lookahead(TokenTypes.SKOLEM) -> consume(TokenTypes.SKOLEM)
                else -> error("Unexpected token")
            }
        if (lookahead(TokenTypes.LEFTPAREN)) {
            consume(TokenTypes.LEFTPAREN)
            do {
                val f = EquivalenceFormula()
                formulas.add(f)
                if (lookahead(TokenTypes.COMMA)) {
                    consume(TokenTypes.COMMA)
                    continue
                } else {
                    break
                }
            } while (true)

            consume(TokenTypes.RIGHTPAREN)
        }

        if (formulas.isEmpty())
            return IdFormula(id.image)
        else
            return Application(id.image, formulas)


    }

    fun EquivalenceFormula(): Formula {
        val f = ImplicationFormula()
        if (lookaheadAndConsume(TokenTypes.EQUIV)) {
            val g = EquivalenceFormula()
            return BinopFormula(Operator.EQUIV, f, g)
        }
        return f
    }

    fun ImplicationFormula(): Formula {
        val f = DisjunctionFormula()
        if (lookaheadAndConsume(TokenTypes.IMPLIES)) {
            val g = ImplicationFormula()
            return BinopFormula(Operator.IMPL, f, g)
        }
        return f
    }

    fun DisjunctionFormula(): Formula {
        val f = ConjunctionFormula()
        if (lookaheadAndConsume(TokenTypes.OR)) {
            val g = DisjunctionFormula()
            return BinopFormula(Operator.OR, f, g)
        }
        return f
    }

    fun ConjunctionFormula(): Formula {
        val f = BaseFormula()
        if (lookaheadAndConsume(TokenTypes.AND)) {
            val g = ConjunctionFormula()
            return BinopFormula(Operator.AND, f, g)
        }
        return f
    }

    fun Instantiation(): Instantiation {
        val t = consume(TokenTypes.FREEVAR)
        consume(TokenTypes.EQUALS)
        val f = EquivalenceFormula()
        consume(TokenTypes.EOF)
        return Instantiation(t.image, f)
    }
}
