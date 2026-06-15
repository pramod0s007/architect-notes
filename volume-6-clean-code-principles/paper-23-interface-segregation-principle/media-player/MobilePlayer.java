/**
 * MobilePlayer — basic mobile device: playback only, no microphone, no outbound stream.
 *
 * // ISP violation: 3 methods forced on mobile that hardware doesn't support.
 * startRecording, stopRecording, getRecording, and streamTo all throw at runtime.
 * Callers that treat MobilePlayer as a MediaPlayer will get runtime surprises.
 */
public class MobilePlayer implements MediaPlayer {

    @Override public void play(String file) {
        System.out.println("Mobile: playing " + file);
    }

    @Override public void pause() {
        System.out.println("Mobile: paused");
    }

    @Override public void stop() {
        System.out.println("Mobile: stopped");
    }

    // ISP violation: mobile has no microphone — forced implementation throws
    @Override public void startRecording() {
        throw new UnsupportedOperationException("Mobile has no microphone");
    }

    // ISP violation: forced implementation throws
    @Override public void stopRecording() {
        throw new UnsupportedOperationException("Mobile has no microphone");
    }

    // ISP violation: forced implementation throws
    @Override public String getRecording() {
        throw new UnsupportedOperationException("Mobile has no microphone");
    }

    // ISP violation: mobile cannot push an outbound stream in this scenario
    @Override public void streamTo(String url) {
        throw new UnsupportedOperationException("Mobile streaming not supported");
    }

    @Override public boolean isStreaming() {
        return false;
    }
}
