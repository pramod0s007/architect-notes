/**
 * BiologicalNeeds — interface for workers that require sustenance and rest.
 *
 * ISP fix: isolates biological life-cycle methods so that robots are never
 * forced to implement them.  Only HumanWorkerFixed implements this.
 */
public interface BiologicalNeeds {

    /** Take a meal break. */
    void eat();

    /** Rest during a sleep cycle. */
    void sleep();
}
