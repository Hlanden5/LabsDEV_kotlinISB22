package com.IS_B22_Dukhov.lab1_variant_10

import kotlin.random.Random

data class ClampResult(
    val original: List<Int>,
    val z: Int,
    val replaced: List<Int>,
    val replacements: Int
)

object ListProcessor {

    fun generateList(size: Int, min: Int = 0, max: Int = 50, seed: Long? = null): List<Int> {
        val rnd = seed?.let { Random(it) } ?: Random
        return List(size) { rnd.nextInt(min, max + 1) }
    }
    fun clampAbove(source: List<Int>, z: Int): ClampResult {
        val (acc, cnt) = source.fold(mutableListOf<Int>() to 0) { (acc, c), v ->
            if (v > z) {
                acc.add(z)
                acc to (c + 1)
            } else {
                acc.add(v)
                acc to c
            }
        }
        return ClampResult(
            original = source,
            z = z,
            replaced = acc.toList(),
            replacements = cnt
        )
    }
}
