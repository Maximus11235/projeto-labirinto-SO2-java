package src.main.ui;

// ui/GameFrame.java
import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {
    private JPanel mazePanel;
    private JTextArea terminalArea;
    private JLabel[][] gridCells;
    
    public GameFrame(int rows, int cols) {
        setTitle("OS CPU Simulator - Execution Maze");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. O Labirinto (Grid Layout)
        mazePanel = new JPanel(new GridLayout(rows, cols));
        mazePanel.setBackground(Color.BLACK);
        gridCells = new JLabel[rows][cols];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                gridCells[i][j] = new JLabel();
                gridCells[i][j].setOpaque(true);
                // Configurar bordas ou cores baseadas no MapLoader aqui
                mazePanel.add(gridCells[i][j]);
            }
        }

        // 2. O Terminal (Text Area)
        terminalArea = new JTextArea(10, 30);
        terminalArea.setEditable(false);
        terminalArea.setBackground(Color.DARK_GRAY);
        terminalArea.setForeground(Color.GREEN);
        terminalArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        JScrollPane scrollPane = new JScrollPane(terminalArea);
        
        // Montagem final
        add(mazePanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.EAST);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    // Método thread-safe para atualizar o console
    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            terminalArea.append(message + "\n");
            // Auto-scroll para o fim
            terminalArea.setCaretPosition(terminalArea.getDocument().getLength());
        });
    }
}