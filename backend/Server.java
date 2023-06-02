import java.io.IOException;

/* An interface for the Server */
public interface Server {
    /* Separate Sockets for the Workers and the Clients */
    void openServer() throws IOException;
}
