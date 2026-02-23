# Урок 6. Пишем первые тесты (JUnit 5)

JUnit 5 — базовый фреймворк для unit-тестов в Java.

Минимальный пример:

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Calculator {
    int sum(int a, int b) { return a + b; }
}

class CalculatorTest {
    @Test
    void shouldReturnSum() {
        Calculator calculator = new Calculator();
        assertEquals(5, calculator.sum(2, 3));
    }
}
```

Ключевые принципы:
- один тест — одна проверяемая идея;
- понятные имена тестов;
- тесты должны быть независимыми.
