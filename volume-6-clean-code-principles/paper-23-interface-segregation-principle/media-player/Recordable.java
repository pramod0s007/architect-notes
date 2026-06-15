/**
 * Recordable — extends Playable with microphone / capture capability.
 *
 * Only devices that physically have a microphone implement this interface.
 * No class is ever forced to provide a fake or throwing implementation.
 */
public interface Recordable extends Playable {

    void startRecording();
    void stopRecording();
    String getRecording();
}
