package server;

import protocol.RequestMessage;
import protocol.ResponseMessage;
import protocol.ResponseStatus;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;


public class ClientHandler implements Runnable {


    private final Socket clientSocket;
    private final TicketService ticService;
    private final Logger log;

    public ClientHandler(Socket clientSocket, TicketService ticService, Logger log) {
        this.clientSocket = clientSocket;
        this.ticService = ticService;
        this.log = log;
    }
  
    // Hovedløkken for å håndtere klientforespørsler
    @Override
    public void run() {
        
        // Logg tilkobling
        String clientAddress = clientSocket.getRemoteSocketAddress().toString();
        log.log("CLIENT_CONNECTED", null, clientAddress, null);
     
        // Bruk try-with-resources for å sikre at strømmer og socket lukkes riktig
        try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

            while (true) {

                Object obj = in.readObject();

                if (obj instanceof RequestMessage) {
                    
                    // Håndter forespørselen og send svar
                    RequestMessage req = (RequestMessage) obj;
                    ResponseMessage res = ticService.handleRequest(req);
                    out.writeObject(res);
                    out.flush();

                } else {
                    
                    // Mottok en melding av ukjent type – logg og send feilmelding
                    ResponseMessage error = new ResponseMessage(
                        ResponseStatus.ERROR_UNKNOWN_OPERATION,
                        "Ukjent meldingstype",
                        null
                    );

                    out.writeObject(error);
                    out.flush();
                }

            }
        
            // Når klienten kobler fra, vil readObject() kaste EOFException, og vi kan håndtere det i catch-blokken
        } catch (EOFException e) {

            // Klienten har koblet fra – logg og avslutt tråden
        } catch (IOException | ClassNotFoundException e) {

            System.err.println("Feil i ClientHandler: " + e.getMessage());
       
            // Uansett hvordan løkken avsluttes, logg at klienten har koblet fra og lukk socketen
        } finally {

            log.log("CLIENT_DISCONNECTED", null, clientAddress, null);

            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Kunne ikke lukke socket: " + e.getMessage());
            }
        }
    }
}
