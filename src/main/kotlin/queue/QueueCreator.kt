package queue

import util.ReformattedData
import java.util.*

interface QueueCreator {
    fun createQueue(sourceData: ReformattedData): List<Int>
}

object Algo4 : QueueCreator {
    override fun createQueue(sourceData: ReformattedData): List<Int> {
        val linksMatrix = sourceData.linksMatrix

        val sortedByCriticalPathGroups = IntRange(0, linksMatrix.lastIndex)
                .map { findCriticalPathByNodes(it, sourceData) }
                .groupBy(MutableList<Int>::size)
                .toSortedMap(Comparator { o1, o2 -> o2 - o1 })

        return sortedByCriticalPathGroups.values.map { sameCPValuesList ->
            sameCPValuesList.sortedByDescending { getConnectivity(it.first(), linksMatrix) }
        }.flatten().map { it.first() }
    }

    private fun getConnectivity(vertexId: Int, linksMatrix: Array<IntArray>): Int {
        val outgoing = linksMatrix[vertexId].count { it != 0 }
        val incoming = linksMatrix.count { it[vertexId] != 0 }
        return outgoing + incoming
    }
}

object Algo8 : QueueCreator {

    override fun createQueue(sourceData: ReformattedData): List<Int> {
        val linksMatrix = sourceData.linksMatrix


        val sortedByCriticalPathGroups = IntRange(0, linksMatrix.lastIndex)
                .map { findCriticalPathByNodesBackword(it, sourceData) }
                .groupBy(MutableList<Int>::size)
                .toSortedMap(Comparator { o1, o2 -> o1 - o2 })

        return sortedByCriticalPathGroups.values.map { sameCPValuesList ->
            sameCPValuesList.sortedByDescending { Algo8.getConnectivity(it.first(), sourceData) }
        }.flatten().map { it.first() }
    }

    private fun getConnectivity(vertexId: Int, data: ReformattedData): Int {
        return data.matrixIdToValueMap[vertexId] ?: 0
    }
}

object Algo15 : QueueCreator {
    override fun createQueue(sourceData: ReformattedData) = sourceData.matrixIdToValueMap.toList()
            .sortedBy { (_, value) -> value }
            .map { it.first }
}

object Algo1 : QueueCreator {
    override fun createQueue(sourceData: ReformattedData): List<Int> {
        val indices = sourceData.linksMatrix.indices

        val criticalPathsByNodes = indices.map { findCriticalPathByNodes(it, sourceData).size }
        val criticalPathsByDifficulty = indices.map { matrixId ->
            findCriticalPathByDifficulty(matrixId, sourceData).map { sourceData.matrixIdToValueMap[it]!! }.sum()
        }

        val maxByNodes = criticalPathsByNodes.max()!!.toDouble()
        val maxByDifficulty = criticalPathsByDifficulty.max()!!.toDouble()

        val priorities = indices.mapIndexed { index, _ -> criticalPathsByNodes[index] / maxByNodes + criticalPathsByDifficulty[index] / maxByDifficulty }
        println("prop -> $priorities,")
        return priorities.withIndex().sortedByDescending { it.value }.map { it.index }
    }
}

private fun findCriticalPath(vertexId: Int, sourceData: ReformattedData, isMoreCritical: (List<Int>, Stack<Int>) -> Boolean): MutableList<Int> {
    val linksMatrix = sourceData.linksMatrix

    fun visit(startId: Int, stack: Stack<Int>, criticalPath: MutableList<Int>) {
        val row = linksMatrix[startId]
        val couldVisit = row.withIndex().filter { it.value != 0 }.map { it.index }

        couldVisit.forEach { toVisit ->
            stack.push(toVisit)

            if (isMoreCritical(criticalPath, stack)) {
                criticalPath.clear()
                criticalPath.addAll(stack)
            }
            visit(toVisit, stack, criticalPath)

            stack.pop()
        }
    }

    val stack = Stack<Int>().apply { push(vertexId) }
    val criticalPath = mutableListOf(vertexId)
    visit(vertexId, stack, criticalPath)

    return criticalPath
}

private fun findCriticalPathBackward(vertexId: Int, sourceData: ReformattedData, isMoreCritical: (List<Int>, Stack<Int>) -> Boolean): MutableList<Int> {
    val linksMatrix = sourceData.linksMatrix
    val backwordMatrix = Array(linksMatrix.size) { _ -> IntArray(linksMatrix.size) { 0 } }

    linksMatrix.forEachIndexed { i, ints ->
        ints.forEachIndexed { j, value ->
            if (value == 1) {
                backwordMatrix[j][i] = value
            }
        }
    }

    fun visit(startId: Int, stack: Stack<Int>, criticalPath: MutableList<Int>) {
        val row = backwordMatrix[startId]
        val couldVisit = row.withIndex().filter { it.value != 0 }.map { it.index }

        couldVisit.forEach { toVisit ->
            stack.push(toVisit)

            if (isMoreCritical(criticalPath, stack)) {
                criticalPath.clear()
                criticalPath.addAll(stack)
            }
            visit(toVisit, stack, criticalPath)

            stack.pop()
        }
    }

    val stack = Stack<Int>().apply { push(vertexId) }
    val criticalPath = mutableListOf(vertexId)
    visit(vertexId, stack, criticalPath)

    println("vertexId -> ${sourceData.matrixIdToRealIdMap[vertexId]}, critical path size -> ${criticalPath.size}, $criticalPath")
    return criticalPath
}

private fun findCriticalPathByNodes(vertexId: Int, sourceData: ReformattedData) =
        findCriticalPath(vertexId, sourceData, { criticalPath, stack -> criticalPath.size < stack.size })

private fun findCriticalPathByDifficulty(vertexId: Int, sourceData: ReformattedData) =
        findCriticalPath(vertexId, sourceData) { criticalPath, stack ->
            val currentMostCritical = criticalPath.map { sourceData.matrixIdToValueMap[it]!! }.sum()
            val currentCritical = stack.map { sourceData.matrixIdToValueMap[it]!! }.sum()
            return@findCriticalPath currentMostCritical < currentCritical
        }

private fun findCriticalPathByNodesBackword(vertexId: Int, sourceData: ReformattedData): MutableList<Int> {
    val result = findCriticalPathBackward(vertexId, sourceData, { criticalPath, stack -> criticalPath.size < stack.size })
    return result
}
