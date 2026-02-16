public class Main {
    public static void main(String[] args) {
        int[] codes = {200, 201, 404, 204, 500};

        int ok = 0;
        int fail = 0;

        for (int code : codes) {
            if (is2xx(code)) {
                System.out.println("OK: " + code);
                ok++;
            } else {
                System.out.println("FAIL: " + code);
                fail++;
            }
        }

        System.out.println("OK_COUNT=" + ok);
        System.out.println("FAIL_COUNT=" + fail);
    }

    static boolean is2xx(int code) {
        // TODO:
        // Верните true, если code в диапазоне 200..299 включительно.
        // В реальных AQA-тестах такой метод переиспользуется в проверках statusCode.
        return false;
    }
}
