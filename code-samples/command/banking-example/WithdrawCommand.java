public final class WithdrawCommand implements Command {

    private final BankAccount account;
    private final double amount;

    public WithdrawCommand(BankAccount account, double amount) {
        if (account == null) {
            throw new IllegalArgumentException("account must not be null");
        }
        this.account = account;
        this.amount = amount;
    }

    @Override
    public void execute() {
        account.withdraw(amount);
    }

    @Override
    public void undo() {
        account.deposit(amount);
    }

    @Override
    public String name() {
        return "withdraw";
    }
}
