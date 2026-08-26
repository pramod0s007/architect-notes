// The 3rd concern (digital signing) arrives here as ONE new class — not a doubled tree.
class Signer implements OutputTransform {
    public String apply(String input) {
        return "[SIGNED]" + input + "[/SIGNED]";
    }
}
