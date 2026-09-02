// Routes a read to any replica that has caught up to at least the required
// LSN -- the position the client's own most recent write left the primary at.
// If none has, falls back to the primary itself rather than ever serving data
// older than what the client just wrote.
class ReadRouter {
    String route(Replica[] candidates, int requiredLsn, String primaryBio) {
        for (Replica r : candidates) {
            if (r.replicatedLsn >= requiredLsn) {
                return r.readBio() + " (served by " + r.id + ")";
            }
        }
        return primaryBio + " (served by PRIMARY -- no replica had caught up)";
    }
}
