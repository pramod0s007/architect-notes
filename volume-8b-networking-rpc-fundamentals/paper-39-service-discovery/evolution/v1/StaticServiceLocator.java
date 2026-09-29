import java.util.*;

/**
 * A hardcoded list of backend addresses, fixed at deploy time. Nothing about this class
 * knows whether the addresses it returns are still backed by a running instance.
 */
public class StaticServiceLocator {
    private final List<String> addresses;

    StaticServiceLocator(List<String> addresses) {
        this.addresses = addresses;
    }

    List<String> resolve() {
        return addresses;
    }
}
