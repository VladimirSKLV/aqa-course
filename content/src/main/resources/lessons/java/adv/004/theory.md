# Урок 4. Основы ООП для AQA

В этом уроке мы не делаем обзор трека — только предметная база, которая нужна для написания устойчивых автотестов.

## 1) Классы и объекты

**Класс** — это шаблон (описание структуры и поведения).
**Объект** — конкретный экземпляр класса, созданный в памяти.

Пример:

```java
class User {
    String login;

    User(String login) {
        this.login = login;
    }

    String greet() {
        return "Привет, " + login;
    }
}

public class Main {
    public static void main(String[] args) {
        User user = new User("qa_user");
        System.out.println(user.greet());
    }
}
```

Для автотестов это важно, потому что Page Object, API-клиенты, тестовые данные — это тоже объекты со своей ответственностью.

## 2) Инкапсуляция

**Инкапсуляция** — сокрытие деталей реализации и управление доступом к состоянию объекта.

Пример:

```java
class Account {
    private double balance;

    public Account(double initialBalance) {
        this.balance = initialBalance;
    }

    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма должна быть > 0");
        }
        balance += amount;
    }

    public double getBalance() {
        return balance;
    }
}
```

Почему это полезно в тестах:
- тест не ломает объект прямой записью в поля;
- бизнес-правила проверяются в одном месте (например, в `deposit`);
- легче поддерживать код при изменениях.

## 3) Объект с внутренним состоянием и влияние на тест

**Внутреннее состояние** — данные, которые меняются во времени внутри объекта.

Пример состояния:

```java
class Cart {
    private int itemsCount = 0;

    public void addItem() {
        itemsCount++;
    }

    public int getItemsCount() {
        return itemsCount;
    }
}
```

Если один и тот же объект используется в нескольких тестах, состояние может «протекать» между ними, и тесты станут flaky.

Плохой подход:
- общий `Cart cart = new Cart();` на весь класс тестов;
- тест A добавил 1 товар, тест B ожидает 0.

Хороший подход:
- новый объект на каждый тест;
- явная инициализация в `@BeforeEach`;
- отсутствие скрытых зависимостей между тестами.

Итог: чем лучше вы контролируете состояние объектов, тем стабильнее и предсказуемее автотесты.
