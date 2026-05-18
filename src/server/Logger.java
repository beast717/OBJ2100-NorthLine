package server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;


public class Logger {

    private static Logger instance; 
    private final BufferedWriter writer; 
    
    // Logger er en singleton, så konstruktøren er private
    private Logger() throws IOException {
        this.writer = new BufferedWriter(new FileWriter("server.log", true));
    }
    
    // Synchronized for å sikre trådsikker tilgang til loggeren
    public static synchronized Logger getInstance() throws IOException {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

      public synchronized void log(String event, String ticketId, String actorId, String details) {
        try {
            String line = Instant.now() + "\t" + event + "\t"
                        + (ticketId != null ? ticketId : "-") + "\t"
                        + (actorId != null ? actorId : "-") + "\t"
                        + (details != null ? details : "-");
            writer.write(line);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("Logging feilet: " + e.getMessage());
        }
    }

    public synchronized void close() {
        try {
            writer.close();
        } catch (IOException e) {
            System.err.println("Kunne ikke lukke logg: " + e.getMessage());
        }
    }

    
}
