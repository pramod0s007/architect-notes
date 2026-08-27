// No lock, anywhere. Every step is "tell, then enqueue the next step" — never "ask, then block".
class TransferCoordinator extends Actor {
    void transfer(AccountActor from, AccountActor to, double amount, Runnable onComplete) {
        send(() ->
            from.debit(amount, () ->
                send(() ->
                    to.credit(amount, () ->
                        send(onComplete)
                    )
                )
            )
        );
    }
}
