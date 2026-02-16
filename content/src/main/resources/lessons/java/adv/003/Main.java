import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    enum Status { NEW, PAID, FAILED }

    record Order(String id, Status status, BigDecimal amount, List<String> tags) {}

    public static void main(String[] args) {
        List<Order> orders = List.of(
                new Order("A-100", Status.PAID,   new BigDecimal("10.50"), List.of("api", "smoke")),
                new Order("A-101", Status.NEW,    new BigDecimal("2.00"),  List.of("web")),
                new Order("A-102", Status.PAID,   new BigDecimal("7.25"),  List.of("api", "regression")),
                new Order("A-103", Status.FAILED, new BigDecimal("1.00"),  List.of("android", "smoke"))
        );

        // TODO 1: сумма amount для заказов со статусом PAID (BigDecimal)
        BigDecimal paidTotal = null;

        // TODO 2: уникальные теги (из всех orders), отсортировать по алфавиту, соединить через запятую
        String tags = null;

        // TODO 3: id первого FAILED заказа, если нет — "NONE"
        String firstFailed = null;

        System.out.println("PAID_TOTAL=" + paidTotal);
        System.out.println("TAGS=" + tags);
        System.out.println("FIRST_FAILED=" + firstFailed);
    }
}
