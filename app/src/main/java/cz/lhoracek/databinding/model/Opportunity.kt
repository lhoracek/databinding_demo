package cz.lhoracek.databinding.model

data class Opportunity(
    val title: String,
    val id: String,
    val odds: List<Odd>
)

data class Odd(
    val odd: Float,
    val id: String
)
