// Exactly one lock per operation. No second lock is ever held while waiting for this one,
// so circular wait — and therefore deadlock — is structurally impossible here.
class AccountService {
    void deposit(Account account, double amount) {
        synchronized (account) {
            account.credit(amount);
        }
    }
}
