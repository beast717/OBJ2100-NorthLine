package protocol;

/**
 * Representerer de gyldige operasjonene i klient-server-protokollen.
 *
 * @author Ahmed
 */
public enum Operation {

    /** Registrer en ny henvendelse i systemet. */
    REGISTER,

    /** Hent neste tilgjengelige (PENDING) henvendelse. */
    FETCH,

    /** Kanseller en eksisterende PENDING-henvendelse. */
    CANCEL,

    /** Marker en ASSIGNED-henvendelse som fullført. */
    COMPLETE;
}