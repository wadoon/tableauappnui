import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import de.ukd.ilkd.tableau.FormulaParser
import de.ukd.ilkd.tableau.Node
import de.ukd.ilkd.tableau.lex
import de.uka.ilkd.tableau.ui.TableauPane
import de.ukd.ilkd.tableau.SAMPLE
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        Column {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primarySurface,
                title = {
                    Text("TableauApp")
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Refresh, "Restart")
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.PlayArrow, "Start auto")
                    }
                })
            /*Text("Draw something!")
            Canvas(Modifier) {
                drawRect(
                    Brush.radialGradient(
                        0F to Yellow,
                        .2F to Red,
                        .1F to Cyan
                    ), topLeft = Offset(10F, 10F), Size(120F, 120F)
                )
            }
             */
            val root = remember { mutableStateOf<Node?>(null) }

            if (root.value == null)
                showInputDialog(root)
            else
                TableauPane(root)
        }
    }
}

@Composable
fun showInputDialog(root: MutableState<Node?>) {
    val errorState = mutableStateOf<String?>(null)
    var error by errorState

    var inputFormulae by remember { mutableStateOf(SAMPLE) }

    Column {
        Text(error ?: "", fontWeight = FontWeight(900), fontSize = 16.sp)

        if (error != null) {
            Snackbar {
                Text(
                    error ?: "", fontWeight = FontWeight(900),
                    color = MaterialTheme.colors.error
                )
            }
        }

        TextField(
            minLines = 10,
            placeholder = {
                Text("Please enter formulae")
            },
            readOnly = false,
            singleLine = false,
            enabled = true,
            value = inputFormulae,
            onValueChange = {
                inputFormulae = it
                error = null
                val toks = lex(it)
                val fp = FormulaParser(toks)
                try {
                    val formulae = fp.Formulae()
                } catch (e: Exception) {
                    error = e.message
                }
            }
        )
        Button(onClick = {}) {
            Text("Proof!")
        }
    }
}
