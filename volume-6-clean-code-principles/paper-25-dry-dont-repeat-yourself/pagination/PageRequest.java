/**
 * DRY: single definition of pagination parameters and their constraints.
 *
 * Before DRY: each repository and controller had its own page/size parsing
 * and its own bounds checks — often inconsistently capped at different values.
 */
public class PageRequest {

    private final int page;
    private final int size;

    public PageRequest(int page, int size) {
        this.page = page;
        this.size = size;
        validate();
    }

    private void validate() {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be negative, got: " + page);
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100, got: " + size);
        }
    }

    public int getPage() { return page; }
    public int getSize() { return size; }
    public int getOffset() { return page * size; }

    @Override
    public String toString() {
        return "PageRequest{page=" + page + ", size=" + size + "}";
    }
}
