# Kotlin Advanced 1. Data class, точность чисел и время в тестах

## Для кого этот урок

- **С опытом в языке:** систематизируете подход к доменным моделям в тестах.
- **Без глубокого опыта:** поймёте, почему тестовые модели часто ломаются из-за денег и времени, и как Kotlin помогает этого избежать.

---

## `data class`: что даёт и почему важно в AQA

`data class` автоматически генерирует:
- `equals` / `hashCode` (сравнение по полям),
- `toString` (удобный вывод в логи),
- `copy` (клонирование с изменением отдельных полей).

```kotlin
import java.math.BigDecimal

data class Payment(
    val id: String,
    val amount: BigDecimal,
    val currency: String
)
```

### Практическая польза

Вместо ручного сравнения каждого поля в тесте вы сравниваете объекты целиком.

```kotlin
val expected = Payment("p-101", BigDecimal("10.50"), "USD")
val actual = Payment("p-101", BigDecimal("10.50"), "USD")

println(expected == actual) // true
```

---

## Деньги и `BigDecimal`: почему не `Double`

`Double` хранит дроби с погрешностью двоичной арифметики.
Для финансовых проверок это риск false-fail и flaky-тестов.

```kotlin
val a = 0.1 + 0.2
println(a) // может быть 0.30000000000000004
```

С `BigDecimal` поведение предсказуемое.

```kotlin
val amount = BigDecimal("10.10")
val fee = BigDecimal("0.20")
val total = amount + fee
println(total) // 10.30
```

---

## Время: `Instant`, timezone и стабильность тестов

API и логи чаще всего работают в UTC. Для сравнения времени удобно использовать `Instant`.

```kotlin
import java.time.Instant

val createdAt = Instant.parse("2026-01-10T10:15:30Z")
val now = Instant.now()
val valid = !createdAt.isAfter(now)
```

### Что проверять в автотесте

- дата не в будущем,
- `updatedAt >= createdAt`,
- разница между событиями в допустимом диапазоне.

---

## `copy`: безопасная модификация тестовых данных

```kotlin
val base = Payment("p-101", BigDecimal("10.50"), "USD")
val changedAmount = base.copy(amount = BigDecimal("11.00"))
```

Полезно при parameterized-тестах: меняете только нужное поле без риска сломать остальную структуру.

---

## Антипаттерны

1. **Сравнивать деньги через `Double`**.
2. **Сравнивать даты как строки без парсинга**.
3. **Хранить всё в `Map<String, Any>` вместо типизированной модели**.

---

## Мини-сценарий для API-теста

```kotlin
import java.math.BigDecimal
import java.time.Instant

data class PaymentResponse(
    val id: String,
    val amount: BigDecimal,
    val createdAt: Instant
)

fun isValid(response: PaymentResponse): Boolean {
    val amountValid = response.amount >= BigDecimal.ZERO
    val timeValid = !response.createdAt.isAfter(Instant.now())
    return amountValid && timeValid
}
```

## Домашнее задание

Только тестирование:
- квиз по data class, BigDecimal и работе со временем.
