// v2: Same interface — save() is still part of the contract.
// The pressure is coming from ReadOnlyDocument below.
public interface Document {
    String getTitle();
    String getContent();
    void save();
}
