/**
 * Loan applicant — the candidate object evaluated by specifications.
 */
public final class Customer {

    private final String name;
    private final int age;
    private final double annualIncome;
    private final int creditScore;
    private final boolean isBlacklisted;
    private final String country;
    private final boolean hasOutstandingLoans;
    private final boolean isPremium;

    public Customer(String name, int age, double annualIncome, int creditScore,
                    boolean isBlacklisted, String country,
                    boolean hasOutstandingLoans, boolean isPremium) {
        this.name = name;
        this.age = age;
        this.annualIncome = annualIncome;
        this.creditScore = creditScore;
        this.isBlacklisted = isBlacklisted;
        this.country = country;
        this.hasOutstandingLoans = hasOutstandingLoans;
        this.isPremium = isPremium;
    }

    public String getName()            { return name; }
    public int getAge()                { return age; }
    public double getAnnualIncome()    { return annualIncome; }
    public int getCreditScore()        { return creditScore; }
    public boolean isBlacklisted()     { return isBlacklisted; }
    public String getCountry()         { return country; }
    public boolean hasOutstandingLoans() { return hasOutstandingLoans; }
    public boolean isPremium()         { return isPremium; }

    @Override
    public String toString() {
        return String.format("Customer{name='%s', age=%d, income=%.0f, credit=%d, " +
                "blacklisted=%b, country='%s', outstandingLoans=%b, premium=%b}",
                name, age, annualIncome, creditScore,
                isBlacklisted, country, hasOutstandingLoans, isPremium);
    }
}
