# Generics, Class<T> и безопасные приведения типов (safeCast)

Этот урок — про generics в Java и практический приём, который часто нужен в AQA: **безопасно привести тип** во время выполнения и не упасть с `ClassCastException`.

В реальной работе такое встречается, когда:

- вы получаете `Object` из обобщённого контейнера (например, `Map<String, Object>`, `JsonPath.get(...)`, кастомный “context” теста);
- часть данных может иметь разный тип (строка/число/булево) в зависимости от окружения или версии API;
- вы пишете универсальные утилиты для тестов: преобразование значений, нормализация входных данных, извлечение из response/context.

---

## 1) Что такое generics в Java

**Generics** позволяют задавать параметр типа:

- `List<String>` — список строк
- `Map<String, Integer>` — мапа ключ→значение
- `Optional<User>` — либо `User`, либо “пусто”

Плюсы для AQA:

1) **Безопасность типов**: меньше случайных `ClassCastException`.
2) **Читаемость**: в сигнатурах видно, что именно передаётся/возвращается.
3) **Универсальность утилит**: один метод может работать с разными DTO/типами.

Пример:

```java
List<String> names = List.of("a", "b");
// names.add(123); // ошибка компиляции
```

---

## 2) Type erasure: важное ограничение

В Java generics работают на этапе компиляции. В рантайме происходит **стирание типов (type erasure)**.

То есть `List<String>` и `List<Integer>` в рантайме — оба просто `List`.

Это приводит к практическим последствиям:

- нельзя сделать `new T()`
- нельзя проверить `value instanceof T`
- нельзя получить `Class<T>` просто из `T`

Из-за этого часто используется приём “**type token**” — передача `Class<T>` в метод.

---

## 3) Class<T> как type token

Если у вас есть `Class<T> type`, вы можете:

- проверить тип: `type.isInstance(value)`
- безопасно привести: `type.cast(value)`

Это намного лучше, чем “слепой” каст:

```java
Integer x = (Integer) value; // может упасть ClassCastException
```

---

## 4) Safe cast: идея и реализация

Нам нужен метод:

```java
static <T> Optional<T> safeCast(Object value, Class<T> type)
```

Поведение:

- если `value` является экземпляром `type`, вернуть `Optional.of(casted)`
- иначе вернуть `Optional.empty()`

Реализация:

```java
if (type.isInstance(value)) {
    return Optional.of(type.cast(value));
}
return Optional.empty();
```

---

## 5) Где это используется в AQA

### 5.1) Map/Context, shared state между шагами

```java
Map<String, Object> ctx = new HashMap<>();
ctx.put("userId", 42);

Optional<Integer> userId = safeCast(ctx.get("userId"), Integer.class);
assertTrue(userId.isPresent());
```

### 5.2) Работа с JsonPath/RestAssured (когда типы “плавают”)

Иногда API возвращает число как `Integer`, иногда как `Long`, иногда как `String` (это плохо, но бывает).

```java
Object raw = jsonPath.get("id");
Optional<Long> asLong = safeCast(raw, Long.class);
Optional<Integer> asInt = safeCast(raw, Integer.class);
Optional<String> asString = safeCast(raw, String.class);
```

Дальше вы нормализуете:

- если `Long` → ок
- если `Integer` → конвертируете в `Long`
- если `String` → парсите

---

## 6) Ошибки и хорошие практики

1) **Не используйте `instanceof` + явный каст**, когда можно использовать `Class<T>`:
   - `type.isInstance(...)` + `type.cast(...)` обычно читается и безопаснее.

2) **Используйте `Optional`** для “может быть / может не быть”:
   - это явно сигнализирует, что значение не гарантировано.

3) **Избегайте `Optional.get()` без проверки**:
   - лучше `orElse`, `orElseThrow`, `ifPresent`, `map`.

---

## Домашнее задание (код)

Реализуйте метод:

```java
static <T> Optional<T> safeCast(Object value, Class<T> type)
```

Проверка выполняется запуском `main`. Ожидаемый вывод:

```
str=hello
int=42
missing=true
```
