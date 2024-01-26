import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import de.ukd.ilkd.tableau.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import de.ukd.ilkd.tableau.ui.TableauPane

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
            val history = remember { mutableStateOf<History?>(null) }

            if (root.value == null)
                showInputDialog(root, history)
            else
                TableauPane(root.value!!, history)
        }
    }
}

@Composable
fun showInputDialog(root: MutableState<Node?>, history: MutableState<History?>) {
    val errorState = remember { mutableStateOf<String?>(null) }
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
            //cursorBrush = SolidColor(Color.Black),
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
            },
        )

        Button(onClick = {
            error = null
            val toks = lex(inputFormulae)
            val fp = FormulaParser(toks)
            try {
                val formulae = fp.Formulae()

                Node.resetCounter()
                Constants.resetCounters()

                val first = formulae.first()
                val top = Node(0, first, null, null)
                root.value = top
                var last = top
                for (i in 1 until formulae.size) {
                    last = last.addSucc(formulae[i], null)
                }

                //comments.setText("Choose the open leaf to extend")
                history.value = History(top)
                println("parsed!")
            } catch (e: Exception) {
                error = e.message
                println("ERROR!!! $e")
            }
        }) {
            Text("Proof!!!")
        }
    }
}
