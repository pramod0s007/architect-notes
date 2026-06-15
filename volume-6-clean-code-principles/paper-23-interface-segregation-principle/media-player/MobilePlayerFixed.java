/**
 * MobilePlayerFixed — basic mobile device, ISP-compliant.
 *
 * Implements Playable only — exactly the capabilities this hardware has.
 * No recording methods, no streaming methods, no UnsupportedOperationException.
 * Callers that need recording or streaming simply cannot pass a MobilePlayerFixed
 * to those APIs — the compiler enforces the contract.
 */
public class MobilePlayerFixed implements Playable {

    @Override public void play(String file) {
        System.out.println("MobileFixed: playing " + file);
    }

    @Override public void pause() {
        System.out.println("MobileFixed: paused");
    }

    @Override public void stop() {
        System.out.println("MobileFixed: stopped");
    }
}
