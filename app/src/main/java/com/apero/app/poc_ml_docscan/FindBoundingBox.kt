package com.apero.app.poc_ml_docscan

import android.util.Log

fun dfs(matrix: Array<IntArray>, visited: Array<BooleanArray>, i: Int, j: Int) {
    val stack = mutableListOf(Pair(i, j))
    while (stack.isNotEmpty()) {
        val (x, y) = stack.removeAt(stack.size - 1)
        if (visited[x][y]) {
            continue
        }
        visited[x][y] = true
        for ((dx, dy) in listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))) {
            val nx = x + dx
            val ny = y + dy
            if (nx in matrix.indices && ny in matrix[0].indices && matrix[nx][ny] == 1 && !visited[nx][ny]) {
                stack.add(Pair(nx, ny))
            }
        }
    }
}

fun isInvalid(matrix: Array<IntArray>): Boolean {
    val visited = Array(matrix.size) { BooleanArray(matrix[0].size) }
    var connectedComponents = 0

    for (i in matrix.indices) {
        for (j in matrix[i].indices) {
            if (matrix[i][j] == 1 && !visited[i][j]) {
                dfs(matrix, visited, i, j)
                connectedComponents += 1
                if (connectedComponents > 1) {
                    return false
                }
            }
        }
    }
    return true
}
fun printMatrixShape(matrix: Array<IntArray>) {
    val numRows = matrix.size
    val numCols = if (matrix.isNotEmpty()) matrix[0].size else 0
    println("Matrix shape: $numRows x $numCols")
}
fun printMatrix(matrix: Array<IntArray>) {
    println("Matrix values:")
    for (row in matrix) {
        println(row.joinToString(" "))
    }
}
fun printArray(array: FloatArray) {
    println("Input array values:")
    println(array.joinToString(" ") { it.toString() })
}
fun findBoundingBox(maskTensor: FloatArray): MaskPoints? {

    var columns = 224
    var rows = 224
    var minX = columns
    var minY = rows
    var maxX = 0
    var maxY = 0
    var size = 224
    val matrix = Array(size) { IntArray(size) }
    for (i in maskTensor.indices) {
        matrix[i / size][i % size] = if (maskTensor[i] > 0f) 1 else 0
    }
//    printArray(maskTensor)
//    printMatrixShape(matrix)
//    printMatrix(matrix)
    // Check if the matrix is invalid
    val invalid = isInvalid(matrix)
//    if (invalid == false) {
//        Log.d(TAG,"FINALLLLLLLLLLLLLLLLLLLLLLLLLLLLYYYYYYYYYYYYYY: $invalid")
//    }
    Log.d(TAG,"INVALID: $invalid")
    return MaskPoints(
        BoundingBox(minX, minY, maxX, maxY),
        invalid
    )
    }
