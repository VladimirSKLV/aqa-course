# Урок 2. Условия, boolean и сравнения (if/else)

## Зачем это в автотестах

Условия — это сердце любой проверки: "если пришёл 2xx — ок, иначе фейл", "если поле пустое — выводим понятную ошибку".

## 1) `if/else` как основной инструмент ветвления

```java
int status = 201; // Пример статуса из ответа API.

if (status >= 200 && status < 300) { // Проверяем, что код в успешном диапазоне 2xx.
    System.out.println("SUCCESS");   // Явно показываем, что проверка успешна.
} else {
    System.out.println("FAIL");      // Отдельная ветка для неуспеха и диагностики.
}
```

## 2) Логические операторы

```java
boolean hasToken = true;      // Токен получен.
boolean endpointEnabled = true; // Эндпоинт включён.

boolean canCallApi = hasToken && endpointEnabled; // Оба условия должны быть true.
boolean hasAccessIssue = !hasToken || !endpointEnabled; // Хотя бы одно условие не выполнено.
```

## 3) Сравнения строк и чисел

```java
String expectedRole = "ADMIN";             // Ожидаемая роль из тест-кейса.
String actualRole = response.get("role");  // Фактическая роль из API.

if (expectedRole.equals(actualRole)) {      // Сравниваем содержимое строк.
    System.out.println("ROLE OK");
}
```

## 4) Guard clauses для читаемых тестов

```java
String email = response.get("email"); // Поле может отсутствовать.

if (email == null || email.isBlank()) { // Быстро выходим с понятной причиной провала.
    throw new AssertionError("Email не заполнен");
}

System.out.println("Email валиден: " + email); // До этой строки доходим только при корректном значении.
```

## Домашнее задание

Только тестирование:
- пройти квиз по `if/else`, `&&`, `||`, `!`,
- уметь объяснить, почему строки проверяются через `equals`,
- закрепить шаблон проверок "успех/ошибка" для API.
