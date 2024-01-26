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

import androidx.compose.material.AlertDialog
import androidx.compose.runtime.Composable

/**
 * Little dialog to allow to enter formulas
 */
/*
@Composable
fun FormulaDialog{
    AlertDialog() {
        var jLabel1: JLabel? = null
        var jOK: JButton? = null
        var jCancel: JButton? = null
        var jPanel: JPanel? = null
        var jArea: JTextArea? = null

        var isOK: Boolean = false
        private set

                init {
                    initGUI()
                }

        private fun initGUI() {
            try {
                val thisLayout: BorderLayout = BorderLayout()
                getContentPane().setLayout(thisLayout)
                this.setResizable(false)
                this.setModal(true)
                this.setTitle("Enter formulas")
                run {
                    jLabel1 = JLabel()
                    getContentPane().add(jLabel1, BorderLayout.NORTH)
                    val jLabel1Layout: BorderLayout = BorderLayout()
                    jLabel1.setLayout(jLabel1Layout)
                    jLabel1.setText("<html>Enter the formulas to start the tableau with.<br> Separate them by ';'</html>")
                    jLabel1.setBorder(
                        BorderFactory.createEmptyBorder(
                            10, 10, 10, 10
                        )
                    )
                }
                run {
                    jArea = JTextArea()
                    getContentPane().add(jArea, BorderLayout.CENTER)
                    jArea.setBorder(
                        BorderFactory.createBevelBorder(BevelBorder.LOWERED)
                    )
                }
                run {
                    jPanel = JPanel()
                    getContentPane().add(jPanel, BorderLayout.SOUTH)
                    run {
                        jOK = JButton()
                        jPanel.add(jOK)
                        jOK.setText("OK")
                        jOK.addActionListener(object : ActionListener() {
                            fun actionPerformed(evt: ActionEvent?) {
                                this.isOK = true
                                setVisible(false)
                            }
                        })
                    }
                    run {
                        jCancel = JButton()
                        jPanel.add(jCancel)
                        jCancel.setText("Cancel")
                        jCancel.addActionListener(object : ActionListener() {
                            fun actionPerformed(evt: ActionEvent?) {
                                setVisible(false)
                            }
                        })
                    }
                }
                this.setSize(377, 300)
                getRootPane().setDefaultButton(jOK)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        var input: String?
        get() = jArea.getText()
        set(string) {
            jArea.setText(string)
        }

        fun unsetOK() {
            isOK = false
        }

        fun go(line: Int, col: Int) {
            try {
                val linestart: Int = jArea.getLineStartOffset(line - 1)
                jArea.setCaretPosition(linestart + col - 1)
            } catch (e: BadLocationException) {
                e.printStackTrace()
                // just ignore it
            }
        }
    }
}
*/