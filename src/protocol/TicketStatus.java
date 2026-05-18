package protocol;

public enum TicketStatus {
    PENDING,
    ASSIGNED,
    COMPLETED,
    CANCELLED;

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }
}
