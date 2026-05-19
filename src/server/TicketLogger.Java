package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Trådsikker logger for ticket-hendelser.
 * Skriver hendelser til logs/server.log.
 *
 * @author Abdirahman
 */
public class TicketLogger {

    // Mappe og fil der loggoppføringer lagres
    private static final String LOG_DIRECTORY = "logs";
    private static final String LOG_FILE = LOG_DIRECTORY + "/server.log";

    // Privat konstruktør – klassen skal kun brukes via statiske metoder
    private TicketLogger() {}

    /**
     * Logger at en ny ticket ble opprettet av en registrator.
     *
     * @param ticketId    ID til den opprettede ticketen
     * @param actorId     ID til registratoren som opprettet ticketen
     * @param description beskrivelse av problemet som ble registrert
     */
    public static void ticketCreated(String ticketId, String actorId, String description) {
        log("TICKET_CREATED", ticketId, actorId, description);
    }

    /**
     * Logger at en ticket ble hentet og tildelt en supportagent.
     *
     * @param ticketId ID til den tildelte ticketen
     * @param actorId  ID til agenten som hentet ticketen
     */
    public static void ticketAssigned(String ticketId, String actorId) {
        log("TICKET_ASSIGNED", ticketId, actorId, "");
    }

    /**
     * Logger at en ticket ble kansellert av en registrator.
     *
     * @param ticketId ID til den kansellerte ticketen
     * @param actorId  ID til registratoren som kansellerte ticketen
     */
    public static void ticketCancelled(String ticketId, String actorId) {
        log("TICKET_CANCELLED", ticketId, actorId, "");
    }

    /**
     * Logger at en ticket ble fullført av en supportagent.
     *
     * @param ticketId ID til den fullførte ticketen
     * @param actorId  ID til agenten som fullførte ticketen
     */
    public static void ticketCompleted(String ticketId, String actorId) {
        log("TICKET_COMPLETED", ticketId, actorId, "");
    }

    /**
     * Skriver en loggoppføring til filen. Synchronized for å forhindre
     * at flere tråder skriver samtidig og fletter logglinjer.
     */
    private static synchronized void log(
            String eventType,
            String ticketId,
            String actorId,
            String extraInfo
    ) {
        try {
            // Opprett logs-mappen hvis den ikke finnes
            File directory = new File(LOG_DIRECTORY);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            // Åpne filen i append-modus så eksisterende logg ikke overskrives
            try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
                writer.write(formatMessage(eventType, ticketId, actorId, extraInfo));
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            System.err.println("Kunne ikke skrive til logg: " + e.getMessage());
        }
    }

    /**
     * Formaterer en loggoppføring med tidsstempel og alle relevante felt.
     *
     * @return formatert loggstreng
     */
    private static String formatMessage(
            String eventType,
            String ticketId,
            String actorId,
            String extraInfo
    ) {
        return LocalDateTime.now() + " | " +
               "event=" + eventType + " | " +
               "ticketId=" + ticketId + " | " +
               "actor=" + actorId + " | " +
               "info=" + extraInfo;
    }
}
