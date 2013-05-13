

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class EchoClient {

  private static final int PORT = 1234;

  public static void main(String[] args) throws IOException {
    final Socket client = new Socket("localhost", PORT);
    System.out.println("Client connected to port: " + PORT);

    final PrintWriter writer = new PrintWriter(client.getOutputStream(), true);
    final BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
    final BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    
    String echo, userInput;

    while ((userInput = input.readLine()) != null) {
      writer.println(userInput);
      writer.flush();
      echo = reader.readLine();
      if ("end".equals(userInput)) {
        break;
      }
      System.out.println(echo);
    }
    System.out.println("Client shutting down.");
    writer.close();
    input.close();
    reader.close();
    client.close();
  }

}