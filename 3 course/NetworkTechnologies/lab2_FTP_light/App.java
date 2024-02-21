package ftp;

public class App {
    public static void main(String[] args) {
        if (args[0].equals("recive")) {
            Server server = new Server();
            server.StartWorking();
        }
        else {
            Client client = new Client(args[0]);
            client.StartWorking();
        }
    }
}
