import java.io.*;
import java.net.*;
import java.util.*;

public class LanDocsServer {

    private static final Set<PrintWriter> clients =
            Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(9090)) {

            System.out.println("=================================");
            System.out.println("LAN Docs Server Running");
            System.out.println("Port : 9090");
            System.out.println("=================================");

            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler extends Thread {

        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {

            try {

                in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                out = new PrintWriter(
                        socket.getOutputStream(), true);

                clients.add(out);

                String msg;

                while ((msg = in.readLine()) != null) {

                    synchronized (clients) {
                        for (PrintWriter writer : clients) {
                            if (writer != out) {
                                writer.println(msg);
                            }
                        }
                    }
                }

            } catch (Exception e) {
                System.out.println("Client disconnected");
            } finally {

                clients.remove(out);

                try {
                    socket.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}