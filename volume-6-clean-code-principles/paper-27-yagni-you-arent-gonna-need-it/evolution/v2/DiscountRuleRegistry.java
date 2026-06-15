import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

// Registry with one entry. All this ceremony for a single rule.
public class DiscountRuleRegistry {

    private final Map<String, DiscountRule> rules = new HashMap<>();

    public void register(String name, DiscountRule rule) {
        rules.put(name, rule);
    }

    public DiscountRule get(String name) {
        return rules.get(name);
    }

    public Collection<DiscountRule> getAll() {
        return rules.values();
    }
}
