package io.solidar.donation.model

data class Campaign(
    val id: String,
    val title: String,
    val organizer: String,
    val goalInCents: Long,
    val raisedInCents: Long
) {
    val progress: Float
        get() = if (goalInCents <= 0L) {
            0f
        } else {
            (raisedInCents.toFloat() / goalInCents.toFloat()).coerceIn(0f, 1f)
        }
}