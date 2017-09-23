import net.torvald.colourutil.CIEYxy
import net.torvald.colourutil.CIEXYZUtil.toColor
import net.torvald.colourutil.ColourTemp
import java.io.File

/**
 * Only works with ArgyllCMS logs.
 *
 * Created by minjaesong on 2017-09-22.
 */
object SpotExt {

    private val dateFormat = Regex("""[A-Za-z]{3,} [A-Za-z]+ [0-9]{1,2} [0-9]{1,2}:[0-9]{2,}:[0-9]{2,} [^\n]+""")
    private val YxyFormat = Regex("""Yxy: [0-9]+\.[0-9]+ [0-9]+\.[0-9]+ [0-9]+\.[0-9]+(?=\n)""")
    private val daylightCCTStr = Regex("""Closest Daylight temperature  = [0-9]+K \(Delta E [0-9]+\.[0-9]+\)""")
    private val CCTwithinDay = Regex("""[0-9]+(?=K)""")
    private val deltaEwithinDay = Regex("""(?<=E )[0-9]+\.[0-9]+""")
    private val decimalFormat = Regex("""[0-9]+\.[0-9]+""")



    fun parseFile(infile: File): List<Entry>? {
        val rawText = infile.readText()

        val dateMatches = dateFormat.findAll(rawText)
        val matchesIterator = dateMatches.iterator()

        val entries = ArrayList<Entry>()

        // set our boundary.
        // boundary end will be:
        //  1. before next match if this matchResult is not last one
        //  2. before EOF        if this matchResult is last one
        var matchResult = matchesIterator.next() // put 0th elem in the temp val
        while (matchesIterator.hasNext()) {
            val matchResultNext = matchesIterator.next()

            val matchRange = matchResult.range.first until matchResultNext.range.first
            val matchedSection = rawText.subSequence(matchRange)
            matchResult = matchResultNext


            val entry = extractFromSection(matchedSection)
            if (entry != null) entries.add(entry)
        }

        // last match
        run {
            val matchRange = matchResult.range.first until rawText.length
            val matchedSection = rawText.subSequence(matchRange)


            val entry = extractFromSection(matchedSection)
            if (entry != null) entries.add(entry)
        }


        return if (entries.isEmpty()) null else entries
    }

    private fun extractFromSection(section: CharSequence): Entry? {
        // Section is too long to be considered as an accurate measurements
        // (usually caused by error reportings and/or script bugs)
        if (section.length >= 1020) return null // wild MAGICNUMBER appeared!


        val YxyMatch = YxyFormat.find(section)
        val CCTLineMatch = daylightCCTStr.find(section)
        val timeMatch = dateFormat.find(section)


        if (YxyMatch == null || CCTLineMatch == null || timeMatch == null)
            return null // the section is bust (measurement failed)


        val time = timeMatch.value
        val daylightCT = CCTwithinDay.find(CCTLineMatch.value)!!.value.toInt()
        val daylightCTDelta = deltaEwithinDay.find(CCTLineMatch.value)!!.value.toFloat()

        val yxyMatches = decimalFormat.findAll(YxyMatch.value)
        if (yxyMatches.count() != 3) throw InternalError()
        val YxyContainer = ArrayList<Float>()

        yxyMatches.forEach { YxyContainer.add(it.value.toFloat()) }


        return Entry(time, YxyContainer[0], YxyContainer[1], YxyContainer[2], daylightCT, daylightCTDelta)
    }

    val swatchLum = 0.5f

    fun List<Entry>.toHTML(): String {
        val sb = StringBuilder()

        sb.append("""<html><meta charset="utf-8"/><body><table border=1>""")
        sb.append("""<tr><td>Time</td><td>Y</td><td>x</td><td>y</td><td>sRGB</td><td>T<sub>D</sub> (K)</td><td>sRGB</td><td>ΔE<sub>D</sub></td></tr>""")


        this.forEach {
            sb.append("<tr><td>${it.time}</td>" +
                    "<td align='right'>${it.Luma}</td>" +
                    "<td align='right'>${it.x}</td>" +
                    "<td align='right'>${it.y}</td>" +
                    "<td bgcolor='${CIEYxy(swatchLum, it.x, it.y).toColor().toHTMLColorCode()}'></td>" +
                    "<td align='right'>${it.closestDaylightK}</td>" +
                    "<td bgcolor='${ColourTemp(it.closestDaylightK.toFloat(), swatchLum).toHTMLColorCode()}'></td>" +
                    "<td align='right'>${it.deltaE_d}</td></tr>")
        }


        sb.append("<tr><td>Time</td><td>Y</td><td>x</td><td>y</td><td>Y=${swatchLum}</td><td>T<sub>D</sub> (K)</td><td>Y=${swatchLum}</td><td>ΔE<sub>D</sub></td></tr>")


        return sb.toString()
    }



    data class Entry(val time: String, val Luma: Float,  val x: Float, val y: Float, val closestDaylightK: Int, val deltaE_d: Float)
}