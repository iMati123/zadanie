import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 12345;
    private static final int TIMEOUT_MS = 30000;
    private static final int MAX_CLIENTS = 250;

    private static List<Question> questions = new ArrayList<>();
    private static final String ANSWERS_FILE = "bazaOdpowiedzi.txt";
    private static final String RESULTS_FILE = "wyniki.txt";

    public static void main(String[] args) throws IOException {
        loadQuestions("bazaPytan.txt");
        ExecutorService pool = Executors.newFixedThreadPool(MAX_CLIENTS);
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server running on port " + PORT);

        while (true) {
            Socket client = serverSocket.accept();
            pool.execute(() -> handleClient(client));
        }
    }

    private static void handleClient(Socket socket) {
        try (
                socket;
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
        ) {
            int score = 0;
            List<String> answers = new ArrayList<>();

            for (Question q : questions) {
                out.write(q.toProtocolString());
                out.newLine();
                out.flush();

                socket.setSoTimeout(TIMEOUT_MS);
                String response;
                try {
                    response = in.readLine();
                } catch (SocketTimeoutException e) {
                    response = "TIMEOUT";
                }

                answers.add(q.question + " => " + response);
                if (response != null && response.equalsIgnoreCase(q.correct)) {
                    score++;
                }
            }

            out.write("Wynik: " + score + "/" + questions.size());
            out.newLine();
            out.flush();

            synchronized (Server.class) {
                try (BufferedWriter ansOut = new BufferedWriter(new FileWriter(ANSWERS_FILE, true))) {
                    for (String a : answers) {
                        ansOut.write(a);
                        ansOut.newLine();
                    }
                }
                try (BufferedWriter resultOut = new BufferedWriter(new FileWriter(RESULTS_FILE, true))) {
                    resultOut.write("Student: wynik = " + score);
                    resultOut.newLine();
                }
            }

        } catch (IOException e) {
            System.err.println("Błąd klienta: " + e.getMessage());
        }
    }

    private static void loadQuestions(String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                questions.add(Question.fromLine(line));
            }
        }
    }

    static class Question {
        String question, a, b, c, d, correct;

        static Question fromLine(String line) {
            String[] parts = line.split(";");
            return new Question(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
        }

        Question(String q, String a, String b, String c, String d, String correct) {
            this.question = q; this.a = a; this.b = b; this.c = c; this.d = d; this.correct = correct;
        }

        String toProtocolString() {
            return String.join(";", question, a, b, c, d);
        }
    }
}
