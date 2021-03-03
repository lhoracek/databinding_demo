package cz.lhoracek.databinding.model

interface WithId{
    val id: String
}

data class Opportunity(
    val title: String,
    override val id: String,
    val odds: List<Odd>
): WithId

data class Odd(
    val odd: Float,
    override val id: String
): WithId
