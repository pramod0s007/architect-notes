# Search Request — Builder Pattern with Cross-Field Validation

## What This Demonstrates

Builder Pattern applied to an e-commerce search request with 10 fields: query,
category, min/max price, minimum rating, in-stock filter, page, page size, sort
field, and sort order. The builder enforces cross-field invariants — most
importantly that `maxPrice >= minPrice` — before the immutable `SearchRequest` is
allowed to exist.

**Pressure: Complex Object Construction** — search APIs expose many optional
filters. Before the builder, callers used positional constructors like
`new SearchRequest("headphones", null, 50.0, 300.0, 4.0, true, 0, 20, "price", SortOrder.ASC)`.
Swapping the argument order of two adjacent `double` parameters (`minPrice` and
`maxPrice`) compiled without error but silently inverted the price filter,
returning no results instead of the expected range.

## Class Diagram

```
SortOrder (enum)
  ASC | DESC

SearchRequest (immutable)
────────────────────────────────────────────────
- query: String              [required]
- category: String           [default: null]
- minPrice: double           [default: 0.0]
- maxPrice: double           [default: Double.MAX_VALUE]
- minRating: double          [default: 0.0]
- inStockOnly: boolean       [default: false]
- page: int                  [default: 0]
- pageSize: int              [default: 20]
- sortBy: String             [default: "relevance"]
- sortOrder: SortOrder       [default: DESC]
────────────────────────────────────────────────
+ hasCategory(), hasPriceFilter(), hasRatingFilter()
+ getQuery(), getMinPrice(), getMaxPrice(), ...

      △ built by
      │
SearchRequest.Builder
────────────────────────────────────────────────
Builder(query)             ← only required field
+ category(String)   : Builder
+ minPrice(double)   : Builder
+ maxPrice(double)   : Builder
+ minRating(double)  : Builder
+ inStockOnly(boolean): Builder
+ page(int)          : Builder
+ pageSize(int)      : Builder
+ sortBy(String)     : Builder
+ sortOrder(SortOrder): Builder
+ build()            : SearchRequest
   └─ minPrice >= 0
   └─ maxPrice >= minPrice     ← cross-field invariant
   └─ minRating in [0..5]
   └─ page >= 0
   └─ pageSize in [1..100]

SearchService
────────────────────────────────────────────────
+ search(SearchRequest): void
   └─ reads request fields, prints active filters and pagination
```

## Cross-Field Validation at `build()`

Setting `minPrice` and `maxPrice` individually is safe — each setter just stores
the value. The constraint is checked only when the whole picture is available:

```
new SearchRequest.Builder("laptop")
    .minPrice(500.0)
    .maxPrice(100.0)   // not validated here — just stored
    .build()           // IllegalStateException: maxPrice (100.0) must be >= minPrice (500.0)
```

This is the key reason validation belongs in `build()` rather than in each
setter: `maxPrice(100.0)` followed by `minPrice(500.0)` would throw from the
setter if it validated eagerly, even though the final combination would be valid.

## How to Run

```bash
cd volume-4-architect-level-thinking/paper-11-builder-pattern/search-request
javac *.java && java Main
```

Expected output (abbreviated):

```
=== 1. Simple Keyword Search ===
  Active filters : [query='wireless headphones']
  Sort           : relevance DESC
  Page           : 0 (size=20)

=== 2. Filtered Category Browse ===
  Active filters : [query='headphones', category='Electronics > Audio',
                    price=[50.0..300.0], rating>=4.0, inStock=true]
  Sort           : price ASC

=== 3. Paginated Brand Search ===
  Active filters : [query='Sony', category='Electronics', inStock=true]
  Sort           : popularity DESC
  Page           : 2 (size=10)

=== 4. Validation Demo ===
Caught expected error: maxPrice (100.0) must be >= minPrice (500.0)
Caught expected error: Search query cannot be blank
```

## Design Decisions

- **`SearchService` accepts `SearchRequest`, not individual parameters** — the
  service is decoupled from how the request was built. Any combination of
  filters constructed via the builder is valid by the time `search()` is called.
  This makes the service easy to test: pass any builder configuration, including
  extreme cases, without touching the service code.
- **`maxPrice` defaults to `Double.MAX_VALUE`, not `null`** — avoids a nullable
  `Double` wrapper. The `hasPriceFilter()` method checks whether the range
  differs from the all-inclusive default, hiding the sentinel value from callers.
- **`query` required in the `Builder` constructor** — a search without a query
  string is not a valid search request. Requiring it in the constructor means the
  compiler rejects any call that forgets it, unlike a setter that could be
  silently omitted.
- **`inStockOnly` defaults to `false`** — broadest result set by default.
  Callers who want only in-stock items add one explicit call; callers who want all
  items (the majority) write nothing.

## When to Apply

- A request or query object has many optional parameters with sensible defaults
  and cross-field constraints that cannot be checked field by field.
- The object will be passed to a service that should not be burdened with
  validating its own inputs.

## When NOT to Apply

- A search with only one or two optional filters — a simple method signature with
  optional parameters is clearer than a full builder class.
