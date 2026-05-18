package server;

import protocol.TicketStatus;
import java.io.Serializable;
import java.time.LocalDateTime;

public class Ticket implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String description;
    private final LocalDateTime createdAt;

    private TicketStatus status;
    private String assignedAgentId;
    private LocalDateTime updatedAt;

    public Ticket(String id, String description) {
        this.id = id;
        this.description = description;
        this.status = TicketStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    // Gyldige tilstandsoverganger - kalles fra TicketStore under synchronized-blokk

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

    public void cancel() {
        if (status != TicketStatus.PENDING) {
            throw new IllegalStateException(
                "Kan ikke kansellere ticket " + id + " – tilstand er " + status
            );
        }
        this.status = TicketStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public String getId()              { return id; }
    public String getDescription()     { return description; }
    public TicketStatus getStatus()    { return status; }
    public String getAssignedAgentId() { return assignedAgentId; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public LocalDateTime getUpdatedAt(){ return updatedAt; }

    @Override
    public String toString() {
        return "Ticket[" + id + ", " + status +
               (assignedAgentId != null ? ", agent=" + assignedAgentId : "") + "]";
    }
}
