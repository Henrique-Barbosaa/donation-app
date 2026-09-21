package io.solidar.donation.util

private const val CENTS_IN_UNIT = 100L
private const val GROUP_SIZE = 3

/**
 * Formata um valor em centavos como moeda brasileira (ex.: 125050 -> "R$ 1.250,50").
 *
 * Implementado sem `java.text.NumberFormat` porque este código roda também
 * no alvo iOS, onde as APIs da JVM não estão disponíveis.
 */
fun formatBrl(valueInCents: Long): String {
    val units = valueInCents / CENTS_IN_UNIT
    val cents = valueInCents % CENTS_IN_UNIT
    return "R$ ${groupThousands(units)},${cents.toString().padStart(2, '0')}"
}

private fun groupThousands(value: Long): String {
    val digits = value.toString()
    val result = StringBuilder()
    for (index in digits.indices) {
        if (index > 0 && (digits.length - index) % GROUP_SIZE == 0) {
            result.append('.')
        }
        result.append(digits[index])
    }
    return result.toString()
}