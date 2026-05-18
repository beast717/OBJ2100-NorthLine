package protocol;

import java.io.Serializable;

/**
 * Representerer et svar sendt fra server til klient.
 * Feltet {@code ticket} er kun satt ved REGISTER og FETCH, null ellers.
 *
 * @author Ahmed
 */
public class ResponseMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private ResponseStatus status;
    private String message;
    private Ticket ticket;

    /**
     * Oppretter et nytt svar.
     *
     * @param status  resultatkoden for operasjonen
     * @param message lesbar forklaring av resultatet
     * @param ticket  ticket-objektet, eller null hvis ikke relevant
     */
    public ResponseMessage(ResponseStatus status, String message, Ticket ticket) {
        this.status = status;
        this.message = message;
        this.ticket = ticket;
    }

    /** @return resultatkoden for operasjonen */
    public ResponseStatus getStatus() {
        return status;
    }

    /** @return lesbar forklaring av resultatet */
    public String getMessage() {
        return message;
    }

    /** @return ticket-objektet, eller null hvis ikke relevant */
    public Ticket getTicket() {
        return ticket;
    }
}