package com.frolo.test

import kotlin.random.Random


private val sharedRandom: Random = Random.Default

private val chars by lazy {
    ArrayList<Char>().also { list ->
        var c = 'A'
        while (c <= 'Z') {
            list.add(c)
            ++c
        }
    }
}

fun randomInt(until: Int? = null): Int =
    if (until != null) sharedRandom.nextInt(until)
    else sharedRandom.nextInt()

fun randomLong(until: Long? = null): Long =
    if (until != null) sharedRandom.nextLong(until)
    else sharedRandom.nextLong()

fun randomDouble(until: Double? = null): Double =
    if (until != null) sharedRandom.nextDouble(until)
    else sharedRandom.nextDouble()

fun randomFloat(): Float = sharedRandom.nextFloat()

fun randomChar(): Char = chars[sharedRandom.nextInt(chars.size)]

fun randomBoolean(): Boolean = sharedRandom.nextBoolean()

fun randomString(length: Int = sharedRandom.nextInt(10)): String {
    return (1..length).map { randomChar() }.joinToString(separator = "") { "$it" }
}

fun <E : Enum<*>> randomEnumValue(clazz: Class<E>): E? {
    val constants = clazz.enumConstants
            ?: throw AssertionError("$clazz must represent an enum type")
    val randomIndex = randomInt(constants.size)
    return constants.getOrNull(randomIndex)
}

inline fun <reified E : Enum<E>> randomEnumValue(): E? {
    return randomEnumValue(E::class.java)
}

