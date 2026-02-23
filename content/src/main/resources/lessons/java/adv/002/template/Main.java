import java.util.Optional;

public class Main {

    /**
     * Задание:
     * Реализуйте safeCast так, чтобы:
     * - если value является экземпляром type, вернуть Optional.of(type.cast(value))
     * - иначе вернуть Optional.empty()
     *
     * Подсказка:
     * - type.isInstance(value)
     * - type.cast(value)
     */
    public static <T> Optional<T> safeCast(Object value, Class<T> type) {
        // TODO: реализуйте safeCast
        return Optional.empty();
    }

    public static void main(String[] args) {
        System.out.println("str=" + safeCast("hello", String.class).orElse(""));

        // Здесь автоупаковка: 42 -> Integer
        System.out.println("int=" + safeCast(42, Integer.class).orElse(-1));

        // Неверный тип: "123" не является Integer
        boolean missing = safeCast("123", Integer.class).isEmpty();
        System.out.println("missing=" + missing);
    }
}
