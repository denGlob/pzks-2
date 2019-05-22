package util

import com.mxgraph.model.mxCell
import kotlin.NoSuchElementException

@Suppress("ArrayInDataClass")
data class ReformattedData(
    val linksMatrix: Array<IntArray>,
    val matrixIdToRealIdMap: Map<Int, Int>,
    val matrixIdToValueMap: Map<Int, Int>
) {
    fun toRealId(matrixId: Int) = matrixIdToRealIdMap[matrixId] ?: throw NoSuchElementException()
    fun toMatrixId(realId: Int) = matrixIdToRealIdMap.filterValues { it == realId }.keys.first()
}

fun reformatData(cells: Collection<mxCell>): ReformattedData {
    val vertexes = cells.filter { it.isVertex }
    val matrixIdToRealIdMap = vertexes.mapIndexed { index, mxCell -> index to mxCell.id.toInt() }.toMap()
    val matrixIdToValueMap = matrixIdToRealIdMap.mapValues { entry ->
        (vertexes.first { it.id == entry.value.toString() }.value as String).split('/')[1].toInt()
    }
    val edges = cells.filter { it.isEdge }

    fun Int.toMatrixId() = matrixIdToRealIdMap.filterValues { it == this }.keys.first()

    val linksMatrix = Array(vertexes.size, { matrixId ->
        val row = IntArray(vertexes.size)
        val realId = matrixIdToRealIdMap[matrixId]

        val destinationMatrixIdToEdgeValuePairs = edges.filter { edge -> edge.source.id == realId.toString() }
            .map { edge -> edge.target.id.toInt().toMatrixId() to edge.value as Int }
        destinationMatrixIdToEdgeValuePairs.forEach { row[it.first] = it.second }
        row
    })

    return ReformattedData(linksMatrix, matrixIdToRealIdMap, matrixIdToValueMap)
}