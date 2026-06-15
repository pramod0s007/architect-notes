import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// KISS violation: 80+ lines for what ApiResponse handles in 35 lines.
// Adds generic type parameters, metadata objects, a builder, and a factory registry
// for problems that don't exist in the codebase.

abstract class ApiError {
    abstract String code();
    abstract String detail();
}

class ResponseMetadata {
    final String requestId = UUID.randomUUID().toString();
    final String timestamp = Instant.now().toString();
    final Map<String, String> headers = new HashMap<>();
}

class OverEngineeredApiResponse<T, E extends ApiError, M extends ResponseMetadata> {

    // KISS violation: generic type explosion — callers must spell out three types
    // for every response, even a simple "product not found".

    private final int statusCode;
    private final String message;
    private final T data;
    private final E error;
    private final M metadata;
    private final String version;

    private OverEngineeredApiResponse(Builder<T, E, M> builder) {
        this.statusCode = builder.statusCode;
        this.message    = builder.message;
        this.data       = builder.data;
        this.error      = builder.error;
        this.metadata   = builder.metadata;
        this.version    = builder.version;
    }

    // KISS violation: factory methods require callers to supply metadata
    // and an error type even for success responses.
    @SuppressWarnings("unchecked")
    public static <T> OverEngineeredApiResponse<T, ApiError, ResponseMetadata> success(T data) {
        return (OverEngineeredApiResponse<T, ApiError, ResponseMetadata>)
                new Builder<>().statusCode(200).message("OK").data(data)
                        .metadata(new ResponseMetadata()).version("v1").build();
    }

    public static class Builder<T, E extends ApiError, M extends ResponseMetadata> {
        private int statusCode;
        private String message;
        private T data;
        private E error;
        private M metadata;
        private String version = "v1";

        public Builder<T, E, M> statusCode(int v)  { this.statusCode = v; return this; }
        public Builder<T, E, M> message(String v)  { this.message = v;    return this; }
        public Builder<T, E, M> data(T v)          { this.data = v;       return this; }
        public Builder<T, E, M> error(E v)         { this.error = v;      return this; }
        public Builder<T, E, M> metadata(M v)      { this.metadata = v;   return this; }
        public Builder<T, E, M> version(String v)  { this.version = v;    return this; }

        public OverEngineeredApiResponse<T, E, M> build() {
            return new OverEngineeredApiResponse<>(this);
        }
    }

    @Override
    public String toString() {
        return "OverEngineeredApiResponse{" + statusCode + ", version=" + version
                + ", requestId=" + (metadata != null ? metadata.requestId : "none") + "}";
    }
}
