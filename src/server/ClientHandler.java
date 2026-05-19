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

    @Override
    public void run() {

        String clientAddress = clientSocket.getRemoteSocketAddress().toString();
        log.log("CLIENT_CONNECTED", null, clientAddress, null);

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
            // Klient koblet fra normalt
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Feil i ClientHandler: " + e.getMessage());
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
