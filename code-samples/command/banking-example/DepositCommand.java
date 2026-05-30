public final class DepositCommand implements Command {

    private final BankAccount account;
    private final double amount;

    public DepositCommand(BankAccount account, double amount) {
        if (account == null) {
            throw new IllegalArgumentException("account must not be null");
        }
        this.account = account;
        this.amount = amount;
    }

    @Override
    public void execute() {
        account.deposit(amount);
    }

    @Override
    public void undo() {
        account.withdraw(amount);
    }

    @Override
    public String name() {
        return "deposit";
    }
}
