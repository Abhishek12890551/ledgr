package app.ledgr.engine

data class Item(val label: String, val amount: Money, val people: List<PersonId>)
data class Extra(val label: String, val amount: Money)   // signed: tax/tip/delivery > 0, discount < 0

sealed interface SplitSpec {
    data class Equal(val participants: List<PersonId>) : SplitSpec
    data class Exact(val amounts: Map<PersonId, Money>) : SplitSpec
    data class Percent(val basisPoints: Map<PersonId, Int>) : SplitSpec   // must sum to 10_000
    data class Shares(val shares: Map<PersonId, Int>) : SplitSpec
    data class Itemized(val items: List<Item>, val extras: List<Extra>) : SplitSpec
}
