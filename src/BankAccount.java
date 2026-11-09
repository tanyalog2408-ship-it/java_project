import java.util.Objects;

public final class BankAccount {

    private final String id;
    private final String owner;
    private Balance balance;
    private final long withdrawalLimitInRubKopecks;
    private boolean blocked;

    public BankAccount(String id, String owner, Balance initialBalance, long withdrawalLimitInRubKopecks) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id не может быть пустым");
        if (owner == null || owner.isBlank()) throw new IllegalArgumentException("owner не может быть пустым");
        if (initialBalance == null) throw new IllegalArgumentException("initialBalance не может быть null");
        if (withdrawalLimitInRubKopecks < 0) throw new IllegalArgumentException("Лимит не может быть отрицательным");

        this.id = id;
        this.owner = owner;
        this.balance = initialBalance;
        this.withdrawalLimitInRubKopecks = withdrawalLimitInRubKopecks;
    }

    public String getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public Balance getBalance() {
        return balance;
    }

    public long getWithdrawalLimitInRubKopecks() {
        return withdrawalLimitInRubKopecks;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void block() {
        this.blocked = true;
    }

    public void unblock() {
        this.blocked = false;
    }

    /**
     * Пополнение.
     */
    public void deposit(Balance amount) {
        requireNotBlocked();
        if (amount == null) throw new IllegalArgumentException("amount не может быть null");
        this.balance = this.balance.add(amount);
    }

    /**
     * Снятие/списание.
     */
    public void withdraw(Balance amount) {
        requireNotBlocked();
        if (amount == null) throw new IllegalArgumentException("amount не может быть null");
        checkWithdrawalLimit(amount);
        this.balance = this.balance.subtract(amount);
    }

    /**
     * Перевод между счетами. Валюты могут отличаться — сумма будет сконвертирована
     * по курсу из enum Currency. Лимит на вывод проверяется у отправителя
     * в его валюте. Возвращает фактически зачисленную сумму получателю
     * (после конвертации и округления).
     */
    public Balance transferTo(BankAccount target, Balance amount) {
        if (target == null) throw new IllegalArgumentException("target не может быть null");
        if (target == this) throw new IllegalArgumentException("Нельзя переводить на тот же счёт");
        requireNotBlocked();
        target.requireNotBlocked();
        if (amount == null) throw new IllegalArgumentException("amount не может быть null");

        checkWithdrawalLimit(amount);

        // Списываем у отправителя (в его валюте)
        this.balance = this.balance.subtract(amount);

        // Зачисляем получателю (с конвертацией при необходимости)
        Balance credited = amount.getCurrency() == target.balance.getCurrency()
                ? amount
                : amount.convertTo(target.balance.getCurrency());

        target.balance = target.balance.add(credited);
        return credited;
    }

    private void checkWithdrawalLimit(Balance amount) {
        // Лимит задан в рублях; сравниваем, переведя сумму в RUB
        long amountInRub = amount.convertTo(Currency.RUB).getAmountInKopecks();
        if (amountInRub > withdrawalLimitInRubKopecks) {
            throw new IllegalStateException(String.format(
                    "Превышен лимит на вывод: сумма %s, лимит %.2f ₽",
                    amount, withdrawalLimitInRubKopecks / 100.0));
        }
    }

    private void requireNotBlocked() {
        if (blocked) throw new IllegalStateException("Счёт " + id + " заблокирован");
    }

    @Override
    public String toString() {
        return String.format(
                "BankAccount{id='%s', owner='%s', balance=%s, limit=%.2f ₽, blocked=%s}",
                id, owner, balance, withdrawalLimitInRubKopecks / 100.0, blocked);
    }
}