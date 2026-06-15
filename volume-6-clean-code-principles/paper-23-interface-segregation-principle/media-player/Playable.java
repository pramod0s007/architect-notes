/**
 * Playable — minimal contract every media device can honour.
 *
 * ISP fix: segregate the fat MediaPlayer into focused role interfaces.
 * Devices implement only the interfaces that match their hardware capabilities.
 */
public interface Playable {

    void play(String file);
    void pause();
    void stop();
}
