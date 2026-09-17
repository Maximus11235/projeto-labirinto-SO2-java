package ui;

import javax.swing.*;
import java.awt.*;

/**
 * TerminalPanel — console lateral que exibe o "log" das ações dos
 * processos (movimentos relevantes, batalhas, pausas, etc.).
 */
public class TerminalPanel extends JPanel {

    private final JTextArea textArea;

    public TerminalPanel() {
        setLayout(new BorderLayout());

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setBackground(Color.BLACK);
        textArea.setForeground(new Color(80, 220, 80));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Console de Processos"));
        add(scrollPane, BorderLayout.CENTER);
    }

    /** Deve ser chamado apenas na EDT — o GameFrame já garante isso. */
    public void appendLog(String message) {
        textArea.append(message + "\n");
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    public void clear() {
        textArea.setText("");
    }
}
