import java.util.List;

// The overcorrection: the read quorum SIZE satisfies W+R>N, so it feels like
// the consistency problem is already solved -- but this reader just returns
// whichever replica responds first, in arrival order, without ever comparing
// versions. Satisfying the count is necessary. It was never sufficient on its
// own; the client still has to actually use the overlap it's guaranteed.
class FirstResponseReader {
    static Replica readFirst(List<Replica> responsesInArrivalOrder) {
        return responsesInArrivalOrder.get(0);
    }
}
