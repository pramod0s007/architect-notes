import java.util.Set;

/**
 * First gate: blocks requests from known-malicious IP addresses.
 * Returns 403 Forbidden immediately — no further handlers are invoked.
 */
public final class IpBlocklistHandler extends SecurityHandler {

    private final Set<String> blockedIps;

    public IpBlocklistHandler(Set<String> blockedIps) {
        this.blockedIps = Set.copyOf(blockedIps);
    }

    @Override
    public ApiResponse handle(ApiRequest request) {
        if (blockedIps.contains(request.getIp())) {
            System.out.printf("  [IpBlocklist]    BLOCKED  ip=%s%n", request.getIp());
            return ApiResponse.forbidden("IP address " + request.getIp() + " is blocked");
        }
        System.out.printf("  [IpBlocklist]    PASS     ip=%s%n", request.getIp());
        return passToNext(request);
    }
}
