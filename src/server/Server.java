package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

/**
 * Startpunkt for serveren. Lytter på TCP-port 5000 og oppretter
 * en ny tråd per klient via ClientHandler.
 *
 * @author Ahmed
 */
public class Server {
    public static void main(String[] args) throws IOException {
        // Åpner TCP port og venter på klienter
        ServerSocket serverSocket = new ServerSocket(5000);
        // Evig løkke. Serveren skal aldri stoppe
        while (true) {
            // Venter på at en klient skal koble seg til, returnerer en Socket
            Socket clientSocket = serverSocket.accept();
            // Oppretter en ClientHandler for hver klient og starter en ny tråd for å håndtere kommunikasjonen
            ClientHandler handler = new ClientHandler(clientSocket);
            new Thread(handler).start();
        }
    }
}


