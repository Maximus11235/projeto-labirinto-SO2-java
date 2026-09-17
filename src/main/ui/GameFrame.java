package ui;

import core.GameEngine;
import entities.Hero;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * GameFrame — Camada de Apresentação (Swing).
 *
 * Responsabilidade única: montar a janela principal, cablear o teclado ao
 * Hero e expor métodos "seguros" (via SwingUtilities.invokeLater) para que
 * threads de background (ProcessMonster, BattleAutomaton, GameEngine)
 * possam atualizar a tela sem travar ou corromper o Event Dispatch Thread
 * (EDT).
 *
 * IMPORTANTE — API assumida para as classes colaboradoras (ajuste as
 * assinaturas abaixo caso suas classes reais sejam diferentes):
 *
 *   core.GameEngine
 *     - boolean isRunning()
 *     - boolean isPaused()
 *     - void togglePause()
 *     - void shutdown()
 *
 *   entities.Hero
 *     - void handleKeyPress(int keyCode)   // recebe KeyEvent.VK_*
 *     - void handleKeyRelease(int keyCode) // opcional, para movimento contínuo
 *
 *   ui.MazePanel (extends JPanel)
 *     - construtor MazePanel(MapLoader mapLoader, Hero hero, ...)
 *     - void refresh()  // ou apenas usa repaint() padrão do Swing
 *
 *   ui.TerminalPanel (extends JPanel)
 *     - void appendLog(String message)
 *     - void clear()
 *
 * Se os nomes reais diferirem, troque apenas os pontos marcados com
 * comentário "// AJUSTE AQUI" — o resto do arquivo não precisa mudar.
 */
public class GameFrame extends JFrame {

    private static final String TITLE = "O Labirinto dos Processos";
    private static final int TERMINAL_WIDTH = 320;

    private final GameEngine engine;
    private final Hero hero;
    private final MazePanel mazePanel;
    private final TerminalPanel terminalPanel;

    private JLabel statusLabel;

    public GameFrame(GameEngine engine, Hero hero, MazePanel mazePanel, TerminalPanel terminalPanel) {
        super(TITLE);
        this.engine = engine;
        this.hero = hero;
        this.mazePanel = mazePanel;
        this.terminalPanel = terminalPanel;

        buildUI();
        wireKeyboard();
        wireWindowLifecycle();

        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    // ---------------------------------------------------------------
    // Montagem da UI
    // ---------------------------------------------------------------

    private void buildUI() {
        setLayout(new BorderLayout(8, 8));

        // Centro: o labirinto (a própria MazePanel deve usar GridLayout
        // internamente para desenhar a matriz de células).
        add(mazePanel, BorderLayout.CENTER);

        // Lado direito: console de logs (ações dos processos/monstros).
        terminalPanel.setPreferredSize(new Dimension(TERMINAL_WIDTH, 0));
        add(terminalPanel, BorderLayout.EAST);

        // Topo: barra de status simples (estado do GameEngine).
        statusLabel = new JLabel(" Pronto. Use WASD ou as setas para mover o herói. ");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(statusLabel, BorderLayout.NORTH);

        // Rodapé: dica de controles.
        JLabel footer = new JLabel(" [P] Pausar/Retomar   [ESC] Sair ", SwingConstants.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    // ---------------------------------------------------------------
    // Teclado -> Hero (Isolamento de Input)
    // ---------------------------------------------------------------

    private void wireKeyboard() {
        // Usamos um KeyListener no próprio frame; para isso o frame
        // precisa ser focável e receber o foco do teclado.
        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();

                switch (code) {
                    case KeyEvent.VK_P:
                        togglePause();
                        return;
                    case KeyEvent.VK_ESCAPE:
                        confirmExit();
                        return;
                    default:
                        break;
                }

                // Movimento: delega inteiramente ao Hero. O GameFrame não
                // decide "para onde" o herói vai — apenas repassa a intenção.
                if (isMovementKey(code) && !engine.isPaused()) {
                    hero.handleKeyPress(code); // AJUSTE AQUI se a assinatura for outra
                    refreshMaze();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // Reservado para movimento contínuo (segurar tecla); o Hero
                // atual trata isso como no-op, já que o modelo é passo-a-passo.
                hero.handleKeyRelease(e.getKeyCode());
            }
        });
    }

    private boolean isMovementKey(int code) {
        return code == KeyEvent.VK_W || code == KeyEvent.VK_UP
            || code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN
            || code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT
            || code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT;
    }

    // ---------------------------------------------------------------
    // Ciclo de vida da janela <-> GameEngine
    // ---------------------------------------------------------------

    private void wireWindowLifecycle() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
    }

    private void togglePause() {
        engine.togglePause(); // AJUSTE AQUI se o método tiver outro nome
        boolean paused = engine.isPaused();
        setStatus(paused ? "Pausado. Pressione [P] para retomar."
                          : "Em execução. Boa sorte no labirinto!");
        appendLog(paused ? "[SISTEMA] Jogo pausado — todas as threads suspensas."
                          : "[SISTEMA] Jogo retomado — threads notificadas.");
    }

    private void confirmExit() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Encerrar o Labirinto dos Processos?",
                "Sair",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            engine.shutdown(); // AJUSTE AQUI se o método tiver outro nome
            dispose();
            System.exit(0);
        }
    }

    // ---------------------------------------------------------------
    // API thread-safe para as threads de background (Thread-Safety Visual)
    // ---------------------------------------------------------------
    //
    // ProcessMonster, BattleAutomaton e GameEngine rodam em threads
    // próprias. Nenhuma delas deve chamar métodos de Swing diretamente.
    // Elas devem chamar estes métodos públicos, que empacotam a
    // atualização em SwingUtilities.invokeLater() e a executam com
    // segurança na EDT.

    /** Solicita o redesenho do labirinto a partir de qualquer thread. */
    public void refreshMaze() {
        SwingUtilities.invokeLater(mazePanel::refresh);
    }

    /** Adiciona uma linha ao console lateral a partir de qualquer thread. */
    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> terminalPanel.appendLog(message));
    }

    /** Atualiza a barra de status a partir de qualquer thread. */
    public void setStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(" " + message + " "));
    }

    /** Exibe uma mensagem de vitória (chamado quando o Hero sai do labirinto). */
    public void showVictory(String message) {
        SwingUtilities.invokeLater(() -> {
            appendLog("[SISTEMA] " + message);
            setStatus("Vitória!");
            JOptionPane.showMessageDialog(this, message, "Labirinto concluído!",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }

    /** Exibe uma mensagem de derrota/game over. */
    public void showGameOver(String message) {
        SwingUtilities.invokeLater(() -> {
            appendLog("[SISTEMA] " + message);
            setStatus("Fim de jogo.");
            JOptionPane.showMessageDialog(this, message, "Game Over",
                    JOptionPane.WARNING_MESSAGE);
        });
    }

    // ---------------------------------------------------------------
    // Bootstrap
    // ---------------------------------------------------------------

    /**
     * Ponto de entrada visual: monta a janela na EDT, como recomenda a
     * documentação do Swing (nunca construir componentes fora da EDT).
     */
    public static void launch(GameEngine engine, Hero hero, MazePanel mazePanel, TerminalPanel terminalPanel) {
        SwingUtilities.invokeLater(() -> {
            GameFrame frame = new GameFrame(engine, hero, mazePanel, terminalPanel);
            frame.setVisible(true);
            frame.requestFocusInWindow();
        });
    }
}
