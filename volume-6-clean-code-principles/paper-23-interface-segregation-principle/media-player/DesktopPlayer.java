/**
 * DesktopPlayer — full-featured desktop application.
 * Implements every method of the fat MediaPlayer interface meaningfully.
 */
public class DesktopPlayer implements MediaPlayer {

    private boolean streaming  = false;
    private boolean recording  = false;
    private String  lastRecording = null;

    @Override public void play(String file) {
        System.out.println("Desktop: playing " + file);
    }

    @Override public void pause() {
        System.out.println("Desktop: paused");
    }

    @Override public void stop() {
        streaming = false;
        System.out.println("Desktop: stopped");
    }

    @Override public void startRecording() {
        recording = true;
        System.out.println("Desktop: recording started");
    }

    @Override public void stopRecording() {
        recording      = false;
        lastRecording  = "desktop-recording-" + System.currentTimeMillis() + ".mp4";
        System.out.println("Desktop: recording saved as " + lastRecording);
    }

    @Override public String getRecording() {
        return lastRecording;
    }

    @Override public void streamTo(String url) {
        streaming = true;
        System.out.println("Desktop: streaming to " + url);
    }

    @Override public boolean isStreaming() {
        return streaming;
    }
}
