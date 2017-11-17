import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.GridLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.*

import SpotExt.toHTML
import java.io.IOException

/**
 *
 * Synopsis: java -jar spotextgui.java infile outfile [type: html, css, 8bpp, 16bpp]
 *
 * Created by minjaesong on 2017-09-20.
 */
fun main(args: Array<String>) {
    if (args.size == 0) {
        SpotExtGUI()
    }
    else if (args.size == 1) {
        println("Synopsis: java -jar spotextgui.java infile outfile [html, css, 8bpp, 16bpp]; HTML is default operation")
    }
    else {
        val logfile = File(args[0])
        val outfile = File(args[1])

        outfile.writeText(SpotExt.parseFile(logfile)!!.toHTML())
    }
}


class SpotExtGUI : JFrame("Spotread Extractor GUI") {


    val buttonExportHTML = JButton("HTML")
    val buttonExportCSV = JButton("CSV")
    val buttonExportColourmap8 = JButton("8bpp")
    val buttonExportColourmap16 = JButton("16bpp")

    val buttonIOIn = JButton("Browse")
    val buttonIOOut = JButton("Browse")

    val textAreaInfile = JTextField()
    val textAreaOutfile = JTextField()

    val statusMsg = JTextField("OK")

    val msgSelectingInfile = "Browse input file"
    val msgSelectingOutfile = "Browse output file"
    val msgInputFileLoaded = "Input file loaded"
    val msgOutputfileSelected = "Output file selected"
    val msgCanceled = "Operation cancelled"

    val msgNoEntryFound = "No entries found in input file"
    val msgWriteError = "Failed to write: "
    val msgWorkingOn = "Working on..."
    val msgInOutIdentical = "Input and output file is identical!"

    val msgExportHTML = "HTML exported"
    val msgExportCSV = "CSV exported"
    val msgExport8 = "8bpp colour map exported"
    val msgExport16 = "16bpp colour map exported"


    var infile: File? = null
    var outfile: File? = null

    init {
        this.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE


        val mainPanel = JPanel()
        mainPanel.layout = BorderLayout()

        statusMsg.isEditable = false


        val ioPanel = JPanel()
        ioPanel.layout = BorderLayout()

        val ioLabels = JPanel()
        ioLabels.layout = GridLayout(2, 1)
        ioLabels.add(JLabel("Infile"))
        ioLabels.add(JLabel("Outfile"))

        val ioFilenames = JPanel()
        ioFilenames.layout = GridLayout(2, 1)
        ioFilenames.add(textAreaInfile)
        ioFilenames.add(textAreaOutfile)
        textAreaInfile.isEditable = false
        textAreaOutfile.isEditable = false

        val ioBrowseButtons = JPanel()
        ioBrowseButtons.layout = GridLayout(2, 1)
        ioBrowseButtons.add(buttonIOIn)
        ioBrowseButtons.add(buttonIOOut)

        ioPanel.add(ioLabels, BorderLayout.LINE_START)
        ioPanel.add(ioFilenames, BorderLayout.CENTER)
        ioPanel.add(ioBrowseButtons, BorderLayout.LINE_END)


        val exportPanel = JPanel()
        exportPanel.layout = FlowLayout()

        exportPanel.add(buttonExportHTML)
        exportPanel.add(buttonExportCSV)
        exportPanel.add(buttonExportColourmap8)
        exportPanel.add(buttonExportColourmap16)


        mainPanel.add(ioPanel, BorderLayout.PAGE_START)
        mainPanel.add(exportPanel, BorderLayout.CENTER)
        mainPanel.add(statusMsg, BorderLayout.PAGE_END)


        this.add(mainPanel)
        this.setSize(350, 146)
        this.isVisible = true


        ////////////////////////////////////////////////////////////////////

        buttonIOIn.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val fileChooser = JFileChooser()
                when (fileChooser.showOpenDialog(null)) {
                    JFileChooser.APPROVE_OPTION -> {
                        infile = fileChooser.selectedFile
                        statusMsg.text = msgInputFileLoaded
                        textAreaInfile.text = infile!!.canonicalPath
                    }
                    JFileChooser.CANCEL_OPTION -> {
                        statusMsg.text = msgCanceled
                    }
                    JFileChooser.ERROR_OPTION -> {
                        //showerror
                    }
                }
            }
        })

        buttonIOOut.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val fileChooser = JFileChooser()
                when (fileChooser.showSaveDialog(null)) {
                    JFileChooser.APPROVE_OPTION -> {
                        if (fileChooser.selectedFile != infile) {
                            outfile = fileChooser.selectedFile
                            statusMsg.text = msgOutputfileSelected
                            textAreaOutfile.text = outfile!!.canonicalPath
                        }
                        else {
                            JOptionPane.showMessageDialog(null, msgInOutIdentical, "Error", JOptionPane.ERROR_MESSAGE)
                        }
                    }
                    JFileChooser.CANCEL_OPTION -> {
                        statusMsg.text = msgCanceled
                    }
                    JFileChooser.ERROR_OPTION -> {
                        //showerror
                    }
                }
            }
        })

        buttonExportHTML.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (infile != null && outfile != null) {
                    try {
                        statusMsg.text = msgWorkingOn
                        outfile!!.writeText(SpotExt.parseFile(infile!!)!!.toHTML())
                        statusMsg.text = msgExportHTML
                    }
                    catch (n: KotlinNullPointerException) {
                        statusMsg.text = msgNoEntryFound
                    }
                    catch (io: IOException) {
                        statusMsg.text = msgWriteError + io.message
                    }
                }
            }
        })
    }

}