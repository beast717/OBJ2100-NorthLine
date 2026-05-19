package client;

import protocol.Operation;
import protocol.RequestMessage;
import protocol.ResponseMessage;
import protocol.ResponseStatus;
import server.Ticket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;

/**
 * Klientapplikasjon for supportagenter i NorthLine-systemet.
 *
 * <p>Agenten kobler til serveren via TCP og kan hente den neste ledige
 * henvendelsen (FETCH) og markere den som fullfort (COMPLETE).
 * Agenten kan kun ha én aktiv ticket om gangen.</p>
 *
 * @author Mokhamed-Ali
 */
public class AgentClient {

    /** Serverens vertsnavn. */
    private static final String HOST = "localhost";

    /** Portnummeret serveren lytter pa. */
    private static final int PORT = 5000;

    /** Unikt ID for denne agenten, sendt med hver forespørsel. */
    private final String agentId;

    /** Den ticketen agenten jobber med akkurat na, eller {@code null} hvis ingen. */
    private Ticket currentTicket;

    /**
     * Oppretter en ny AgentClient med det angitte agent-ID-et.
     *
     * @param agentId unikt ID som identifiserer agenten overfor serveren
     */
    public AgentClient(String agentId) {
        this.agentId = agentId;
    }

    /**
     * Starter den interaktive menyloopen for agenten.
     *
     * <p>Kjorer til agenten velger å avslutte (valg 3).
     * Viser aktiv ticket i menyen dersom agenten har en.</p>
     */
    public void start() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("=== NorthLine Supportagent ===");
            System.out.println("Innlogget som: " + agentId);

            while (true) {
                System.out.println("\n--- MENY ---");
                if (currentTicket != null) {
                    System.out.println("Aktiv ticket: [" + currentTicket.getId() + "] "
                            + currentTicket.getDescription());
                }
                System.out.println("[1] Hent neste henvendelse");
                System.out.println("[2] Fullfor henvendelse");
                System.out.println("[3] Avslutt");
                System.out.print("Velg: ");

                String valg = scanner.nextLine().trim();

                switch (valg) {
                    case "1" -> hentHenvendelse();
                    case "2" -> fullforHenvendelse();
                    case "3" -> {
                        System.out.println("Logger av. Ha en god dag!");
                        return;
                    }
                    default -> System.out.println("Ugyldig valg – prov igjen.");
                }
            }
        }
    }

    /**
     * Sender en FETCH-forespørsel til serveren for a hente den eldste PENDING-ticketen.
     *
     * <p>Feiler stille dersom agenten allerede har en aktiv ticket.
     * Setter {@link #currentTicket} ved vellykket tildeling.</p>
     */
    private void hentHenvendelse() {
        if (currentTicket != null) {
            System.out.println("Du har allerede en aktiv ticket: [" + currentTicket.getId() + "]. "
                    + "Fullfor den for du henter en ny.");
            return;
        }

        ResponseMessage svar = sendRequest(new RequestMessage(Operation.FETCH, agentId, null, null));
        if (svar == null) return;

        if (svar.getStatus() == ResponseStatus.OK) {
            currentTicket = svar.getTicket();
            System.out.println("Ticket hentet!");
            skrivUtTicket(currentTicket);
        } else if (svar.getStatus() == ResponseStatus.ERROR_NO_TICKETS) {
            System.out.println("Ingen ledige henvendelser akkurat na. Prov igjen senere.");
        } else {
            System.out.println("Uventet feil: " + svar.getMessage());
        }
    }

    /**
     * Sender en COMPLETE-forespørsel for den aktive ticketen.
     *
     * <p>Serveren verifiserer at agenten eier ticketen for den markeres som fullfort.
     * Nullstiller {@link #currentTicket} ved suksess slik at agenten kan hente en ny.</p>
     */
    private void fullforHenvendelse() {
        if (currentTicket == null) {
            System.out.println("Ingen aktiv ticket. Hent en henvendelse forst.");
            return;
        }

        ResponseMessage svar = sendRequest(
                new RequestMessage(Operation.COMPLETE, agentId, currentTicket.getId(), null));
        if (svar == null) return;

        if (svar.getStatus() == ResponseStatus.OK) {
            System.out.println("Ticket [" + currentTicket.getId() + "] er markert som fullfort.");
            currentTicket = null;
        } else {
            System.out.println("Feil: " + svar.getMessage());
        }
    }

    /**
     * Skriver ut detaljene til en ticket pa konsollen.
     *
     * @param ticket ticketen som skal vises
     */
    private void skrivUtTicket(Ticket ticket) {
        System.out.println("----------------------------------");
        System.out.println("ID:          " + ticket.getId());
        System.out.println("Beskrivelse: " + ticket.getDescription());
        System.out.println("Status:      " + ticket.getStatus());
        System.out.println("Opprettet:   " + ticket.getCreatedAt());
        System.out.println("----------------------------------");
    }

    /**
     * Apner en ny TCP-tilkobling, sender forespørselen og returnerer svaret fra serveren.
     *
     * <p>Oppretter en ny tilkobling per kall — serveren er tilstandslos mellom kall.
     * Returnerer {@code null} dersom kommunikasjonen mislykkes.</p>
     *
     * @param forespørsel meldingen som skal sendes til serveren
     * @return serverens svar, eller {@code null} ved feil
     */
    private ResponseMessage sendRequest(RequestMessage forespørsel) {
        try (Socket socket = new Socket(HOST, PORT);
             ObjectOutputStream ut = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inn = new ObjectInputStream(socket.getInputStream())) {

            ut.writeObject(forespørsel);
            ut.flush();
            return (ResponseMessage) inn.readObject();

        } catch (ConnectException e) {
            System.out.println("Kunne ikke koble til server pa " + HOST + ":" + PORT
                    + ". Er serveren startet?");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Kommunikasjonsfeil: " + e.getMessage());
        }
        return null;
    }

    /**
     * Starter AgentClient fra kommandolinjen.
     *
     * @param args valgfritt: args[0] brukes som agentId (standard: "agent-1")
     */
    public static void main(String[] args) {
        String agentId = (args.length > 0) ? args[0] : "agent-1";
        new AgentClient(agentId).start();
    }
}
