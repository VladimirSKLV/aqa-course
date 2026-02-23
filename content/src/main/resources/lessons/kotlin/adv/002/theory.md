# Kotlin Advanced 2. Дженерики, reified и безопасные преобразования

```kotlin
inline fun <reified T> safeCast(value: Any?): T? {
    return value as? T // as? возвращает null вместо исключения, это безопасно для тестовой утилиты.
}
```

```kotlin
val number: Int? = safeCast<Int>("42") // null: строка не приводится к Int безопасно.
```

## Домашнее задание

Только тестирование:
- квиз по generics, `as?` и `reified`.
