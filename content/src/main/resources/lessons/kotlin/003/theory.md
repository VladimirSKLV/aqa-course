# Kotlin Beginner 3. Функции, циклы и структурирование проверок

## Цель урока

Научиться выносить правила в функции и обходить наборы данных без дублирования кода.
Это базовый навык для любого AQA: чем меньше копипаста в тестах, тем ниже стоимость поддержки.

---

## Функции: зачем они в автотестах

Функция позволяет:
- назвать бизнес-правило,
- использовать его в нескольких тестах,
- централизованно менять логику.

```kotlin
fun is2xx(code: Int): Boolean {
    return code in 200..299
}
```

### Улучшенная версия с понятным именем

```kotlin
fun isSuccessfulHttpCode(code: Int): Boolean = code in 200..299
```

Такой код легче читать в отчётах и ревью.

---

## Параметры и возвращаемое значение

- Параметры — входные данные правила.
- Возвращаемый тип — результат проверки.

```kotlin
fun formatStatusLine(code: Int): String {
    return if (isSuccessfulHttpCode(code)) "OK: $code" else "FAIL: $code"
}
```

---

## Циклы: когда использовать `for`

В AQA часто нужно проверить список данных:
- набор HTTP-ответов,
- несколько пользователей,
- набор конфигураций.

```kotlin
val codes = listOf(200, 201, 404)
for (code in codes) {
    println(formatStatusLine(code))
}
```

Плюс Kotlin: цикл по коллекции читается проще, чем индексный подход с ручным `i`.

---

## Подсчёт результатов (OK/FAIL)

```kotlin
var okCount = 0
var failCount = 0

for (code in codes) {
    if (isSuccessfulHttpCode(code)) {
        okCount++
    } else {
        failCount++
    }
}

println("OK_COUNT=$okCount")
println("FAIL_COUNT=$failCount")
```

Это типичный шаблон для агрегированной статистики в smoke/regression прогонах.

---

## Когда начинающим, а когда опытным писать «сложнее»

### Если вы новичок

- Начинайте с простых функций (`Boolean`-результат).
- Избегайте раннего усложнения (обобщений, chain-операций на 6+ шагов).

### Если у вас есть опыт

- Выносите форматирование/классификацию в отдельные функции.
- Готовьте код к переиспользованию в helper-слое тестов.
- Добавляйте осмысленные имена (`isSuccessfulHttpCode`, `formatStatusLine`).

---

## Частые ошибки

1. **Дублирование одного и того же `if` в каждом тесте**
   - Лечится функцией-правилом.
2. **Непонятные имена функций (`check1`, `doWork`)**
   - Используйте доменные имена из предметной области.
3. **Смешивание логики проверки и логики вывода**
   - Разделите: отдельная функция проверки, отдельная функция форматирования.

---

## Мини-сценарий «как в реальном проекте»

```kotlin
fun isSuccessfulHttpCode(code: Int): Boolean = code in 200..299

fun evaluate(code: Int): String =
    if (isSuccessfulHttpCode(code)) "OK: $code" else "FAIL: $code"

fun main() {
    val codes = listOf(200, 201, 404)
    var okCount = 0
    var failCount = 0

    for (code in codes) {
        val line = evaluate(code)
        println(line)
        if (line.startsWith("OK")) okCount++ else failCount++
    }

    println("OK_COUNT=$okCount")
    println("FAIL_COUNT=$failCount")
}
```

## Домашнее задание

- тестирование: квиз по циклам и функциям,
- написание кода: реализовать проверку статусов и подсчёт OK/FAIL.
