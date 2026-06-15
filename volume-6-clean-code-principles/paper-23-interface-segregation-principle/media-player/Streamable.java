/**
 * Streamable — extends Playable with outbound broadcast capability.
 *
 * Only devices capable of pushing a stream to a remote URL implement this.
 * Mobile or offline devices simply don't implement Streamable at all.
 */
public interface Streamable extends Playable {

    void streamTo(String url);
    boolean isStreaming();
}
