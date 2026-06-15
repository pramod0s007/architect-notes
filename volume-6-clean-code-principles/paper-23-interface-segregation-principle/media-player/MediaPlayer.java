/**
 * MediaPlayer — fat interface that bundles playback, recording, and streaming.
 *
 * ISP violation: not every media device supports all three capability groups.
 * A mobile player with no microphone is forced to provide recording methods;
 * a set-top box with no network stack is forced to provide streaming methods.
 * Each forced method either throws at runtime or silently does nothing —
 * both outcomes mislead callers.
 */
public interface MediaPlayer {

    // --- Playback ---
    void play(String file);
    void pause();
    void stop();

    // --- Recording (not all hardware has a microphone) ---
    void startRecording();
    void stopRecording();
    String getRecording();

    // --- Streaming (not all hardware has a stable outbound connection) ---
    void streamTo(String url);
    boolean isStreaming();
}
