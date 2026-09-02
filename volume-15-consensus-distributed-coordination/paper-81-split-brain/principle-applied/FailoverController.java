// Before promoting a new primary, the controller must POSITIVELY confirm the
// old one has been stopped -- not just that the controller can't see it
// anymore. Fencing happens over a shared, independent channel (here, a
// storage-level access grant) that the controller can reach even in exactly
// the scenario where its health-check path to the primary itself is down.
class FailoverController {
    boolean promoteNewPrimary(PrimaryNode oldPrimary, PrimaryNode newPrimary, boolean fencingChannelReachable) {
        if (!fencingChannelReachable) {
            // Can't confirm the old primary is actually stopped -- refuse to
            // promote. No primary at all is safer than two.
            return false;
        }
        oldPrimary.revokeWriteAccess();
        // A real controller would also activate newPrimary here (register it
        // with clients/DNS/proxy) -- omitted since this demo only measures
        // whether the OLD primary got fenced, not the promotion mechanics.
        return true;
    }
}
