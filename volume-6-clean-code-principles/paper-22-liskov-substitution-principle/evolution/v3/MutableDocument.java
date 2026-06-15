// MutableDocument extends Document with save() only for types that can truly support it.
// AutoSaveService and anything that needs to save operates on this type, not Document.
public interface MutableDocument extends Document {
    void save();
}
