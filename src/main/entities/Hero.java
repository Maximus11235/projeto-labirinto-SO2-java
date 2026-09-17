package entities;

import core.MapLoader;

import java.awt.event.KeyEvent;

/**
 * Hero — representa o jogador, controlado pelo teclado.
 *
 * Só sabe responder "para onde posso ir" (consultando o MapLoader) e
 * "quantas tarefas já completei". A interface gráfica não precisa saber
 * como o herói se move — apenas pergunta a posição atual a cada refresh.
 */
public class Hero extends Entity {

    /** Notificado quando o herói alcança a saída com todas as tarefas concluídas. */
    public interface ExitListener {
        void onExit();
    }

    /** Notificado quando o herói tenta sair, mas ainda faltam tarefas. */
    public interface BlockedExitListener {
        void onBlockedExit(int remainingTasks);
    }

    private final MapLoader mapLoader;
    private final int tasksRequired;

    private int tasksCompleted = 0;
    private boolean exited = false;

    private ExitListener exitListener;
    private BlockedExitListener blockedExitListener;

    public Hero(MapLoader mapLoader, int tasksRequired) {
        super(mapLoader.getStartRow(), mapLoader.getStartCol());
        this.mapLoader = mapLoader;
        this.tasksRequired = tasksRequired;
    }

    public void setExitListener(ExitListener listener) {
        this.exitListener = listener;
    }

    public void setBlockedExitListener(BlockedExitListener listener) {
        this.blockedExitListener = listener;
    }

    /** Chamado por um ProcessMonster derrotado ao concluir sua "tarefa". */
    public synchronized void registerTaskCompleted() {
        tasksCompleted++;
    }

    public synchronized int getTasksCompleted() {
        return tasksCompleted;
    }

    public int getTasksRequired() {
        return tasksRequired;
    }

    public synchronized boolean hasExited() {
        return exited;
    }

    /** Processa uma tecla de movimento (WASD ou setas). */
    public void handleKeyPress(int keyCode) {
        int deltaRow = 0;
        int deltaCol = 0;

        switch (keyCode) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                deltaRow = -1;
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                deltaRow = 1;
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                deltaCol = -1;
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                deltaCol = 1;
                break;
            default:
                return; // tecla irrelevante para movimento
        }

        int newRow = getRow() + deltaRow;
        int newCol = getCol() + deltaCol;

        if (mapLoader.isWall(newRow, newCol)) {
            return; // trombou na parede: posição não muda
        }

        setPosition(newRow, newCol);
        checkExit(newRow, newCol);
    }

    /** Reservado para movimento contínuo (segurar tecla); no-op no modelo passo-a-passo atual. */
    public void handleKeyRelease(int keyCode) {
        // Sem efeito por enquanto.
    }

    private synchronized void checkExit(int row, int col) {
        if (!mapLoader.isExit(row, col) || exited) {
            return;
        }
        if (tasksCompleted >= tasksRequired) {
            exited = true;
            if (exitListener != null) {
                exitListener.onExit();
            }
        } else if (blockedExitListener != null) {
            blockedExitListener.onBlockedExit(tasksRequired - tasksCompleted);
        }
    }
}
