# Урок 7. Циклы, параметризованные тесты и аннотации

Параметризованные тесты нужны, когда проверка одна, а входы разные.

```java
@ParameterizedTest
@CsvSource({"200,true", "404,false"})
void statusCheck(int status, boolean expected) {
    boolean actual = status >= 200 && status < 300;
    assertEquals(expected, actual);
}
```

Полезные аннотации:
- `@BeforeEach` — подготовка перед каждым тестом;
- `@AfterEach` — очистка после теста;
- `@ParameterizedTest` — запуск теста с набором данных.

Это снижает копипаст и упрощает поддержку тестов.
