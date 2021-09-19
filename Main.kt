package phonebook

import java.io.File
import kotlin.math.*

data class SortResponse(val isFinished: Boolean, val time: Long)

data class SearchResponse(val isSorted: Boolean, val foundCount: Int, val time: Long, val prepareTime: Long)

data class SearchShortResponse(val foundCount: Int, val time: Long)

fun millisToTime(millis: Long): LongArray {
    val min = millis / 60000
    return longArrayOf(min, (millis - min * 60000) / 1000, (millis - min * 60000) % 1000)
}

fun linearSearch(dirLines: MutableList<String>, findLines: List<String>): SearchResponse {
    val start = System.currentTimeMillis()
    var foundCount = 0
    for (find in findLines) {
        for (line in dirLines) {
            if (line.contains(find)) {
                foundCount++
                break
            }
        }
    }
    val end = System.currentTimeMillis()
    val time = end - start
    return SearchResponse(false, foundCount, time, 0)
}

fun String.toName(): String {
    val a = this.split(" ")
    return if(a.size > 2) "${a[1]} ${a[2]}" else "${a[1]}"
}

fun String.toNumber(): String {
    val a = this.split(" ")
    return "${a[0]}"
}

fun bubbleSort(lines: MutableList<String>, time: Long): SortResponse {
    val n = lines.size
    val startTime = System.currentTimeMillis()
    var curTime = startTime
    for (i in 2..n) {
        for (j in 0..n - i) {
            if (lines[j].toName()  > lines[j + 1].toName()){
                lines[j] = lines[j + 1].also { lines[j + 1] = lines[j] }
            }
        }
        curTime = System.currentTimeMillis() - startTime
        if (curTime > 10 * time) return SortResponse(false, curTime)
    }
    return SortResponse(true, curTime)
}

fun quickSort(lines: MutableList<String>, time: Long, start: Int = 0, end: Int = lines.size): SortResponse {
    if (end - start <= 1) {
        return SortResponse(true, 0);
    }
    val startTime = System.currentTimeMillis()
    val pivotName = lines[start].toName()
    var left = start + 1
    var right = end - 1

    while (left <= right) {
        if (lines[left].toName() <= pivotName) {
            left++
        } else if (lines[right].toName() >= pivotName) {
            right--
        } else {
            lines[left] = lines[right].also { lines[right] = lines[left] }
            left++
            right--
        }
    }

    lines[right] = lines[start].also { lines[start] = lines[right] }

    val leftAns = quickSort(lines, time, start, right)
    if (!leftAns.isFinished) {
        return leftAns
    }
    val rightAns = quickSort(lines, time, right + 1, end)
    val endTime = System.currentTimeMillis()
    if (endTime - startTime > 10 * time) {
        return SortResponse(false, endTime - startTime)
    }
    return SortResponse(true, endTime - startTime)
}

fun jumpSearch(dirLines: MutableList<String>, findLines: List<String>): SearchShortResponse {
    val n = dirLines.size
    val windowSize = sqrt(n.toDouble()).roundToInt()
    val startTime = System.currentTimeMillis()
    var foundCount = 0
    for (find in findLines) {
        var id = 0
        var prev = 0
        var found = false
        while (true) {
            if (dirLines[id].toName() >= find) {
                for (i in id downTo prev) {
                    if(dirLines[i].contains(find)) {
                        found = true
                        break
                    }
                }
            }
            if (found) {
                foundCount++
                break
            }
            prev = id
            if (id == n - 1) {
                break
            }
            else {
                id = min(n - 1, id + windowSize)
            }
        }
    }
    val endTime = System.currentTimeMillis()
    return SearchShortResponse(foundCount, endTime - startTime)
}

fun binarySearch(dirLines: MutableList<String>, findLines: List<String>): SearchShortResponse {
    val startTime = System.currentTimeMillis()
    var foundCount = 0
    for (find in findLines) {
        var left = 0
        var right = dirLines.size - 1
        while (left < right) {
            val middle = (left + right) / 2
            val middleName = dirLines[middle].toName()
            if (middleName > find) {
                right = middle - 1
            } else if (middleName < find) {
                left = middle + 1
            }
            else {
                left = middle
                right = middle
            }
        }
        if (dirLines[left].toName() == find) {
            foundCount++
        }
    }
    val endTime = System.currentTimeMillis()
    return SearchShortResponse(foundCount, endTime - startTime)
}

fun search(dirLines: MutableList<String>, findLines: List<String>, linearTime: Long,
           sort: (MutableList<String>, Long) -> SortResponse,
           search: (MutableList<String>, List<String>) -> SearchShortResponse): SearchResponse {
    val (isSorted, sortTime) = sort(dirLines, linearTime)
    if (!isSorted) {
        val answer = linearSearch(dirLines, findLines)
        return SearchResponse(answer.isSorted, answer.foundCount, answer.time, sortTime)
    }
    val searchAns = search(dirLines, findLines)
    return SearchResponse(true, searchAns.foundCount, searchAns.time, sortTime)
}

fun printSearchResult(dirLines: MutableList<String>, findLines: List<String>,
                      result: SearchResponse, preprocName: String = "Sorting") {
    val (isSorted, foundCount, searchTime, sortTime) = result
    val (min, sec, ms) = millisToTime(searchTime + sortTime)
    println("Found ${foundCount} / ${findLines.size} entries. Time taken: $min min. $sec sec. $ms ms.")
    val (preprocMin, preprocSec, preprocMs) = millisToTime(sortTime)
    print("${preprocName} time: ${preprocMin} min. ${preprocSec} sec. ${preprocMs} ms.")
    val (searchMin, searchSec, searchMs) = millisToTime(searchTime)
    if (isSorted) {
        println()
    } else {
        println(" - STOPPED, moved to linear search")
    }
    println("Searching time: ${searchMin} min. ${searchSec} sec. ${searchMs} ms.")
}

fun hashTableSearch(dirLines: MutableList<String>, findLines: List<String>): SearchResponse {
    var table = HashMap<String, String>()
    var prepareTime = System.currentTimeMillis()
    for (line in dirLines) {
        table[line.toName()] = line.toNumber()
    }
    prepareTime = System.currentTimeMillis() - prepareTime
    var foundCount = 0
    var searchTime = System.currentTimeMillis()
    for (find in findLines) {
        if (find in table) {
            foundCount++
        }
    }
    searchTime = System.currentTimeMillis() - searchTime
    return SearchResponse(true, foundCount, searchTime, prepareTime)
}

fun main() {
    val dirPath = "C:/Users/manke/Downloads/"
    val dirLines = File(dirPath + "directory.txt").readLines().toMutableList()
    val findLines = File(dirPath + "find.txt").readLines()
    println("Start searching (linear search)...")
    val (isLinearSorted, linearFoundCount, linearTime) = linearSearch(dirLines, findLines)
    val (lMin, lSec, lMs) = millisToTime(linearTime)
    println("Found ${linearFoundCount} / ${findLines.size} entries. Time taken: $lMin min. $lSec sec. $lMs ms.")
    println("Start searching (bubble sort + jump search)...")
    printSearchResult(dirLines, findLines, search(dirLines, findLines, linearTime, ::bubbleSort, ::jumpSearch))
    println("Start searching (quick sort + binary search)...")
    printSearchResult(dirLines, findLines, search(dirLines, findLines, linearTime, ::quickSort, ::binarySearch))
    println("Start searching (hash table)...")
    printSearchResult(dirLines, findLines, hashTableSearch(dirLines, findLines), "Creating")
}
