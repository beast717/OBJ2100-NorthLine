package protocol;

/**
 * Representerer mulige utfall av en serveroperasjon.
 *
 * @author Ahmed
 */
public enum ResponseStatus {

    /** Operasjonen ble utført vellykket. */
    OK,

    /** Ticket med angitt ID ble ikke funnet. */
    ERROR_NOT_FOUND,

    /** Operasjonen er ikke tillatt i henvendelses nåværende tilstand. */
    ERROR_INVALID_STATE,

    /** Serveren mottok en ukjent eller ugyldig operasjonskode. */
    ERROR_UNKNOWN_OPERATION,

    /** Ingen PENDING-henvendelser tilgjengelig ved FETCH. */
    ERROR_NO_TICKETS;
}