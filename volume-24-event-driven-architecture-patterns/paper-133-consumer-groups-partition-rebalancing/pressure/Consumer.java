import java.util.Map;

/** A consumer group member that processes messages and records each one it touches. */
public class Consumer {
    private final String name;
    private final Map<Integer, Integer> processCounts;

    public Consumer(String name, Map<Integer, Integer> processCounts) {
        this.name = name;
        this.processCounts = processCounts;
    }

    public void process(int messageIndex) {
        processCounts.merge(messageIndex, 1, Integer::sum);
        System.out.println("Consumer " + name + " processed message " + messageIndex);
    }
}
