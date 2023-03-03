
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class Main {

    public static void main(String[] args) throws IOException {
        BooleanSearchEngine engine = new BooleanSearchEngine(new File("pdfs"));
        var gson = new GsonBuilder().setPrettyPrinting().create();
        try (ServerSocket serverSocket = new ServerSocket(8989)) {
            System.out.println("Старт сервера на порту " + 8989 + "...");

            while (true) {
                try (
                        Socket socket = serverSocket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter out = new PrintWriter(socket.getOutputStream())
                ) {
                    //Принимаем запрос
                    var request = in.readLine();

                    // Запрашиваем результат и отправляем его клиенту
                    var response = gson.toJson(engine.search(request));
                    out.println(response);
                }
            }
        } catch (IOException e) {
            System.out.println("Не могу стартовать сервер");
            e.printStackTrace();
        }
    }
}