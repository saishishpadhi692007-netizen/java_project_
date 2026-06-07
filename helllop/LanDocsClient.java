import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;

public class LanDocsClient {

    private JFrame frame;
    private JTextArea editorArea;
    private JTextArea chatArea;
    private JTextField chatInput;

    private BufferedReader in;
    private PrintWriter out;

    private boolean updating = false;

    public LanDocsClient() {

        frame = new JFrame("LAN Docs");

        editorArea = new JTextArea();
        chatArea = new JTextArea();
        chatInput = new JTextField();

        editorArea.setFont(new Font("Consolas", Font.PLAIN, 18));
        editorArea.setBackground(new Color(40,42,54));
        editorArea.setForeground(Color.WHITE);

        chatArea.setEditable(false);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(editorArea),
                new JScrollPane(chatArea));

        split.setDividerLocation(700);

        frame.add(split, BorderLayout.CENTER);
        frame.add(chatInput, BorderLayout.SOUTH);

        frame.setSize(1200,700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        editorArea.getDocument()
                .addDocumentListener(new DocumentListener() {

                    public void insertUpdate(DocumentEvent e) {
                        sendText();
                    }

                    public void removeUpdate(DocumentEvent e) {
                        sendText();
                    }

                    public void changedUpdate(DocumentEvent e) {
                        sendText();
                    }
                });

        chatInput.addActionListener(e -> {

            String msg = chatInput.getText().trim();

            if (!msg.isEmpty()) {

                out.println("CHAT:" + msg);

                chatArea.append("Me : " + msg + "\n");

                chatInput.setText("");
            }
        });
    }

    private void sendText() {

        if (updating) return;

        if (out != null) {

            String text =
                    editorArea.getText().replace("\n", "\\n");

            out.println("TEXT:" + text);
        }
    }

    private void connect() {

        try {

            String ip = JOptionPane.showInputDialog(
                    frame,
                    "Server IP",
                    "127.0.0.1");

            if (ip == null) System.exit(0);

            Socket socket = new Socket(ip, 9090);

            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            out = new PrintWriter(
                    socket.getOutputStream(), true);

            chatArea.append("Connected\n\n");

            new Thread(() -> {

                try {

                    String line;

                    while ((line = in.readLine()) != null) {

                        if (line.startsWith("TEXT:")) {

                            String text =
                                    line.substring(5)
                                            .replace("\\n", "\n");

                            SwingUtilities.invokeLater(() -> {

                                updating = true;

                                int pos =
                                        editorArea.getCaretPosition();

                                editorArea.setText(text);

                                editorArea.setCaretPosition(
                                        Math.min(pos,
                                                text.length()));

                                updating = false;
                            });
                        }

                        else if (line.startsWith("CHAT:")) {

                            String msg = line.substring(5);

                            SwingUtilities.invokeLater(() ->
                                    chatArea.append(
                                            "Friend : "
                                                    + msg + "\n"));
                        }
                    }

                } catch (Exception e) {

                    SwingUtilities.invokeLater(() ->
                            chatArea.append(
                                    "\nDisconnected\n"));
                }

            }).start();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    frame,
                    "Cannot connect to server.\nStart server first.");

            System.exit(0);
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            LanDocsClient client = new LanDocsClient();

            client.frame.setVisible(true);

            client.connect();
        });
    }
}