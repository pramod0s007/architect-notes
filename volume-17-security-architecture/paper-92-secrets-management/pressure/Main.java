public class Main {
    public static void main(String[] args) {
        // The exact same compiled class -- and therefore the exact same
        // hardcoded API_KEY literal -- ships to every environment. There is
        // no such thing as "the dev key" and "the prod key" here; there is
        // only PaymentGateway.API_KEY, wherever this class file goes.
        System.out.println("Source code (and therefore this compiled class) is identical across dev and prod.");
        System.out.println("Anyone with access to the source -- a contractor, a leaked repo, a dev laptop --");
        System.out.println("can read the literal directly:");
        String leakedFromSource = PaymentGateway.API_KEY;
        System.out.println("  Leaked key (read straight out of source): " + leakedFromSource);

        System.out.println();
        System.out.println("That leaked key is used to authorize a real, live production charge of 500000c:");
        boolean authorized = PaymentGateway.authorizeCharge(leakedFromSource, 500000);
        System.out.println("  Production charge authorized: " + authorized);

        System.out.println();
        System.out.println(authorized
            ? "CONFIRMED FAILURE: a key extracted from source code -- which has no separate identity from "
              + "the real production credential, because they are literally the same compiled constant -- "
              + "successfully authorized a real production charge. Whoever has read access to the "
              + "codebase, at any security tier, has the production secret, whether that was ever the "
              + "intent or not."
            : "Unexpected -- investigate.");
    }
}
