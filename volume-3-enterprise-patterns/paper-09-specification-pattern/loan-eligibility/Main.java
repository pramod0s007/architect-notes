import java.util.List;

/**
 * Run: javac *.java && java Main
 */
public final class Main {

    public static void main(String[] args) {

        EligibilityService service = new EligibilityService();

        List<Customer> applicants = List.of(

            // Passes all standard rules
            new Customer("Alice Müller",  34, 75_000, 720, false, "DE", false, false),

            // Too young
            new Customer("Bob Chen",      19, 45_000, 700, false, "US", false, false),

            // Income too low
            new Customer("Carol Davis",   28, 22_000, 680, false, "US", false, false),

            // Credit score too low — but premium + high income override
            new Customer("David Park",    42, 150_000, 580, false, "KR", false, true),

            // Blacklisted — denied even with premium status
            new Customer("Eve Torres",    35, 200_000, 800, true, "ES", true, true),

            // Fails credit AND income — two rules broken
            new Customer("Frank Okafor",  30, 18_000, 600, false, "NG", false, false)
        );

        System.out.printf("%-18s %-10s %s%n", "Applicant", "Decision", "Failed rules");
        System.out.println("-".repeat(70));

        for (Customer customer : applicants) {
            boolean eligible = service.isEligible(customer);
            List<String> failed = service.getFailedRules(customer);

            String decision = eligible ? "APPROVED" : "DENIED";
            String failedStr = failed.isEmpty() ? "—" : String.join(", ", failed);

            System.out.printf("%-18s %-10s %s%n",
                    customer.getName(), decision, failedStr);
        }
    }
}
