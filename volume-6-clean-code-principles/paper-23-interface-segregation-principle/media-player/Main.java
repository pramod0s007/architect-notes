/** Demos the fat MediaPlayer ISP violation, then the segregated fix. */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== VIOLATION: Fat MediaPlayer interface ===");
        MediaPlayer desktop = new DesktopPlayer();
        MediaPlayer mobile  = new MobilePlayer();

        desktop.play("intro.mp4");
        desktop.startRecording();
        desktop.stopRecording();
        desktop.streamTo("rtmp://live.example.com/stream1");

        mobile.play("podcast.mp3");
        System.out.println("Calling mobile.startRecording():");
        try { mobile.startRecording(); }
        catch (UnsupportedOperationException e) { System.out.println("  " + e.getMessage()); }

        System.out.println("Calling mobile.streamTo():");
        try { mobile.streamTo("rtmp://live.example.com/stream2"); }
        catch (UnsupportedOperationException e) { System.out.println("  " + e.getMessage()); }

        System.out.println();
        System.out.println("=== FIX: Playable / Recordable / Streamable ===");
        DesktopPlayerFixed fixedDesktop = new DesktopPlayerFixed();
        MobilePlayerFixed  fixedMobile  = new MobilePlayerFixed();

        // Both implement Playable — polymorphic, no surprises
        playTrack(fixedDesktop, "session.mp4");
        playTrack(fixedMobile,  "podcast.mp3");

        // Only desktop can record or stream — compiler enforced
        record(fixedDesktop);
        stream(fixedDesktop, "rtmp://live.example.com/stream1");
        System.out.println("MobileFixed has no recording or streaming methods at all.");
    }

    private static void playTrack(Playable p, String file) { p.play(file); p.stop(); }

    private static void record(Recordable r) {
        r.startRecording();
        r.stopRecording();
        System.out.println("Recording: " + r.getRecording());
    }

    private static void stream(Streamable s, String url) {
        s.streamTo(url);
        System.out.println("isStreaming: " + s.isStreaming());
        s.stop();
    }
}
