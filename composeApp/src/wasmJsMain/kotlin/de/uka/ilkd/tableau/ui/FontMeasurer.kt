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
package de.uka.ilkd.tableau.ui

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp

/**
 * use instances of this little class to measure the size of strings.
 *
 * @author MU
 */
class FontMeasurer(private val style: TextStyle) {
    //@Composable


    /*private val g2d: Graphics2D = g2d

    fun getBounds(text: String?): Dimension {
        val frc: FontRenderContext = g2d.getFontRenderContext()
        val font: Font = g2d.getFont()
        val r: Rectangle2D = font.getStringBounds(text, frc)
        return Dimension(r.getWidth() as Int, r.getHeight() as Int)
    }

    fun getDescent(text: String?): Float {
        val frc: FontRenderContext = g2d.getFontRenderContext()
        val font: Font = g2d.getFont()
        return font.getLineMetrics(text, frc).getDescent()
    }*/
}
