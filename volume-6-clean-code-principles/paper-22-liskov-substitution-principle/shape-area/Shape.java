/**
 * Shape — contract for any geometric shape used in floor-plan billing.
 *
 * LSP requires that every implementor returns a correct, stable area()
 * regardless of how the object was constructed or mutated.
 */
public interface Shape {

    /**
     * Returns the area of this shape in square metres.
     *
     * @return area, always &gt;= 0
     */
    double area();

    /**
     * Returns a human-readable description including dimensions.
     *
     * @return description string
     */
    String describe();
}
