import java.util.List;

/**
 * DRY: single definition of a paginated response envelope.
 *
 * Generic so it works for products, orders, users — any entity type.
 * Total-pages calculation lives here once, not in every caller.
 */
public class PageResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    private PageResponse(List<T> content, int page, int size, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
    }

    public static <T> PageResponse<T> of(List<T> content, PageRequest req, long totalElements) {
        return new PageResponse<>(content, req.getPage(), req.getSize(), totalElements);
    }

    public List<T> getContent()      { return content; }
    public int getPage()             { return page; }
    public int getSize()             { return size; }
    public long getTotalElements()   { return totalElements; }
    public int getTotalPages()       { return totalPages; }
    public boolean hasNext()         { return page + 1 < totalPages; }

    @Override
    public String toString() {
        return "Page " + page + "/" + (totalPages - 1)
                + " | items=" + content.size()
                + " | total=" + totalElements;
    }
}
