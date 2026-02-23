# Kotlin Advanced 1. Data class, BigDecimal и время

```kotlin
data class Payment(val id: String, val amount: BigDecimal) // data class даёт equals/hashCode/toString для удобных ассертов.
```

```kotlin
val createdAt = Instant.parse("2026-01-10T10:15:30Z") // Парсим API-дату в тип Instant.
val valid = !createdAt.isAfter(Instant.now())          // Проверяем, что дата не из будущего.
```

## Домашнее задание

Только тестирование:
- квиз по data class, BigDecimal и работе со временем.
