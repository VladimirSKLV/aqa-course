# Условия, boolean и сравнения (if/else)

Условная логика — один из базовых инструментов AQA. Она встречается почти в каждом проекте:

- ветвление по **HTTP статусу** (200/400/500);
- разные проверки для **разных ролей** и типов пользователей;
- обработка **опциональных полей** в JSON (поле может отсутствовать или быть `null`);
- UI сценарии: **элемент видим / не видим**, **кнопка активна / не активна**;
- безопасные проверки, чтобы не ловить `NullPointerException` из‑за `null`;
- “мягкие” проверки: собрать список ошибок и завершить тест с понятным отчётом.

В этом уроке: тип `boolean`, сравнения, логические операторы, `if/else`, короткое замыкание и типичные ошибки.

---

## 1) Тип boolean

`boolean` — примитивный тип, который может быть только `true` или `false`.

Примеры:

```java
boolean ok = true;
boolean hasToken = token != null;
boolean visible = element.isDisplayed();
```

В автотестах `boolean` чаще всего появляется как результат:

- сравнений: `status == 200`, `age >= 18`
- условий: `if (response != null && response.isValid()) { ... }`
- логических выражений: `a > b && c < d`

### 1.1) assertTrue/assertFalse и читаемость

В тестах стремитесь к читаемости: выражение должно “говорить само за себя”.

```java
boolean isOkStatus = status >= 200 && status < 300;
assertTrue(isOkStatus, "Ожидали 2xx статус");
```

---

## 2) Операторы сравнения

Для чисел и примитивов:

- `==` — равно
- `!=` — не равно
- `>` — больше
- `<` — меньше
- `>=` — больше или равно
- `<=` — меньше или равно

Пример (типичный для API):

```java
int status = 200;

boolean isOk = status == 200;
boolean isClientError = status >= 400 && status < 500;
boolean isServerError = status >= 500;
```

### 2.1) Важное отличие: сравнение объектов и строк

Для **объектов** оператор `==` сравнивает **ссылки** (то есть “это один и тот же объект в памяти”), а не содержимое.

Для сравнения содержимого используйте:

- `equals(...)` для строк/объектов
- `Objects.equals(a, b)` если возможен `null`

Пример (правильно):

```java
String role = response.getRole();

// безопасно: константа слева
if ("ADMIN".equals(role)) {
    // ...
}

// или безопасно при null:
if (java.util.Objects.equals(role, "ADMIN")) {
    // ...
}
```

Пример (ошибка):

```java
// НЕПРАВИЛЬНО: сравнение ссылок
if (role == "ADMIN") {
    // иногда "работает", иногда нет
}
```

Почему это важно в AQA: строки из JSON/БД/HTTP ответов — почти всегда новые объекты, и `==` даст неожиданные результаты.

---

## 3) Логические операторы

- `&&` (AND) — истина, если обе части истинны
- `||` (OR) — истина, если хотя бы одна часть истинна
- `!` (NOT) — инверсия (true → false, false → true)

Примеры:

```java
boolean ok = status == 200 && responseTimeMs < 1500;
boolean needAuth = status == 401 || status == 403;
boolean notEmpty = !list.isEmpty();
```

### 3.1) Короткое замыкание (short-circuit)

Java вычисляет логические выражения “лениво”:

- в `A && B` если `A == false`, то `B` **не вычисляется**
- в `A || B` если `A == true`, то `B` **не вычисляется**

Это часто спасает от `NullPointerException`:

```java
// user может быть null
if (user != null && user.getName() != null) {
    System.out.println(user.getName());
}
```

Если перепутать порядок, будет NPE:

```java
// ОШИБКА: user.getName() вычислится первым
if (user.getName() != null && user != null) {
    // ...
}
```

### 3.2) Приоритет операторов и скобки

`!` имеет более высокий приоритет, чем `&&`, а `&&` — выше, чем `||`.
Чтобы избежать ошибок и повысить читаемость — используйте скобки и “разбиение” условий на переменные.

```java
boolean isOkStatus = status >= 200 && status < 300;
boolean fastEnough = responseTimeMs < 1500;

assertTrue(isOkStatus && fastEnough);
```

---

## 4) if / else

Базовая форма:

```java
if (condition) {
    // выполняется, когда condition == true
} else {
    // выполняется, когда condition == false
}
```

Цепочка условий:

```java
if (status >= 200 && status < 300) {
    System.out.println("success");
} else if (status >= 400 && status < 500) {
    System.out.println("client error");
} else {
    System.out.println("server/other");
}
```

### 4.1) Реальные примеры из AQA

#### 4.1.1) API: разные проверки для success и error

```java
int status = response.statusCode();

if (status == 200) {
    // проверяем тело ответа
    assertNotNull(response.jsonPath().getString("id"));
} else {
    // проверяем сообщение об ошибке
    assertNotNull(response.jsonPath().getString("error"));
}
```

#### 4.1.2) UI: элемент может отсутствовать

```java
if (loginError.isDisplayed()) {
    assertEquals("Wrong password", loginError.getText());
} else {
    // если ошибки нет — ожидаем, что пользователь вошёл
    assertTrue(profileIcon.isDisplayed());
}
```

---

## 5) Тернарный оператор (кратко)

Тернарный оператор — это компактная форма `if/else` для простых выражений:

```java
String statusText = status == 200 ? "OK" : "ERROR";
```

Используйте, если выражение короткое и не ухудшает читаемость. Если логика сложная — лучше обычный `if/else`.

---

## 6) Типичные ошибки новичков

### 6.1) Сравнение строк через `==`
Используйте `equals`, иначе получите “плавающее” поведение.

### 6.2) Слишком сложные условия в одну строку
Разбивайте на переменные и делайте читаемо.

### 6.3) Отсутствие проверки на null
Используйте short-circuit (`&&`) и `Objects.equals`.

---

## Домашнее задание (код)

В шаблоне кода есть метод `isAdult(int age)`. Реализуйте его через `if/else`:

- вернуть `true`, если `age >= 18`
- иначе вернуть `false`

После нажатия **Проверить** программа должна вывести:

```
ADULT? false
ADULT? true
```
