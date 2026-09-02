class LogEntry {
    final String serviceName;
    final String identifierField;
    final String identifierValue;
    final String message;

    LogEntry(String serviceName, String identifierField, String identifierValue, String message) {
        this.serviceName = serviceName;
        this.identifierField = identifierField;
        this.identifierValue = identifierValue;
        this.message = message;
    }
}
