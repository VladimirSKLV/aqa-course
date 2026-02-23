# Kotlin Advanced 3. Коллекции, Sequence и extension-функции для тестовой аналитики

## Зачем это в AQA

После прогона тестов и API-вызовов у нас много данных:
- список заказов,
- список ошибок,
- список тегов/меток,
- метрики статусов.

Нужно быстро фильтровать, агрегировать и формировать отчётную информацию.

---

## Коллекции: базовый pipeline

```kotlin
val paidTotal = orders
    .filter { it.status == Status.PAID }
    .map { it.amount }
    .fold(BigDecimal.ZERO) { acc, v -> acc + v }
```

Здесь три шага:
1. `filter` — оставили только нужные записи,
2. `map` — взяли только нужное поле,
3. `fold` — агрегировали в итог.

---

## `Sequence`: что это и когда применять

`Sequence` даёт ленивую обработку: элементы считаются по мере необходимости.

```kotlin
val paidTotal = orders
    .asSequence()
    .filter { it.status == Status.PAID }
    .map { it.amount }
    .fold(BigDecimal.ZERO) { acc, v -> acc + v }
```

### Когда это полезно

- большие коллекции,
- длинные chain-операции,
- промежуточные шаги не должны создавать лишние списки.

### Когда можно оставить обычные коллекции

- данных мало,
- важнее простота кода, чем микрооптимизация.

---

## Extension-функции: выразительный DSL в тестах

Extension-функции позволяют добавлять методы к существующим типам без наследования.

```kotlin
fun List<Order>.firstFailedId(): String =
    firstOrNull { it.status == Status.FAILED }?.id ?: "NONE"
```

Теперь правило звучит по-доменному:

```kotlin
val firstFailed = orders.firstFailedId()
```

Это улучшает читаемость теста почти как «живой язык» команды.

---

## Агрегация тегов для отчётности

```kotlin
val tags = orders
    .flatMap { it.tags }
    .distinct()
    .sorted()
    .joinToString(",")
```

Часто требуется для выгрузки метрик и пост-процессинга результатов прогонов.

---

## Антипаттерны

1. **Смешивать бизнес-логику и печать в одном огромном chain**.
2. **Использовать `Sequence` «всегда», даже на 5 элементах**.
3. **Extension-функции с неочевидными именами (`doMagic`)**.

---

## Комплексный пример

```kotlin
import java.math.BigDecimal

enum class Status { PAID, FAILED, NEW }

data class Order(
    val id: String,
    val amount: BigDecimal,
    val status: Status,
    val tags: List<String>
)

fun List<Order>.firstFailedId(): String =
    firstOrNull { it.status == Status.FAILED }?.id ?: "NONE"

fun List<Order>.paidTotal(): BigDecimal =
    asSequence()
        .filter { it.status == Status.PAID }
        .map { it.amount }
        .fold(BigDecimal.ZERO) { acc, v -> acc + v }

fun List<Order>.allTagsCsv(): String =
    flatMap { it.tags }
        .distinct()
        .sorted()
        .joinToString(",")
```

В итоге код проверок остаётся коротким, а общая логика — переиспользуемой.

## Домашнее задание

- тестирование: квиз по коллекциям и extension-функциям,
- написание кода: посчитать сумму, собрать теги, найти первый FAILED.
