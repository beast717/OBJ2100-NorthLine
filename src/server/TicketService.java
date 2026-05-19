package server;

import protocol.RequestMessage;
import protocol.ResponseMessage;
import protocol.ResponseStatus;
import protocol.Ticket;
import java.util.Optional;

/**
 * Applikasjonslogikken som behandler innkommende forespørsler fra klienter.
 * <p>
 * Tar imot en {@link RequestMessage}, utfører riktig operasjon mot
 * {@link TicketStore}, logger handlingen via {@link TicketLogger}, og
 * returnerer en {@link ResponseMessage} med resultatet.
 *
 * @author Guleed
 */
public class TicketService {

    private final TicketStore ticStore;

    /**
     * Oppretter en ny TicketService som opererer mot det angitte lageret.
     *
     * @param ticStore lageret som tickets leses fra og skrives til
     */
    public TicketService(TicketStore ticStore) {
        this.ticStore = ticStore;
    }

    /**
     * Hovedmetoden — ruter forespørselen til riktig hjelpemetode basert
     * på operasjonen i forespørselen.
     *
     * @param req forespørselen fra klienten
     * @return svaret som skal sendes tilbake til klienten
     */
    public ResponseMessage handleRequest(RequestMessage req) {
        switch (req.getOperation()) {
            case REGISTER:
                return handleRegister(req);
            case FETCH:
                return handleFetch(req);
            case CANCEL:
                return handleCancel(req);
            case COMPLETE:
                return handleComplete(req);
            default:
                return new ResponseMessage(ResponseStatus.ERROR_UNKNOWN_OPERATION, "Ukjent operasjon", null);
        }
    }

    /**
     * Registrerer en ny ticket basert på beskrivelsen i forespørselen
     * og logger opprettelsen.
     *
     * @param req forespørsel av typen REGISTER
     * @return OK-svar med den nye ticketen
     */
    private ResponseMessage handleRegister(RequestMessage req) {
        Ticket ticket = ticStore.createTicket(req.getDescription());
        TicketLogger.ticketCreated(ticket.getId(), req.getSenderId(), ticket.getDescription());
        return new ResponseMessage(ResponseStatus.OK, "Ticket registrert", ticket);
    }

    /**
     * Henter neste ledige ticket og tildeler den til avsenderen.
     *
     * @param req forespørsel av typen FETCH
     * @return OK-svar med tildelt ticket, eller {@code ERROR_NO_TICKETS}
     *         hvis det ikke finnes ledige henvendelser
     */
    private ResponseMessage handleFetch(RequestMessage req) {
        Optional<Ticket> result = ticStore.claimNext(req.getSenderId());

        if (result.isEmpty()) {
            return new ResponseMessage(ResponseStatus.ERROR_NO_TICKETS, "Ingen ledige henvendelser", null);
        } else {
            Ticket ticket = result.get();
            TicketLogger.ticketAssigned(ticket.getId(), req.getSenderId(), ticket.getDescription());
            return new ResponseMessage(ResponseStatus.OK, "Henvendelse tildelt", ticket);
        }
    }

    /**
     * Kansellerer en eksisterende ticket. Skiller mellom "finnes ikke"
     * og "feil tilstand" i feilsvaret.
     *
     * @param req forespørsel av typen CANCEL
     * @return OK ved suksess, ellers {@code ERROR_NOT_FOUND} eller
     *         {@code ERROR_INVALID_STATE}
     */
    private ResponseMessage handleCancel(RequestMessage req) {
        boolean success = ticStore.cancel(req.getTicketId());

        if (success) {
            String description = ticStore.findById(req.getTicketId())
                    .map(Ticket::getDescription).orElse("");
            TicketLogger.ticketCancelled(req.getTicketId(), req.getSenderId(), description);
            return new ResponseMessage(ResponseStatus.OK, "Ticket kansellert", null);
        } else {
            if (ticStore.findById(req.getTicketId()).isEmpty()) {
                return new ResponseMessage(ResponseStatus.ERROR_NOT_FOUND, "Ticket finnes ikke", null);
            } else {
                return new ResponseMessage(ResponseStatus.ERROR_INVALID_STATE, "Kan ikke kansellere — ticket er ikke PENDING", null);
            }
        }
    }

    /**
     * Markerer en ticket som fullført. Kun agenten som har ticketen
     * tildelt kan fullføre den.
     *
     * @param req forespørsel av typen COMPLETE
     * @return OK ved suksess, ellers {@code ERROR_NOT_FOUND} eller
     *         {@code ERROR_INVALID_STATE}
     */
    private ResponseMessage handleComplete(RequestMessage req) {
        boolean success = ticStore.complete(req.getTicketId(), req.getSenderId());

        if (success) {
            String description = ticStore.findById(req.getTicketId())
                    .map(Ticket::getDescription).orElse("");
            TicketLogger.ticketCompleted(req.getTicketId(), req.getSenderId(), description);
            return new ResponseMessage(ResponseStatus.OK, "Ticket fullført", null);
        } else {
            if (ticStore.findById(req.getTicketId()).isEmpty()) {
                return new ResponseMessage(ResponseStatus.ERROR_NOT_FOUND, "Ticket finnes ikke", null);
            } else {
                return new ResponseMessage(ResponseStatus.ERROR_INVALID_STATE, "Kan ikke fullføre — feil status eller feil agent", null);
            }
        }
    }
}
