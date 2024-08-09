import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * flowOf(1, 2, 3, 4, 5)
 *     .aggregateWithNulls(3)
 *     .collect { println(it) }
 * prints
 * [null, null, 1]
 * [null, 1, 2]
 * [1, 2, 3]
 * [2, 3, 4]
 * [3, 4, 5]
 */
public fun <T> Flow<T>.aggregateWithNulls(n: Int): Flow<List<T?>> {
    require(n > 0) { "The size n must be greater than 0" }

    // Buffer to hold the last n elements
    val buffer = ArrayDeque<T?>(n)

    // Emit the first element with null padding
    repeat(n) { buffer.addFirst(null) }
    return map { value ->
        buffer.removeFirst()
        buffer.add(value)
        buffer.toList()
    }
}
