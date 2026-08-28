// Watches an internal resource, not the thing users actually experience.
// A reasonable-sounding proxy: high CPU usually means the system is
// struggling. "Usually" is doing a lot of work in that sentence.
class CauseBasedAlerter {
    private static final double CPU_THRESHOLD = 0.90;

    boolean shouldAlert(double cpuUtilization) {
        return cpuUtilization > CPU_THRESHOLD;
    }
}
