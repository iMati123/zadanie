import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String SERVER = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (
                Socket socket = new Socket(SERVER, PORT);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                Scanner scanner = new Scanner(System.in)
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("Wynik:")) {
                    System.out.println(line);
                    break;
                }

                String[] parts = line.split(";");
                System.out.println("Pytanie: " + parts[0]);
                System.out.println("A) " + parts[1]);
                System.out.println("B) " + parts[2]);
                System.out.println("C) " + parts[3]);
                System.out.println("D) " + parts[4]);
                System.out.print("Twoja odpowiedź (A/B/C/D): ");
                String answer = scanner.nextLine().toUpperCase();
                out.write(answer);
                out.newLine();
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("Błąd klienta: " + e.getMessage());
        }
    }
}
