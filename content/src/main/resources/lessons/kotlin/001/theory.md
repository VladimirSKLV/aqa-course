# Kotlin Beginner 1. Переменные и типы данных

```kotlin
val userId: Long = 123L // id чаще хранят в Long, чтобы не упереться в диапазон Int.
var retries: Int = 0     // var используем только там, где значение реально меняется.
val isActive = true      // Boolean-флаг для условий в тесте.
```

```kotlin
val expected = "OK"                  // Эталон из тест-кейса.
val actual: String? = response.status // String? подчёркивает, что поле может быть null.
val same = expected == actual         // В Kotlin == сравнивает значения, а не ссылки.
```

## Домашнее задание

Только тестирование:
- квиз по `val/var`, базовым типам и `null`.
