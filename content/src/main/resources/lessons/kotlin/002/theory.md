# Kotlin Beginner 2. Условия и null-safety

```kotlin
val status = 201 // Статус из API.

if (status in 200..299) {                // in диапазоне делает условие короче и читабельнее.
    println("SUCCESS")                  // Успешная ветка.
} else {
    println("FAIL: $status")            // Диагностика при провале.
}
```

```kotlin
val email: String? = response.email       // nullable-поле из ответа.
val normalized = email?.trim() ?: ""      // Безопасный вызов + значение по умолчанию.
```

## Домашнее задание

Только тестирование:
- квиз по `if`, диапазонам, `?.` и `?:`.
