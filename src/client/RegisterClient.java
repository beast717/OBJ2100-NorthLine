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
 * Klientapplikasjon for registratorer i NorthLine-systemet.
 *
 * <p>Registratoren kan opprette nye henvendelser (REGISTER) og kansellere
 * eksisterende PENDING-tickets (CANCEL). Ulikt agenten kan registratoren
 * ha flere aktive tickets samtidig — ingen en-om-gangen-begrensning.</p>
 *
 * @author Noxche
 */
public class RegisterClient {

    /** Serverens vertsnavn. */
    private static final String HOST = "localhost";

    /** Portnummeret serveren lytter pa. */
    private static final int PORT = 5000;

    /** Unikt ID for denne registratoren, sendt med hver forespørsel. */
    private final String registratorId;

    /**
     * Oppretter en ny RegisterClient med det angitte registrator-ID-et.
     *
     * @param registratorId unikt ID som identifiserer registratoren overfor serveren
     */
    public RegisterClient(String registratorId) {
        this.registratorId = registratorId;
    }

    /**
     * Starter den interaktive menyloopen for registratoren.
     *
     * <p>Kjorer til registratoren velger a avslutte (valg 3).</p>
     */
    public void start() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("=== NorthLine Registrator ===");
            System.out.println("Innlogget som: " + registratorId);

            while (true) {
                System.out.println("\n--- MENY ---");
                System.out.println("[1] Registrer ny henvendelse");
                System.out.println("[2] Kanseller henvendelse");
                System.out.println("[3] Avslutt");
                System.out.print("Velg: ");

                String valg = scanner.nextLine().trim();

                switch (valg) {
                    case "1" -> registrerHenvendelse(scanner);
                    case "2" -> kansellerHenvendelse(scanner);
                    case "3" -> {
                        System.out.println("Logger av. Ha en god dag!");
                        return;
                    }
                    default -> System.out.println("Ugyldig valg - prov igjen.");
                }
            }
        }
    }

    /**
     * Ber brukeren beskrive problemet og sender en REGISTER-forespørsel til serveren.
     *
     * <p>Serveren oppretter en ny ticket med status AVAILABLE og returnerer
     * den med generert ID. Beskrivelsen kan ikke vaere tom.</p>
     *
     * @param scanner innleseren som brukes til a lese beskrivelsen fra konsollen
     */
    private void registrerHenvendelse(Scanner scanner) {
        System.out.print("Beskriv problemet: ");
        String beskrivelse = scanner.nextLine().trim();
        if (beskrivelse.isEmpty()) {
            System.out.println("Beskrivelse kan ikke vaere tom.");
            return;
        }

        ResponseMessage svar = sendRequest(
                new RequestMessage(Operation.REGISTER, registratorId, null, beskrivelse));
        if (svar == null) return;

        if (svar.getStatus() == ResponseStatus.OK) {
            Ticket ticket = svar.getTicket();
            System.out.println("Henvendelse registrert!");
            skrivUtTicket(ticket);
        } else {
            System.out.println("Uventet feil: " + svar.getMessage());
        }
    }

    /**
     * Ber brukeren oppgi ticket-ID og sender en CANCEL-forespørsel til serveren.
     *
     * <p>Kun PENDING-tickets kan kanselleres. Serveren returnerer {@code ERROR_INVALID_STATE}
     * dersom ticketen allerede er tildelt eller fullfort, og {@code ERROR_NOT_FOUND}
     * dersom ID-en ikke finnes.</p>
     *
     * @param scanner innleseren som brukes til a lese ticket-ID fra konsollen
     */
    private void kansellerHenvendelse(Scanner scanner) {
        System.out.print("Ticket-ID som skal kanselleres: ");
        String ticketId = scanner.nextLine().trim();
        if (ticketId.isEmpty()) {
            System.out.println("Ticket-ID kan ikke vaere tom.");
            return;
        }

        ResponseMessage svar = sendRequest(
                new RequestMessage(Operation.CANCEL, registratorId, ticketId, null));
        if (svar == null) return;

        if (svar.getStatus() == ResponseStatus.OK) {
            System.out.println("Ticket [" + ticketId + "] er kansellert.");
        } else if (svar.getStatus() == ResponseStatus.ERROR_NOT_FOUND) {
            System.out.println("Ingen ticket med ID [" + ticketId + "] ble funnet.");
        } else if (svar.getStatus() == ResponseStatus.ERROR_INVALID_STATE) {
            System.out.println("Ticket [" + ticketId + "] kan ikke kanselleres — den er allerede tildelt eller fullfort.");
        } else {
            System.out.println("Uventet feil: " + svar.getMessage());
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
     * Starter RegisterClient fra kommandolinjen.
     *
     * @param args valgfritt: args[0] brukes som registratorId (standard: "registrator-1")
     */
    public static void main(String[] args) {
        String id = (args.length > 0) ? args[0] : "registrator-1";
        new RegisterClient(id).start();
    }
}
