# Kotlin Advanced 2. Дженерики, `reified` и безопасные преобразования

## Зачем это AQA

В тестовых фреймворках и helper-слое часто нужны универсальные утилиты:
- извлечь поле из разнородного payload,
- безопасно привести тип,
- переиспользовать одну функцию для разных моделей.

Дженерики и `reified` позволяют делать это без небезопасных кастов.

---

## Базовая идея дженериков

Дженерик — это «тип-параметр» функции или класса.

```kotlin
fun <T> firstOrNull(items: List<T>): T? =
    if (items.isEmpty()) null else items[0]
```

Та же функция работает для `List<Int>`, `List<String>`, `List<Order>`.

---

## Безопасное приведение `as?`

- `as` — бросает исключение при несовместимом типе.
- `as?` — возвращает `null`.

Для автотестов обычно лучше `as?`: тест падает контролируемо, а не аварийно в неожиданной точке.

```kotlin
val raw: Any = "42"
val number: Int? = raw as? Int
println(number) // null
```

---

## `inline` + `reified`: что это и когда нужно

Обычно тип `T` в runtime «стирается» (type erasure). `reified` позволяет получить доступ к типу внутри функции.

```kotlin
inline fun <reified T> safeCast(value: Any?): T? {
    return value as? T
}
```

```kotlin
val a: Int? = safeCast<Int>("42")
val b: String? = safeCast<String>("42")
println(a) // null
println(b) // 42
```

---

## Практика: извлечение полей из `Map<String, Any?>`

Частый кейс в API/UI-тестах — полуструктурированные данные.

```kotlin
inline fun <reified T> Map<String, Any?>.read(key: String): T? {
    return this[key] as? T
}

val payload: Map<String, Any?> = mapOf(
    "id" to "A-100",
    "attempt" to 3,
    "active" to true
)

val id: String? = payload.read("id")
val attempt: Int? = payload.read("attempt")
val missing: Long? = payload.read("attempt") // null, тип не совпал
```

---

## Ошибки, которые часто встречаются

1. **Слепое использование `as`**
   - Риск `ClassCastException`.
2. **Избыточные unchecked-cast без объяснения**
   - Если избежать нельзя, документируйте причину.
3. **Слишком сложные generic-сигнатуры в начале пути**
   - Начните с простого API, усложняйте только по необходимости.

---

## Для новичков и для опытных

### Новичкам
- Используйте `as?` + обработку `null`.
- Пишите короткие универсальные функции по одному сценарию.

### Опытным
- Выносите generic-утилиты в отдельный test-utils модуль.
- Добавляйте понятные имена (`readTyped`, `safeCastOrNull`).
- Комбинируйте с extension-функциями для доменной выразительности.

## Домашнее задание

Только тестирование:
- квиз по generics, `as?` и `reified`.
