public class Main {

    public static void main(String[] args) {

        BankAccount p1 = new BankAccount(
                "ACC-001", "Маша", new Balance(10_000_00, Currency.RUB), 5_000_00);
        BankAccount p2 = new BankAccount(
                "ACC-002", "Миша", new Balance(50_00, Currency.USD), 10_000_00);

        System.out.println("Старт:");
        System.out.println("  " + p1);
        System.out.println("  " + p2);

        p1.deposit(new Balance(150_00, Currency.RUB));
        System.out.println("\nПосле пополнения Маши:");
        System.out.println("  " + p1);

        p1.transferTo(p2, new Balance(200_00, Currency.RUB));
        System.out.println("\nПосле перевода 200 ₽ от Маши к Мише:");
        System.out.println("  " + p1);
        System.out.println("  " + p2);

        Balance зачислено = p2.transferTo(p1, new Balance(10_00, Currency.USD));
        System.out.println("\nПосле перевода 10 USD от Миши к Маше (зачислено: " + зачислено + "):");
        System.out.println("  " + p1);
        System.out.println("  " + p2);

        System.out.println("\n100 USD в EUR: "
                + new Balance(100_00, Currency.USD).convertTo(Currency.EUR));

        System.out.println("\nПроверка корнер-кейсов:");

        try {
            new Balance(-1, Currency.RUB);
        } catch (IllegalArgumentException e) {
            System.out.println("  [ok] отрицательный баланс: " + e.getMessage());
        }

        try {
            new Balance(100, Currency.RUB).subtract(new Balance(200, Currency.RUB));
        } catch (IllegalArgumentException e) {
            System.out.println("  [ok] недостаточно средств: " + e.getMessage());
        }

        try {
            new Balance(100, Currency.RUB).add(new Balance(100, Currency.USD));
        } catch (IllegalArgumentException e) {
            System.out.println("  [ok] разные валюты: " + e.getMessage());
        }

        try {
            p1.withdraw(new Balance(1_000_000_00, Currency.RUB));
        } catch (IllegalStateException e) {
            System.out.println("  [ok] превышение лимита: " + e.getMessage());
        }

        p2.block();
        try {
            p2.deposit(new Balance(100, Currency.USD));
        } catch (IllegalStateException e) {
            System.out.println("  [ok] заблокированный счёт: " + e.getMessage());
        }
        p2.unblock();

        try {
            p1.deposit(null);
        } catch (IllegalArgumentException e) {
            System.out.println("  [ok] null-аргумент: " + e.getMessage());
        }

        try {
            p1.transferTo(p1, new Balance(1, Currency.RUB));
        } catch (IllegalArgumentException e) {
            System.out.println("  [ok] перевод самому себе: " + e.getMessage());
        }
    }
}