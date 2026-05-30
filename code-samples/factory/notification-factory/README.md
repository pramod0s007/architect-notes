# Notification Factory

Runnable sample for **Factory Pattern** (Paper 12).

## Run

```bash
cd code-samples/factory/notification-factory
javac *.java
java Main
```

## Usage

```java
Notification n = NotificationFactory.create("EMAIL");
n.send("user@example.com", "Your order shipped");
```

Creation variation is centralized; callers depend on `Notification`, not concrete types.
