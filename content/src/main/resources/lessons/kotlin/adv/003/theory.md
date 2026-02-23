# Kotlin Advanced 3. Коллекции, sequence и extension-функции

```kotlin
val paidTotal = orders
    .asSequence()                                   // Ленивый pipeline, полезно на больших наборах данных.
    .filter { it.status == Status.PAID }            // Берём только оплаченные.
    .map { it.amount }                               // Извлекаем сумму.
    .fold(BigDecimal.ZERO) { acc, v -> acc + v }     // Суммируем значения без мутаций.
```

```kotlin
fun List<Order>.firstFailedId(): String =
    firstOrNull { it.status == Status.FAILED }?.id ?: "NONE" // Extension-функция делает код теста короче.
```

## Домашнее задание

- тестирование: квиз по коллекциям и extension-функциям,
- написание кода: посчитать сумму, собрать теги, найти первый FAILED.
