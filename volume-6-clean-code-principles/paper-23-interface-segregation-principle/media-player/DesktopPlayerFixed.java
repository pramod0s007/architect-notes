/**
 * DesktopPlayerFixed — full-featured desktop, ISP-compliant.
 *
 * Implements Recordable and Streamable (both extend Playable) because this
 * hardware genuinely supports all three capability groups.
 * Every method is a real implementation — no throws, no no-ops.
 */
public class DesktopPlayerFixed implements Recordable, Streamable {

    private boolean streaming = false;
    private String  lastRecording = null;

    @Override public void play(String file) {
        System.out.println("DesktopFixed: playing " + file);
    }

    @Override public void pause() {
        System.out.println("DesktopFixed: paused");
    }

    @Override public void stop() {
        streaming = false;
        System.out.println("DesktopFixed: stopped");
    }

    @Override public void startRecording() {
        System.out.println("DesktopFixed: recording started");
    }

    @Override public void stopRecording() {
        lastRecording = "recording-" + System.currentTimeMillis() + ".mp4";
        System.out.println("DesktopFixed: recording saved as " + lastRecording);
    }

    @Override public String getRecording() { return lastRecording; }

    @Override public void streamTo(String url) {
        streaming = true;
        System.out.println("DesktopFixed: streaming to " + url);
    }

    @Override public boolean isStreaming() { return streaming; }
}
