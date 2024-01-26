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
package de.ukd.ilkd.tableau.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// TODO: Auto-generated Javadoc
/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI
 * Builder, which is free for non-commercial use. If Jigloo is being used
 * commercially (ie, by a corporation, company or business for any purpose
 * whatever) then you should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details. Use of Jigloo implies
 * acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN
 * PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR
 * ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
@Composable
fun TableauApplet() {
    Column {
        TopAppBar(
            elevation = 4.dp,
            title = {
                Text("I'm a TopAppBar")
            },
            backgroundColor = MaterialTheme.colors.primarySurface,
            navigationIcon = {
                IconButton(onClick = {/* Do Something*/ }) {
                    Icon(Icons.Filled.ArrowBack, null)
                }
            }, actions = {
                IconButton(onClick = {/* Do Something*/ }) {
                    Icon(Icons.Filled.Share, null)
                }
                IconButton(onClick = {/* Do Something*/ }) {
                    Icon(Icons.Filled.Settings, null)
                }
            })
        Text("Hello World")
    }


    /*
    var southPanel: JToolBar? = null
    var jNew: JButton? = null
    var jSample: JButton? = null
    var jComment: JLabel? = null
    var tableauComponent: TableauPane? = null
    var jScrollPane: JScrollPane? = null
    var jUnicode: JToggleButton? = null
    var jInstance: JButton? = null
    var jAuto: JButton? = null
    var jPrint: JButton? = null
    var jLatex: JButton? = null
    var jExport: JButton? = null
    var jUndo: JButton? = null
    val jModelSearch: JButton? = null

    var lastInput = ""

    //     ModelSearch modelSearchThread;
    val guiPanel: JComponent

    /*
     * read and set applet parameters
     */

    fun init() {
        val showAnces: String = getParameter("showancestor")
        if ("false".equals(showAnces)) {
            SHOW_ANCESTORS = false
        }

        val autorun: String = getParameter("allowautorun")
        if ("true".equals(autorun)) {
            ALLOW_AUTORUN = true
            jAuto.setEnabled(ALLOW_AUTORUN)
        }

        val allowunification: String = getParameter("allowunification")
        if ("true".equals(allowunification)) {
            TableauPane.ALLOW_UNIFICATION = true
        }

        val absolutetx: String = getParameter("absolutetex")
        if ("false".equals(absolutetx)) {
            ABSOLUTE_TEX_EXPORT = false
        }

        try {
            TableauPane.FONT_SIZE = Integer.parseInt(getParameter("fontsize"))
        } catch (ex: Exception) {
            // okay, just don't set it
        }

        getContentPane().add(guiPanel)
    }

    /**
     * Inits the gui.
     * @return the freshly created component to represent this gui
     */
    fun makeGUIPanel(): JComponent {
        try {
            val result: JPanel = JPanel()
            val thisLayout: BorderLayout = BorderLayout()
            result.setLayout(thisLayout)
            run {
                southPanel = JToolBar()
                result.add(southPanel, BorderLayout.NORTH)
                run {
                    jSample = JButton()
                    southPanel.add(jSample)
                    jSample.setIcon(mkImg("sample"))
                    jSample.setToolTipText("Load a sample")
                    jSample.addActionListener(object : ActionListener() {
                        fun actionPerformed(evt: ActionEvent) {
                            jSampleActionPerformed(evt)
                        }
                    })
                }
                run {
                    jNew = JButton()
                    southPanel.add(jNew)
                    jNew.setIcon(mkImg("new"))
                    jNew.setToolTipText("New Tableau")
                    jNew.addActionListener(object : ActionListener() {
                        fun actionPerformed(evt: ActionEvent) {
                            jNewActionPerformed(evt)
                        }
                    })
                }
                run {
                    jInstance = JButton()
                    southPanel.add(jInstance)
                    jInstance.setIcon(mkImg("instance"))
                    jInstance.setToolTipText("Instantiate free variable")
                    jInstance.addActionListener(object : ActionListener() {
                        fun actionPerformed(evt: ActionEvent) {
                            jInstanceActionPerformed(evt)
                        }
                    })
                }
                run {
                    jUnicode = JToggleButton()
                    jUnicode.setIcon(mkImg("unicode"))
                    jUnicode.setToolTipText("Use unicode characters")
                    jUnicode.setSelected(true)
                    southPanel.add(jUnicode)
                    jUnicode.addActionListener(object : ActionListener() {
                        fun actionPerformed(evt: ActionEvent) {
                            unicodeChanged(evt)
                        }
                    })
                }
                run {
                    jUndo = JButton()
                    southPanel.add(jUndo)
                    jUndo.setIcon(mkImg("undo"))
                    jUndo.setToolTipText("Undo the last instantiation or rule application")
                    jUndo.addActionListener(object : ActionListener() {
                        fun actionPerformed(evt: ActionEvent?) {
                            tableauComponent!!.undo()
                        }
                    })
                }
                run {
                    jExport = JButton()
                    southPanel.add(jExport)
                    jExport.setIcon(mkImg("export"))
                    jExport.setToolTipText("Export to PNG")
                    jExport.addActionListener(object : ActionListener() {
                        fun actionPerformed(evt: ActionEvent) {
                            jExportActionPerformed(evt)
                        }
                    })
                }
                run {
                    jLatex = JButton()
                    southPanel.add(jLatex)
                    jLatex.setIcon(mkImg("tex"))
                    jLatex.setToolTipText("Export to LaTeX")
                    jLatex.addActionListener(object : ActionListener() {
                        fun actionPerformed(evt: ActionEvent) {
                            jLatexActionPerformed(evt)
                        }
                    })
                }
                run {
                    jPrint = JButton()
                    southPanel.add(jPrint)
                    jPrint.setIcon(mkImg("print"))
                    jPrint.setToolTipText("Print")
                    //jPrint.setEnabled(false);
                    jPrint.addActionListener(object : ActionListener() {
                        fun actionPerformed(evt: ActionEvent) {
                            jPrintActionPerformed(evt)
                        }
                    })
                }
                run {
                    jAuto = JButton()
                    southPanel.add(jAuto)
                    jAuto.setIcon(mkImg("go"))
                    jAuto.setEnabled(ALLOW_AUTORUN)
                    jAuto.addActionListener(object : ActionListener() {
                        fun actionPerformed(evt: ActionEvent) {
                            jAutoActionPerformed(evt)
                        }
                    })
                }
            }
            run {
                jComment = JLabel()
                result.add(jComment, BorderLayout.SOUTH)
                jComment.setText("Visualisation of the tableau calculus - Mattias Ulbrich 2007-2016 - #" + BUILD)
            }
            run {
                jScrollPane = JScrollPane()
                jScrollPane.getVerticalScrollBar().setUnitIncrement(20)
                result.add(jScrollPane, BorderLayout.CENTER)
                run {
                    tableauComponent = TableauPane(jComment)
                    jScrollPane.setViewportView(tableauComponent)
                }
            }

            return result
        } catch (e: Exception) {
            e.printStackTrace()
            return JLabel("An exception occurred (s. console): " + e.getMessage())
        }
    }

    /**
     * Open a formula dialog and let the user enter a new formula.
     *
     * @param evt
     * the event
     */
    fun jNewActionPerformed(evt: ActionEvent) {
        val fd: FormulaDialog = FormulaDialog(JFrame())
        fd.setInput(lastInput)
        fd.setVisible(true)
        while (fd.isOK()) {
            fd.unsetOK()
            lastInput = fd.getInput()
            val r: Reader = StringReader(lastInput)
            val parser: FormulaParser = FormulaParser(r)
            try {
                val f: Array<Formula?> = parser.Formulae()
                tableauComponent!!.init(f)
            } catch (ex: ParseException) {
                fd.go(ex.currentToken.beginLine, ex.currentToken.beginColumn)
                JOptionPane.showMessageDialog(
                    null, ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE
                )
                fd.setVisible(true)
            } catch (e: Exception) {
                e.printStackTrace()
                JOptionPane.showMessageDialog(
                    null, e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE
                )
                fd.setVisible(true)
            }
        }
    }

    /**
     * Act if the unicode button has been pressed.
     *
     * @param evt
     * the evt, not needed
     */
    fun unicodeChanged(evt: ActionEvent) {
        Constants.USE_UNICODE = jUnicode.isSelected()
        tableauComponent!!.refresh()
    }

    /**
     * Instantiates a new tableau applet. Initialise the gui.
     */
    init {
        guiPanel = makeGUIPanel()
    }

    /**
     * Restart applet with an instance of the sample formula.
     *
     * @param evt
     * the evt, not needed
     */
    fun jSampleActionPerformed(evt: ActionEvent) {
        val parser: FormulaParser = FormulaParser(StringReader(SAMPLE))
        try {
            val f: Array<Formula?> = parser.Formulae()
            tableauComponent!!.init(f)
        } catch (e: ParseException) {
            e.printStackTrace()
            JOptionPane.showMessageDialog(
                null, e.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    /**
     * Open the variabel instantiation dialog
     *
     * @param evt
     * the evt, not needed
     */
    fun jInstanceActionPerformed(evt: ActionEvent) {
        val assign: String = JOptionPane.showInputDialog("Enter the instantiation similar to X1 = f(g(X2),c)") ?: return

        val parser: FormulaParser = FormulaParser(StringReader(assign))
        try {
            val inst: Instantiation = parser.Instantiation()
            val `var`: String = inst.getInstantiatedVariable()
            tableauComponent!!.instantiate(`var`, inst)
            tableauComponent!!.refresh()
        } catch (e: Exception) {
            e.printStackTrace()
            JOptionPane.showMessageDialog(
                null, e.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    /**
     * initiate automatic proofing. delegated to the [.tableauComponent].
     *
     * @param evt
     * the evt
     * @see TableauPane.automaticProve
     */
    fun jAutoActionPerformed(evt: ActionEvent) {
        tableauComponent!!.automaticProve()
    }

    protected fun jModelSearchActionPerformed(e: ActionEvent?) {
    //        if(modelSearchThread == null || !modelSearchThread.isAlive()) {
    //            int bound = Integer.parseInt(JOptionPane.showInputDialog(
    //            "Bound for the size of models to consider?"));
    //            modelSearchThread = new ModelSearch(lastInput, bound);
    //            modelSearchThread.run();
    //        } else {
    //            modelSearchThread.interrupt();
    //        }
    }

    /**
     * open the export file chooser and save current view as PNG, GIF or JPG.
     *
     * @param evt
     * the evt
     */
    fun jExportActionPerformed(evt: ActionEvent) {
        try {
            val jfc: JFileChooser = JFileChooser(".")
            jfc.removeChoosableFileFilter(jfc.getAcceptAllFileFilter())
            jfc.addChoosableFileFilter(FileNameExtensionFilter("GIF image file", "GIF"))
            jfc.addChoosableFileFilter(FileNameExtensionFilter("JPEG image file", "JPG"))
            jfc.addChoosableFileFilter(FileNameExtensionFilter("PNG image file", "PNG"))
            if (jfc.showSaveDialog(null) === JFileChooser.APPROVE_OPTION) {
                val outFile: File = jfc.getSelectedFile()
                val d: Dimension = tableauComponent.getOptimalSize()
                    ?: throw IllegalStateException("The tableau is empty - cannot export")
                val im: BufferedImage = BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB)
                val g: Graphics = im.getGraphics()
                g.setColor(Color.white)
                g.fillRect(0, 0, d.width, d.height)
                tableauComponent.paint(im.getGraphics())
                if (jfc.getFileFilter() is FileNameExtensionFilter) {
                    val extFilter: FileNameExtensionFilter = jfc.getFileFilter() as FileNameExtensionFilter
                    ImageIO.write(im, extFilter.getExtensions().get(0), outFile)
                } else {
                    // should not happen actually
                    throw IllegalStateException("You need to select a file format")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            JOptionPane.showMessageDialog(
                null,
                e.toString(),
                "Error", JOptionPane.ERROR_MESSAGE
            )
        }
    }

    /**
     * Export a latex .tex file.
     *
     * @param evt
     * the evt, ignored
     */
    fun jLatexActionPerformed(evt: ActionEvent) {
        try {
            val jfc: JFileChooser = JFileChooser(".")
            if (jfc.showSaveDialog(null) === JFileChooser.APPROVE_OPTION) {
                val outFile: File = jfc.getSelectedFile()
                val fw: FileWriter = FileWriter(outFile)
                catResource(fw, "latex/header.latex")
                val sb = StringBuilder()
                tableauComponent!!.toLatex(sb, ABSOLUTE_TEX_EXPORT)
                fw.write(sb.toString())
                catResource(fw, "latex/footer.latex")
                fw.close()
            }
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                null,
                e.toString(),
                "Error", JOptionPane.ERROR_MESSAGE
            )
        }
    }

    /*
     * open a resource and paste its text to a writer.
     */
    @Throws(IOException::class)
    fun catResource(w: Writer, resourceName: String) {
        val url: URL = getClass().getResource(resourceName)
            ?: throw FileNotFoundException("Resource not found: $resourceName")
        val buf = CharArray(1024)
        val r: Reader = InputStreamReader(url.openStream())
        var read: Int = r.read(buf, 0, 1024)
        while (read != -1) {
            w.write(buf, 0, read)
            read = r.read(buf, 0, 1024)
        }
        r.close()
    }

    /**
     * print to printer
     *
     * @param evt
     * the evt
     */
    fun jPrintActionPerformed(evt: ActionEvent) {
        val printJob: PrinterJob = PrinterJob.getPrinterJob()
        printJob.setPrintable(tableauComponent)
        if (printJob.printDialog()) {
            try {
                printJob.print()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    /*
     * look up an img. return an error icon on other cases.
     */
    fun mkImg(name: String): Icon {
        try {
            return ImageIcon(getClass().getResource("img/$name.gif"))
        } catch (ex: Exception) {
            System.err.println("Missing Icon: $name")
            return object : Icon() {
                val iconHeight: Int
                    get() = 24

                val iconWidth: Int
                    get() = 24

                fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
                    g.setColor(Color.black)
                    g.drawString("?", x, y + 20)
                }
            }
        }
    }

    fun setComment(string: String?) {
        jComment.setText(string)
    }

    companion object {
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

        /**
         * Main method to display this JApplet inside a new JFrame.
         *
         * First read and set system properties instead of applet parameters.
         */
        fun main(args: Array<String?>?) {
            // set system properties

            SHOW_ANCESTORS = Boolean.parseBoolean(
                System.getProperty(
                    "tablet.showancestor", Boolean.toString(
                        SHOW_ANCESTORS
                    )
                )
            )
            ALLOW_AUTORUN = Boolean.parseBoolean(
                System.getProperty(
                    "tablet.allowautorun", Boolean.toString(
                        ALLOW_AUTORUN
                    )
                )
            )
            ABSOLUTE_TEX_EXPORT = Boolean.parseBoolean(
                System.getProperty(
                    "tablet.absolutetex", Boolean.toString(
                        ABSOLUTE_TEX_EXPORT
                    )
                )
            )
            TableauPane.FONT_SIZE = Integer.getInteger("tablet.fontsize", TableauPane.FONT_SIZE)
            TableauPane.ALLOW_UNIFICATION = Boolean.parseBoolean(
                System.getProperty(
                    "tablet.allowunification",
                    Boolean.toString(TableauPane.ALLOW_UNIFICATION)
                )
            )

            val frame: JFrame = JFrame("Tableau Proof")
            val inst = TableauApplet()
            frame.getContentPane().add(inst.guiPanel)

            frame.setSize(600, 400)
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
            frame.setVisible(true)
        }
    }
     */
}
