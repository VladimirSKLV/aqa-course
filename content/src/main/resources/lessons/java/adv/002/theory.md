# Advanced 2. Generics, Class<T> и safeCast

## Зачем это в инженерной AQA-практике

В реальных фреймворках тестов много универсальных утилит. Без generics они быстро превращаются в `Object` + `ClassCastException`.

## 1) Что дают generics

```java
List<String> names = List.of("api", "web"); // Тип списка известен компилятору: только строки.
String first = names.get(0);                   // Не нужен ручной каст, меньше риска runtime-ошибок.
```

## 2) Рефлексия через `Class<T>`

```java
Object raw = "hello";                             // Данные пришли как Object.
Class<String> type = String.class;                 // Явно описываем ожидаемый тип.

boolean isString = type.isInstance(raw);           // Безопасно проверяем совместимость типов.
String value = type.cast(raw);                     // Выполняем контролируемое приведение.
```

## 3) Шаблон safeCast

```java
static <T> Optional<T> safeCast(Object value, Class<T> type) {
    if (type.isInstance(value)) {                  // Защищаемся от неверного каста заранее.
        return Optional.of(type.cast(value));      // Возвращаем корректно приведённое значение.
    }
    return Optional.empty();                       // Неверный тип — возвращаем пустой результат без исключения.
}
```

## Домашнее задание

Только тестирование:
- квиз по generics и `Class<T>`,
- объяснить, почему `safeCast` надёжнее, чем прямой `(T) value`.
