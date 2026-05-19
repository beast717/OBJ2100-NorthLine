package server;

import protocol.RequestMessage;
import protocol.ResponseMessage;
import protocol.ResponseStatus;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

/**
 * Kjører på en egen tråd og håndterer all kommunikasjon med én klient.
 * <p>
 * Leser {@link RequestMessage}-objekter fra socketens inn-strøm, sender
 * dem videre til {@link TicketService}, og skriver {@link ResponseMessage}
 * tilbake. Tråden lever til klienten kobler fra eller en I/O-feil oppstår.
 *
 * @author Guleed
 */
public class ClientHandler implements Runnable {


    private final Socket clientSocket;
    private final TicketService ticService;

    /**
     * Oppretter en handler for en nylig akseptert klient-tilkobling.
     *
     * @param clientSocket socketen koblet til klienten
     * @param ticService   tjenesten som skal behandle forespørslene
     */
    public ClientHandler(Socket clientSocket, TicketService ticService) {
        this.clientSocket = clientSocket;
        this.ticService = ticService;
    }

    /**
     * Hovedløkken: leser forespørsler fra klienten, delegerer til
     * {@link TicketService}, og sender svar tilbake. Avsluttes
     * når klienten kobler fra eller ved I/O-feil. Socketen lukkes
     * alltid i {@code finally}-blokken.
     */
    @Override
    public void run() {


        // Bruk try-with-resources for å sikre at strømmer og socket lukkes riktig
        try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

            while (true) {

                Object obj = in.readObject();

                if (obj instanceof RequestMessage) {

                    RequestMessage req = (RequestMessage) obj;
                    ResponseMessage res = ticService.handleRequest(req);
                    out.writeObject(res);
                    out.flush();

                } else {

                    ResponseMessage error = new ResponseMessage(
                        ResponseStatus.ERROR_UNKNOWN_OPERATION,
                        "Ukjent meldingstype",
                        null
                    );

                    out.writeObject(error);
                    out.flush();
                }

            }

        } catch (EOFException e) {
            // Klienten har koblet fra – avslutt tråden stille
        } catch (IOException | ClassNotFoundException e) {

            System.err.println("Feil i ClientHandler: " + e.getMessage());

        } finally {

            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Kunne ikke lukke socket: " + e.getMessage());
            }
        }
    }
}
