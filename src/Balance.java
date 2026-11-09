import java.util.Objects;

/**
 * Неизменяемый денежный баланс: сумма в копейках (минимальных единицах) + валюта.
 */
public final class Balance {

    private final long amountInKopecks;
    private final Currency currency;

    public Balance(long amountInKopecks, Currency currency) {
        if (currency == null) {
            throw new IllegalArgumentException("Валюта не может быть null");
        }
        if (amountInKopecks < 0) {
            throw new IllegalArgumentException("Сумма баланса не может быть отрицательной");
        }
        this.amountInKopecks = amountInKopecks;
        this.currency = currency;
    }

    public static Balance zero(Currency currency) {
        return new Balance(0, currency);
    }

    public long getAmountInKopecks() {
        return amountInKopecks;
    }

    public Currency getCurrency() {
        return currency;
    }

    /**
     * Сложение: возвращает новый Balance.
     */
    public Balance add(Balance other) {
        requireSameCurrency(other, "add");
        return new Balance(Math.addExact(this.amountInKopecks, other.amountInKopecks), currency);
    }

    /**
     * Вычитание: возвращает новый Balance. Не допускает уход в минус.
     */
    public Balance subtract(Balance other) {
        requireSameCurrency(other, "subtract");
        if (other.amountInKopecks > this.amountInKopecks) {
            throw new IllegalArgumentException("Недостаточно средств для вычитания");
        }
        return new Balance(this.amountInKopecks - other.amountInKopecks, currency);
    }

    /**
     * Конвертация по курсу из enum: 1 основная единица валюты = priceInRublesKopecks копеек.
     * Округление — вниз (в пользу банка), чтобы не создавать деньги из воздуха.
     */
    public Balance convertTo(Currency target) {
        if (target == null) {
            throw new IllegalArgumentException("Целевая валюта не может быть null");
        }
        if (target == currency) {
            return this;
        }
        // amountKopecks * priceSource / priceTarget  (все курсы выражены в рублёвых копейках)
        long result = Math.multiplyExact(amountInKopecks, currency.getPriceInRublesKopecks())
                / target.getPriceInRublesKopecks();
        return new Balance(result, target);
    }

    private void requireSameCurrency(Balance other, String op) {
        if (other == null) {
            throw new IllegalArgumentException("Аргумент операции " + op + " не может быть null");
        }
        if (other.currency != this.currency) {
            throw new IllegalArgumentException(
                    "Несовпадение валют в операции " + op + ": " + currency + " vs " + other.currency);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Balance)) return false;
        Balance other = (Balance) o;
        return amountInKopecks == other.amountInKopecks && currency == other.currency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amountInKopecks, currency);
    }

    @Override
    public String toString() {
        long major = amountInKopecks / 100;
        long minor = amountInKopecks % 100;
        return String.format("%d.%02d %s", major, minor, currency.getSymbol());
    }
}
