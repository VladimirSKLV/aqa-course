# Kotlin Beginner 2. Условия, `when`, диапазоны и безопасные вызовы

## Зачем этот урок AQA-инженеру

Условная логика — основа проверок:
- статус ответа 2xx/4xx/5xx,
- разные сценарии по роли пользователя,
- fallback-поведение, если поле отсутствует.

В Kotlin для этого есть `if`, диапазоны и мощная конструкция `when` (аналог `switch` из Java, но значительно гибче).

---

## `if` как выражение

В Kotlin `if` может возвращать значение.

```kotlin
val status = 201

val result = if (status in 200..299) {
    "SUCCESS"
} else {
    "FAIL: $status"
}

println(result)
```

Это удобно: меньше временных переменных и дублирования.

---

## Диапазоны: `in 200..299`

Проверка через диапазон — читаемый способ описать бизнес-правило в тесте.

```kotlin
val is2xx = status in 200..299
val isClientError = status in 400..499
```

### Когда использовать

- для кодов ответов,
- для допустимых границ значений,
- для валидации длины или количеств.

---

## `when`: что это, как работает и зачем нужно

`when` — это ветвление по условиям. Если вы знаете Java `switch`, то:
- `when` — более мощный и безопасный вариант,
- не требует `break`,
- может проверять не только равенство, но и диапазоны, типы и произвольные условия.

```kotlin
val statusGroup = when (status) {
    in 200..299 -> "SUCCESS"
    in 400..499 -> "CLIENT_ERROR"
    in 500..599 -> "SERVER_ERROR"
    else -> "UNKNOWN"
}
```

### Когда выбирать `when`, а не `if`

- если веток 3 и больше,
- если нужно описать классификацию (например, группы статусов),
- если важна читаемость для команды.

---

## Null-safety в условиях

Частый кейс: в ответе поле nullable, но в проверке нужна строка.

```kotlin
val email: String? = response.email
val normalized = email?.trim()?.lowercase() ?: ""

val emailState = when {
    normalized.isEmpty() -> "EMPTY"
    "@" !in normalized -> "INVALID"
    else -> "OK"
}
```

Здесь `when` без аргумента работает как цепочка условий.

---

## Антипаттерны и как их избежать

1. **Слишком длинный `if-else if-else`**
   - Перепишите на `when`.
2. **Игнорирование nullable-полей**
   - Добавьте `?.` и `?:`.
3. **Магические числа без пояснения**
   - Выносите диапазоны в именованные функции/константы.

```kotlin
fun isSuccessfulStatus(code: Int): Boolean = code in 200..299
```

---

## Мини-пример из автотеста API

```kotlin
val status = response.code
val bodyMessage: String? = response.message

val statusLabel = when (status) {
    in 200..299 -> "OK"
    in 400..499 -> "BAD_REQUEST_SIDE"
    else -> "OTHER"
}

val message = bodyMessage?.trim() ?: "<empty>"
println("status=$statusLabel, message=$message")
```

Такой формат удобно логировать в отчёт теста.

## Домашнее задание

Только тестирование:
- квиз по `if`, `when`, диапазонам, `?.` и `?:`.
