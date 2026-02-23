# Урок 13. Подготовка к API- и Web-автоматизации

Перед ростом набора тестов настройте проект:

1. **Зависимости**
   - API: JUnit + Rest Assured/HttpClient.
   - Web: JUnit + Selenium.
2. **Конфиги**
   - `baseUrl`, таймауты, браузер (`-Dbrowser=chrome`).
3. **Запуск из CI и локально**
   - `./gradlew test -DbaseUrl=...`
4. **Структура**
   - отдельные папки/пакеты для `api` и `web` тестов.

## Минимальный Web smoke (Selenium)

```java
WebDriver driver = new ChromeDriver();
driver.get(baseUrl);
assertTrue(driver.getTitle().length() > 0);
driver.quit();
```

В нашей песочнице кнопка **Web автотесты** теперь запускает smoke-проверки и пытается открыть URL в браузере.
