import java.math.BigDecimal

enum class Status { NEW, PAID, FAILED }

data class Order(val id: String, val status: Status, val amount: BigDecimal, val tags: List<String>)

fun main() {
    val orders = listOf(
        Order("A-100", Status.PAID, BigDecimal("10.50"), listOf("api", "smoke")),
        Order("A-101", Status.NEW, BigDecimal("2.00"), listOf("api")),
        Order("A-102", Status.PAID, BigDecimal("7.25"), listOf("regression")),
        Order("A-103", Status.FAILED, BigDecimal("1.00"), listOf("smoke"))
    )

    // TODO 1: посчитать сумму amount для заказов со статусом PAID
    val paidTotal: BigDecimal = BigDecimal.ZERO

    // TODO 2: собрать уникальные теги, отсортировать и склеить через запятую
    val tags: String = ""

    // TODO 3: найти id первого FAILED заказа, если нет — "NONE"
    val firstFailed: String = "NONE"

    println("PAID_TOTAL=$paidTotal") // Должно быть 17.75 после реализации.
    println("TAGS=$tags")            // Должно быть api,regression,smoke.
    println("FIRST_FAILED=$firstFailed")
}
