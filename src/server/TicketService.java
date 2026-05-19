// author: Guleed
package server;

import protocol.RequestMessage;
import protocol.ResponseMessage;
import protocol.ResponseStatus;
import protocol.Ticket;
import java.util.Optional;


public class TicketService {

    private final TicketStore ticStore;

    // KONSTRUKTØR
    public TicketService(TicketStore ticStore) {
        this.ticStore = ticStore;
    }

    // Hovedmetoden — ruter forespørselen til riktig hjelpemetode
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

  
        // Registrer Ticket, skriver i log og returnerer som registrert. 
        private ResponseMessage handleRegister(RequestMessage req) {
            Ticket ticket = ticStore.createTicket(req.getDescription());
            TicketLogger.ticketCreated(ticket.getId(), req.getSenderId(), ticket.getDescription());
            return new ResponseMessage(ResponseStatus.OK, "Ticket registrert", ticket);
        }

       // Hent neste ledige ticket, skriver i log og returnerer den. Håndterer også feilsituasjonen der ingen tickets er ledige
        private ResponseMessage handleFetch(RequestMessage req) {
            Optional<Ticket> result = ticStore.claimNext(req.getSenderId());
            
            if ( result.isEmpty()) {
                return new ResponseMessage(ResponseStatus.ERROR_NO_TICKETS, "Ingen ledige henvendelser", null);
            } else  {

                Ticket ticket = result.get();
                TicketLogger.ticketAssigned(ticket.getId(), req.getSenderId());
                return new ResponseMessage(ResponseStatus.OK, "Henvendelse tildelt", ticket);

            }
            
        }

       // Kanseller en ticket, skriver i log og returnerer OK. Håndterer også feilsituasjoner
        private ResponseMessage handleCancel(RequestMessage req) {
            boolean success =  ticStore.cancel(req.getTicketId());

            if (success) {
                TicketLogger.ticketCancelled(req.getTicketId(), req.getSenderId());
                return new ResponseMessage(ResponseStatus.OK, "Ticket kansellert", null);
            } else {
            
                // Sjekk hvorfor det feilet
                if (ticStore.findById(req.getTicketId()).isEmpty()) {
                    return new ResponseMessage(ResponseStatus.ERROR_NOT_FOUND, "Ticket finnes ikke", null);
                } else {
                    return new ResponseMessage(ResponseStatus.ERROR_INVALID_STATE, "Kan ikke kansellere — ticket er ikke PENDING", null);
                }
            }
        }

       // Fullfør en ticket, skriver i log og returnerer OK. Håndterer også feilsituasjoner
        private ResponseMessage handleComplete(RequestMessage req) {
            boolean success = ticStore.complete(req.getTicketId(), req.getSenderId());

            if (success) {
                TicketLogger.ticketCompleted(req.getTicketId(), req.getSenderId());
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












    

