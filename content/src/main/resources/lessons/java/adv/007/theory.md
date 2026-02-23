# Урок 7. Циклы, параметризованные тесты и аннотации

Когда логика одна, а набор входных данных разный, используйте параметризованные тесты.

```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordValidatorTest {
    @ParameterizedTest
    @CsvSource({"qwerty,false", "Qwerty123,true"})
    void shouldValidatePassword(String password, boolean expected) {
        boolean actual = password.length() >= 8 && password.matches(".*\\d.*");
        assertTrue(actual == expected);
    }
}
```

Полезные аннотации жизненного цикла:
- `@BeforeEach` — подготовка данных перед каждым тестом;
- `@AfterEach` — очистка после теста.

Цель: меньше дублирования, больше читаемости.
