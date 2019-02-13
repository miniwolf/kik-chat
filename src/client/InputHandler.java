package client;

import com.google.gson.Gson;
import demo.data.messaging.*;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static demo.data.messaging.MessagesKt.readReceiptFromTextMessage;

public class InputHandler implements Runnable {
    private Gson gson;

    private Boolean running = true;
    private Connection inReader;
    private List<String> requestLines = new ArrayList<>();

    public InputHandler(Socket socket) {
        inReader = new Connection(socket, 8192);
        gson = new Gson();
    }

    @Override
    public void run() {
        while (running) {
            try {
                getResponse(inReader.readLine(false));
                Thread.sleep(100);
            } catch (IOException | InterruptedException | Lines.KeyNotFoundException e) {
                e.printStackTrace();
            } catch (Connection.EndOfStreamException oes) {
                running = false;
            }
        }
    }

    public void getResponse(String line) throws IOException, Connection.EndOfStreamException, Lines.KeyNotFoundException {
        System.out.println(line);
        requestLines.add(line);

        if (line.length() == 0) {
            inReader.Request = new Lines(requestLines.toArray(new String[0]));
            requestLines.clear();
            var body = inReader.ReadRequestBody();
            System.out.println(body);
            System.out.println();

            Messages messages = gson.fromJson(body, Messages.class);

            switch (messages.getMessages()[0].getType()) {
                case "text":
                    personWrote(gson.fromJson(body, InTextMessageMessage.class).getMessages()[0]);
                    break;
                case "is-typing":
                    personIsTyping(gson.fromJson(body, InReadReceiptMessage.class).getMessages()[0]);
                    break;
                default:
                    System.err.println(messages.getMessages()[0].getType() + ": is not supported yet");
            }
        }
    }

    public void personWrote(InTextMessage inMessage) throws IOException {
        System.out.println(inMessage.getFrom() + ": " + inMessage.getBody());
        var receipt = readReceiptFromTextMessage(inMessage);
        inReader.Write(gson.toJson(receipt));
    }

    public void personIsTyping(InReadReceipt receipt) {
        System.out.println(receipt.getFrom() + " has " + (receipt.isTyping() ? "started" : "stopped") + " typing");
    }

    public void stop() {
        running = false;
    }
}
