# Урок 6. Пишем первые тесты (JUnit 5)

Минимальный тест на JUnit 5:

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MathTest {
    @Test
    void sum_shouldReturn5() {
        assertEquals(5, 2 + 3);
    }
}
```

## Как добавить зависимости

### Gradle
```kotlin
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

tasks.test {
    useJUnitPlatform()
}
```

### Maven
```xml
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter</artifactId>
  <version>5.10.2</version>
  <scope>test</scope>
</dependency>
```

Тесты запускаются командами:
- `./gradlew test`
- `mvn test`
