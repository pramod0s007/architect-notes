interface Callable<T> {
    T call() throws TimeoutException;
}
