# Advanced 1. Точность чисел, даты и парсинг данных в автотестах

## Зачем это нужно senior AQA

Flaky-тесты часто связаны не с инфраструктурой, а с некорректной математикой и временем.

## 1) Деньги и `BigDecimal`

```java
BigDecimal expected = new BigDecimal("10.50"); // Создаём из строки, чтобы не занести двоичную погрешность.
BigDecimal actual = new BigDecimal("10.5");    // С точки зрения значения — то же число.

boolean sameValue = expected.compareTo(actual) == 0; // compareTo сравнивает численное значение.
```

## 2) Время через `java.time`

```java
Instant createdAt = Instant.parse("2026-01-10T10:15:30Z"); // Парсим ISO-дату из API в надёжный тип.
Instant now = Instant.now();                                 // Текущее UTC-время для сравнения.

boolean notFromFuture = !createdAt.isAfter(now);            // Проверяем бизнес-правило: запись не из будущего.
```

## 3) Допуск (epsilon) для неточных вычислений

```java
double expectedRatio = 0.3;                     // Ожидаемое значение.
double actualRatio = 0.1 + 0.2;                 // Вычисленное значение с возможной погрешностью.
boolean closeEnough = Math.abs(expectedRatio - actualRatio) < 1e-9; // Сравниваем с обоснованным допуском.
```

## Домашнее задание

Только тестирование:
- квиз по BigDecimal, времени, timezone и допускам,
- разобрать, какие сравнения в ваших тестах могут быть нестабильными и почему.
