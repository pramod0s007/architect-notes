import java.util.ArrayList;
import java.util.List;

/**
 * PRESSURE: an audit log that records who accessed what, and when, is stored as ordinary mutable
 * rows in the same database everything else lives in. It's the simplest way to add auditing to an
 * existing system -- just another table, with an INSERT for every access. But "just another
 * table" means it's also reachable by UPDATE and DELETE, by anyone with database access, the same
 * as any other table. When an unauthorized access needs to be covered up, whether by an insider or
 * by an attacker who has already compromised the database, removing the evidence is exactly as
 * easy as removing the access ever happened at all -- and the log itself has no way to notice, or
 * to tell anyone afterward, that this happened.
 */
public class Main {
    public static void main(String[] args) {
        AuditLog log = new AuditLog();

        log.append("alice", "VIEW_SSN", "customer-401");
        log.append("bob", "VIEW_SSN", "customer-402");
        log.append("carol", "VIEW_SSN", "customer-401");

        System.out.println("Audit log after three accesses:");
        printLog(log);

        System.out.println();
        System.out.println("An incident response investigation is opened after customer-401 reports "
                + "their SSN was viewed by someone they never interacted with. Before the "
                + "investigation reaches the log, the entry for carol's access is deleted from the "
                + "table directly:");
        log.delete(2);

        System.out.println();
        System.out.println("Audit log after the deletion:");
        printLog(log);

        System.out.println();
        System.out.println("Result: the log now shows only two accesses to customer-401's SSN, both "
                + "attributable to employees with a legitimate reason to view it. Carol's access --"
                + " the one the investigation was actually opened to find -- left no trace, and "
                + "nothing about the log's own structure indicates that a third entry ever existed "
                + "or was ever removed.");
    }

    private static void printLog(AuditLog log) {
        List<AuditEntry> entries = log.entries();
        for (int i = 0; i < entries.size(); i++) {
            AuditEntry e = entries.get(i);
            System.out.println("  [" + i + "] actor=" + e.actor + " action=" + e.action
                    + " subject=" + e.subject);
        }
    }

    static class AuditEntry {
        String actor;
        String action;
        String subject;

        AuditEntry(String actor, String action, String subject) {
            this.actor = actor;
            this.action = action;
            this.subject = subject;
        }
    }

    static class AuditLog {
        private final List<AuditEntry> entries = new ArrayList<>();

        void append(String actor, String action, String subject) {
            entries.add(new AuditEntry(actor, action, subject));
        }

        void delete(int index) {
            entries.remove(index);
        }

        List<AuditEntry> entries() {
            return entries;
        }
    }
}
