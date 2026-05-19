package server;

import protocol.Ticket;
import protocol.TicketStatus;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;


public class TicketStore {

    private final Map<String, Ticket> henvendelser = new LinkedHashMap<>();
    private int nextId = 1;

     
    // Alle metoder som endrer tilstand på tickets er synchronized for å sikre trådsikkerhet
    public synchronized Ticket createTicket(String description) {
            String id = "TICKET-" + (nextId++);
            Ticket ticket = new Ticket(id, description);
            henvendelser.put(id, ticket);
            return ticket;
    }
    
    // Hjelpemetode for å legge til en ticket direkte, brukes i tester
    public synchronized void add(Ticket ticket) {
        henvendelser.put(ticket.getId(), ticket);
    }
   
    // Hjelpemetode for å tømme alle tickets, brukes i tester
    public synchronized Optional<Ticket> findById(String id) {
        return Optional.ofNullable(henvendelser.get(id));
    }
   
    // Hjelpemetode for å tømme alle tickets, brukes i tester
    public synchronized Optional<Ticket> claimNext(String agentId) {
        for (Ticket ticket : henvendelser.values()) {
            if (ticket.getStatus() == TicketStatus.PENDING) {
                ticket.assign(agentId);
                return Optional.of(ticket);
            }
        }
        return Optional.empty();
    }
   
    // Hjelpemetode for å tømme alle tickets, brukes i tester
    public synchronized boolean cancel(String id) {
        Optional<Ticket> found = findById(id);
        if (found.isEmpty()) {
            return false;
        }
        try {
            found.get().cancel();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    // Hjelpemetode for å tømme alle tickets, brukes i tester
    public synchronized boolean complete(String id, String agentId) {
        Optional<Ticket> found = findById(id);
        if (found.isEmpty()) {
            return false;
        }
        try {
            found.get().complete(agentId);
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }
}
