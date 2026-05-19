package protocol;

import java.io.Serializable;
import java.time.LocalDateTime;
/**
 * Representerer en støttehenvendelse og dens tilstand i systemet.
 * Tilstandsoverganger kalles fra TicketStore under synchronized-blokk.
 *
 * @author Ahmed
 */
public class Ticket implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String description;
    private final LocalDateTime createdAt;

    private TicketStatus status;
    private String assignedAgentId;
    private LocalDateTime updatedAt;

    /**
     * Oppretter en ny henvendelse med tilstand PENDING.
     *
     * @param id          unik identifikator for henvendelsen
     * @param description beskrivelse av problemet
     */
    public Ticket(String id, String description) {
        this.id = id;
        this.description = description;
        this.status = TicketStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * Tildeler henvendelsen til en agent. Krever tilstand PENDING.
     *
     * @param agentId ID til agenten som henter henvendelsen
     * @throws IllegalStateException hvis tilstanden ikke er PENDING
     */
    public void assign(String agentId) {
        if (status != TicketStatus.PENDING) {
            throw new IllegalStateException(
                "Kan ikke tildele ticket " + id + " – tilstand er " + status
            );
        }
        this.assignedAgentId = agentId;
        this.status = TicketStatus.ASSIGNED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Markerer henvendelsen som fullført. Krever tilstand ASSIGNED og riktig agent.
     *
     * @param agentId ID til agenten som fullfører henvendelsen
     * @throws IllegalStateException hvis tilstanden ikke er ASSIGNED eller feil agent
     */
    public void complete(String agentId) {
        if (status != TicketStatus.ASSIGNED) {
            throw new IllegalStateException(
                "Kan ikke fullføre ticket " + id + " – tilstand er " + status
            );
        }
        if (!agentId.equals(assignedAgentId)) {
            throw new IllegalStateException(
                "Agent " + agentId + " eier ikke ticket " + id
            );
        }
        this.status = TicketStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Kansellerer henvendelsen. Krever tilstand PENDING.
     *
     * @throws IllegalStateException hvis tilstanden ikke er PENDING
     */
    public void cancel() {
        if (status != TicketStatus.PENDING) {
            throw new IllegalStateException(
                "Kan ikke kansellere ticket " + id + " – tilstand er " + status
            );
        }
        this.status = TicketStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    /** @return unik identifikator for henvendelsen */
    public String getId()              { return id; }

    /** @return beskrivelse av problemet */
    public String getDescription()     { return description; }

    /** @return nåværende tilstand */
    public TicketStatus getStatus()    { return status; }

    /** @return ID til tildelt agent, eller null hvis ikke tildelt */
    public String getAssignedAgentId() { return assignedAgentId; }

    /** @return tidspunkt henvendelsen ble opprettet */
    public LocalDateTime getCreatedAt(){ return createdAt; }

    /** @return tidspunkt for siste tilstandsendring */
    public LocalDateTime getUpdatedAt(){ return updatedAt; }

    @Override
    public String toString() {
        return "Ticket[" + id + ", " + status +
               (assignedAgentId != null ? ", agent=" + assignedAgentId : "") + "]";
    }
}
