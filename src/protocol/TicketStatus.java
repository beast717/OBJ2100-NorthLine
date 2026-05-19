package protocol;

/**
 * Representerer mulige tilstander en henvendelse kan ha i løpet av sin livssyklus.
 *
 * @author Ahmed
 */
public enum TicketStatus {

    /** Henvendelsen er registrert og venter på å bli hentet av en supportagent. */
    PENDING,

    /** Henvendelsen er hentet og tildelt én spesifikk agent. */
    ASSIGNED,

    /** Agenten har løst problemet og markert henvendelsen som fullført. Sluttilstand. */
    COMPLETED,

    /** Registratoren har kansellert henvendelsen. Sluttilstand. */
    CANCELLED;

    /**
     * Sjekker om tilstanden er en sluttilstand – ingen videre overganger er mulige.
     *
     * @return true hvis tilstanden er COMPLETED eller CANCELLED
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }
}
