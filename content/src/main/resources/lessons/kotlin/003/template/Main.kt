fun is2xx(code: Int): Boolean {
    // TODO: верните true, если code в диапазоне 200..299.
    // Это вынесено в функцию, чтобы переиспользовать правило в нескольких проверках.
    return false
}

fun main() {
    val codes = listOf(200, 201, 404) // Эмулируем ответы API.
    var ok = 0                         // Считаем успешные ответы.
    var fail = 0                       // Считаем неуспешные ответы.

    for (code in codes) {
        if (is2xx(code)) {
            println("OK: $code")      // Логируем успешный код.
            ok++
        } else {
            println("FAIL: $code")    // Логируем проблемный код.
            fail++
        }
    }

    println("OK_COUNT=$ok")           // Итог по успешным проверкам.
    println("FAIL_COUNT=$fail")       // Итог по неуспешным проверкам.
}
