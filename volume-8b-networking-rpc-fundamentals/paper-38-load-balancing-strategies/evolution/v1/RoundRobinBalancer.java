/** Cycles through backends in fixed order, ignoring how much work each one can actually do. */
public class RoundRobinBalancer {
    private final Backend[] backends;
    private int next = 0;

    RoundRobinBalancer(Backend[] backends) {
        this.backends = backends;
    }

    void assign(int requests) {
        for (int i = 0; i < requests; i++) {
            backends[next].assigned++;
            next = (next + 1) % backends.length;
        }
    }
}
