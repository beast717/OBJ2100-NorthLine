package protocol;

import java.io.Serializable;

/**
 * Representerer en forespørsel sendt fra klient til server.
 * Feltene {@code ticketId} og {@code description} kan være null
 * avhengig av hvilken operasjon som utføres.
 *
 * @author Ahmed
 */
public class RequestMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Operation operation;
    private String senderId;
    private String ticketId;
    private String description;

    /**
     * Oppretter en ny forespørsel.
     *
     * @param operation   operasjonen som skal utføres
     * @param senderId    ID til aktøren som sender forespørselen
     * @param ticketId    ID til henvendelsen (påkrevd for CANCEL og COMPLETE, null ellers)
     * @param description beskrivelse av problemet (påkrevd for REGISTER, null ellers)
     */
    public RequestMessage(Operation operation, String senderId, String ticketId, String description) {
        this.operation = operation;
        this.senderId = senderId;
        this.ticketId = ticketId;
        this.description = description;
    }

    /** @return operasjonen som skal utføres */
    public Operation getOperation() {
        return operation;
    }

    /** @return ID til aktøren som sendte forespørselen */
    public String getSenderId() {
        return senderId;
    }

    /** @return ID til henvendelsen, eller null hvis ikke relevant */
    public String getTicketId() {
        return ticketId;
    }

    /** @return beskrivelse av problemet, eller null hvis ikke relevant */
    public String getDescription() {
        return description;
    }
}