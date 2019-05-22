package view

import com.mxgraph.view.mxGraph
import queue.Algo1
import queue.Algo15
import queue.Algo4
import queue.Algo8
import system.System
import system.gantt.GanttDiagram
import system.schedule.Scheduler3
import system.schedule.Scheduler2
import system.schedule.Scheduler4
import util.reformattedData
import java.awt.Dimension
import javax.swing.*
import javax.swing.JScrollPane

class SimulationTab(private val tasksGraph: mxGraph, private val systemGraph: mxGraph) : JPanel() {

    private val taskData
        get() = tasksGraph.reformattedData

    private val systemData
        get() = systemGraph.reformattedData

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        val gnattDiagram = GanttDiagram()
        add(JScrollPane(gnattDiagram))

        val toolsPanel = JPanel().apply { maximumSize = Dimension(700, 36) }
        //region Adding queue tools to panel
0
        val createQueueButton = JButton("Create queue")
        toolsPanel.add(createQueueButton)

        val firstAlgoRB = JRadioButton("1").apply { actionCommand = text }
        val fourthAlgoRB = JRadioButton("4").apply { actionCommand = text }
        val eightAlgoRB = JRadioButton("8").apply { actionCommand = text }

        val fifteenthAlgoRB = JRadioButton("15", true).apply { actionCommand = text }
        val queueSelector = ButtonGroup().apply {
            add(firstAlgoRB)
//            add(fourthAlgoRB)
            add(eightAlgoRB)
//            add(fifteenthAlgoRB)
        }
        toolsPanel.apply {
            add(firstAlgoRB)
//            add(fourthAlgoRB)
            add(eightAlgoRB)
//            add(fifteenthAlgoRB)
        }
        createQueueButton.addActionListener {
            createQueue(queueSelector)
                .map { taskData. toRealId(it) }
                .joinToString(prefix = "Queue by ${queueSelector.selection.actionCommand} algorithm: ", separator = " > ")
                .apply(::println)
        }

        add(toolsPanel)
        //endregion

        //region Adding modeling tools to panel
        toolsPanel.add(JLabel("    Links: "))

        val linksField = JTextField("2", 4)
        toolsPanel.add(linksField)

//        val isFullDuplexCheckbox = JCheckBox("Full duplex")
//        toolsPanel.add(isFullDuplexCheckbox)

        val fourthSchedulerRB = JRadioButton("4").apply { actionCommand = text }
        val secondSchedulerRB = JRadioButton("2", true).apply { actionCommand = text }
        val thirdSchedulerRB = JRadioButton("3", true).apply { actionCommand = text }
        val schedulerSelector = ButtonGroup().apply {
//            add(fourthSchedulerRB)
//            add(secondSchedulerRB)
            add(thirdSchedulerRB)
        }
        toolsPanel.apply {
//            add(secondSchedulerRB)
            add(thirdSchedulerRB)
//            add(fourthSchedulerRB)
        }

        val simulateButton = JButton("Simulate")
        simulateButton.addActionListener {
            if (SystemTab.checkForLonely(systemData)) return@addActionListener

            val schedulerNumber = schedulerSelector.selection.actionCommand
            val scheduler = schedulerAlgos[schedulerNumber]!!
            val queue = createQueue(queueSelector)
            val system = System(systemData, linksField.text.toInt(), true)

            system.placeTasks(taskData, queue, scheduler)
            gnattDiagram.repaintForSystem(system)
        }
        toolsPanel.add(simulateButton)

        add(toolsPanel)
        //endregion
    }

    private val schedulerAlgos = mapOf("4" to Scheduler4, "2" to Scheduler2, "3" to Scheduler3)

    private val queueAlgos = mapOf("4" to Algo4, "15" to Algo15, "1" to Algo1, "8" to Algo8)

    private fun createQueue(queueSelector: ButtonGroup): List<Int> {
        val algoNumber = queueSelector.selection.actionCommand
        val algo = queueAlgos[algoNumber]!!
        return algo.createQueue(taskData)
    }
}