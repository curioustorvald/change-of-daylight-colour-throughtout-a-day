package net.torvald.colourutil

import net.torvald.colourutil.CIEXYZUtil.toColor


/**
 * RGB- and CIE-Modeled CCT calculator
 * Created by minjaesong on 2016-07-26.
 */
object ColourTemp {

    /** returns CIExyY-based colour converted to slick.color
     * @param CIE_Y 0.0 - 1.0+ */
    operator fun invoke(temp: Float, CIE_Y: Float): Color =
            CIEXYZUtil.colourTempToXYZ(temp, CIE_Y).toColor()
}