// One row-level change in the source database's true history. This is the
// GROUND TRUTH the database actually experienced -- every insert, update,
// and delete, in order. Whatever capture mechanism a consumer uses is only
// as complete as its ability to reconstruct this exact sequence.
class ChangeEvent {
    final String id;
    final String status; // null for a delete
    final int updatedAt;
    final boolean deleted;

    ChangeEvent(String id, String status, int updatedAt, boolean deleted) {
        this.id = id;
        this.status = status;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
    }
}
