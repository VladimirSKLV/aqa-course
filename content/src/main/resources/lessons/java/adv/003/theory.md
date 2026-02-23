# Урок 003 (Advanced): Коллекции, Stream API и Optional — практично для AQA

Для продвинутого AQA типичная ситуация: из API приходит **список объектов**, вы делаете:
- фильтрацию по условиям;
- группировку;
- извлечение нужных полей;
- проверку уникальности;
- агрегации (sum/avg/min/max);
- читаемые ошибки и отчётность.

В Java это чаще всего делается через:
- **коллекции** (`List`, `Set`, `Map`);
- **Stream API**;
- **Optional** (аккуратная работа с отсутствующими значениями).

---

## 1) Коллекции: что выбирать и почему

### 1.1 List
Список, допускает дубликаты, порядок важен.

- `ArrayList` — быстрый доступ по индексу, стандарт по умолчанию.
- `LinkedList` — редко нужен в AQA (обычно проигрывает по производительности).

Пример: список статусов/элементов/объектов ответа:
```java
List<Integer> codes = List.of(200, 201, 404);
```

### 1.2 Set
Множество, **уникальные** элементы.

- `HashSet` — быстрый, порядок не гарантирует.
- `LinkedHashSet` — сохраняет порядок вставки (часто полезно для стабильного вывода).
- `TreeSet` — хранит отсортировано (дороже).

Пример: проверка уникальности id:
```java
Set<String> ids = new HashSet<>(responseIds);
if (ids.size() != responseIds.size()) {
    throw new AssertionError("Есть дубликаты id");
}
```

### 1.3 Map
Ключ-значение.

- `HashMap` — стандарт.
- `LinkedHashMap` — порядок вставки (удобно для предсказуемых отчётов).
- `TreeMap` — сортировка по ключу.

Пример: сгруппировать ошибки по типу:
```java
Map<String, List<String>> errorsByType = new HashMap<>();
```

---

## 2) Immutable коллекции: стабильность тестов

`List.of(...)`, `Map.of(...)` создают неизменяемые коллекции.
Почему полезно:
- меньше случайных побочных эффектов;
- проще понимать код;
- меньше flaky по причине “кто-то поменял список”.

Если вам нужна изменяемая:
```java
List<String> mutable = new ArrayList<>(List.of("a", "b"));
```

---

## 3) Stream API: когда это реально полезно в AQA

### 3.1 Модель “pipeline”
Stream — это конвейер:
- intermediate operations: `map`, `filter`, `flatMap`, `sorted`, `distinct`
- terminal operations: `collect`, `forEach`, `count`, `anyMatch`, `allMatch`, `findFirst`

### 3.2 Примеры “как в тестах”

#### Проверить, что все статусы 2xx
```java
boolean allOk = codes.stream().allMatch(c -> c >= 200 && c < 300);
```

#### Найти первую ошибку
```java
int firstBad = codes.stream()
        .filter(c -> c < 200 || c >= 300)
        .findFirst()
        .orElse(-1);
```

#### Собрать список id из объектов ответа
```java
List<String> ids = users.stream()
        .map(User::id)
        .toList();
```

#### Сгруппировать по статусу
```java
Map<Status, List<Order>> byStatus = orders.stream()
        .collect(Collectors.groupingBy(Order::status));
```

#### Посчитать сумму
```java
BigDecimal total = orders.stream()
        .filter(o -> o.status() == Status.PAID)
        .map(Order::amount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
```

---

## 4) Optional: без null-check “лесенок”

`Optional` полезен как **результат поиска**:
- нашли запись — вернули
- не нашли — пусто

```java
Optional<Order> failed = orders.stream()
        .filter(o -> o.status() == Status.FAILED)
        .findFirst();
```

Хорошие практики:
- `orElseThrow()` в тестах часто лучше, чем `orElse(null)`
- избегайте `get()` (почти всегда плохая идея)

```java
Order o = failed.orElseThrow(() -> new AssertionError("Нет FAILED заказа"));
```

---

## 5) Частые ошибки и “неочевидные” моменты

### 5.1 Stream и побочные эффекты
`peek()` и изменения объектов внутри `map()` — источник трудноотлаживаемых проблем.
В тестах лучше:
- сначала собрать нужные данные,
- потом отдельно логировать/ассерты.

### 5.2 Порядок важен
`HashSet` / `HashMap` не гарантируют порядок.
Если вы формируете строку для сравнения/отчёта — используйте сортировку или `LinkedHash*`.

### 5.3 Производительность vs читаемость
Stream часто читаемее, но не всегда быстрее.
В AQA важнее:
- ясность,
- стабильность,
- предсказуемые ошибки.

---

## 6) Пример “как это выглядит” в тесте API

Идея проверки ответа:
- получить список заказов
- убедиться, что нет отрицательных сумм
- убедиться, что есть хотя бы один PAID
- найти первый FAILED и вывести его id

Stream-стиль делает это компактно и читаемо.

---

## Домашнее задание (Урок 003 Advanced)

### Часть A — тест (в приложении)
Будут вопросы про:
- List/Set/Map и когда что выбирать
- `groupingBy`, `allMatch/anyMatch/findFirst`
- почему порядок в HashMap/HashSet нельзя использовать в сравнениях
- Optional: `orElseThrow` vs `get`

### Часть B — код (в приложении)
Дан список заказов. Нужно через Stream API:
1) посчитать сумму `PAID_TOTAL`
2) собрать уникальные `TAGS` (отсортировать, соединить запятой)
3) найти `FIRST_FAILED` (id первого заказа со статусом FAILED, иначе NONE)

Цель: научиться писать **короткие, но надёжные** проверки коллекций в автотестах.

