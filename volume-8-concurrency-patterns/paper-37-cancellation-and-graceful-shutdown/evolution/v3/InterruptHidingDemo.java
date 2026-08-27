// Demonstrates that swallowing InterruptedException without restoring the flag
// hides the interrupt from code further up the call stack.
class InterruptHidingDemo {

    // The "wrong direction": catches and discards, no restore.
    static void innerSwallows() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            // ignored — no restore, no break, no log
        }
    }

    static boolean runAndCheckIfCallerSeesInterrupt() throws InterruptedException {
        Thread self = Thread.currentThread();
        Thread interruptor = new Thread(() -> {
            try {
                Thread.sleep(20);
            } catch (InterruptedException ignored) {
            }
            self.interrupt();
        });
        interruptor.start();

        innerSwallows(); // the interrupt happens WHILE this is sleeping
        interruptor.join();

        // A truly-interrupted caller should see isInterrupted() == true here.
        return self.isInterrupted();
    }
}
