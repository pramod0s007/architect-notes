class Service {
    final String name;
    final String shape; // "HTTP_STANDARD" or "BATCH_JOB"

    Service(String name, String shape) {
        this.name = name;
        this.shape = shape;
    }
}
