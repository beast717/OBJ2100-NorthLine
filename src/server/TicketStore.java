package server;

import protocol.Ticket;
import protocol.TicketStatus;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Trådsikkert lager for alle henvendelser (tickets) på serveren.
 * <p>
 * Alle metoder som endrer eller leser tilstand er {@code synchronized}
 * slik at flere {@link ClientHandler}-tråder kan operere samtidig uten
 * å skape inkonsistente data.
 *
 * @author Guleed
 */
public class TicketStore {

    private final Map<String, Ticket> henvendelser = new LinkedHashMap<>();
    private int nextId = 1;

    /**
     * Oppretter en ny ticket med en unik ID og status {@code PENDING}.
     *
     * @param description fritekstbeskrivelse av henvendelsen fra klienten
     * @return den nyopprettede ticketen
     */
    public synchronized Ticket createTicket(String description) {
            String id = "TICKET-" + (nextId++);
            Ticket ticket = new Ticket(id, description);
            henvendelser.put(id, ticket);
            return ticket;
    }

    /**
     * Legger til en eksisterende ticket direkte i lageret.
     * Hovedsakelig brukt fra tester for å sette opp kjent tilstand.
     *
     * @param ticket ticketen som skal legges til
     */
    public synchronized void add(Ticket ticket) {
        henvendelser.put(ticket.getId(), ticket);
    }

    /**
     * Slår opp en ticket basert på ID.
     *
     * @param id ticket-ID å lete etter
     * @return {@link Optional} med ticketen hvis funnet, ellers tom
     */
    public synchronized Optional<Ticket> findById(String id) {
        return Optional.ofNullable(henvendelser.get(id));
    }

    /**
     * Finner den første ledige ticketen (status {@code PENDING}) og
     * tildeler den til den angitte agenten.
     *
     * @param agentId ID-en til agenten som skal få ticketen
     * @return {@link Optional} med den tildelte ticketen, eller tom hvis
     *         det ikke finnes noen ledige
     */
    public synchronized Optional<Ticket> claimNext(String agentId) {
        for (Ticket ticket : henvendelser.values()) {
            if (ticket.getStatus() == TicketStatus.PENDING) {
                ticket.assign(agentId);
                return Optional.of(ticket);
            }
        }
        return Optional.empty();
    }

    /**
     * Kansellerer en ticket dersom den finnes og er i en kansellerbar tilstand.
     *
     * @param id ID-en til ticketen som skal kanselleres
     * @return {@code true} hvis kanselleringen lyktes, ellers {@code false}
     *         (ticket finnes ikke eller har feil status)
     */
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

    /**
     * Markerer en ticket som fullført. Kun agenten som har ticketen
     * tildelt kan fullføre den.
     *
     * @param id      ID-en til ticketen
     * @param agentId ID-en til agenten som forsøker å fullføre
     * @return {@code true} hvis fullføringen lyktes, ellers {@code false}
     *         (ticket finnes ikke, feil status, eller feil agent)
     */
    
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
