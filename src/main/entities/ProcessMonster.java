package src.main.entities;

import src.main.core.GameEngine;
import src.main.core.MapLoader;
import src.main.ui.GameFrame;
import java.util.Random;

public class ProcessMonster extends Entity implements Runnable {
    private boolean isExecuting = false; // Fica true quando o Herói colide
    private GameFrame ui;
    private Random random = new Random();

    public ProcessMonster(int startX, int startY, MapLoader mapLoader, GameEngine engine, GameFrame ui) {
        super(startX, startY, mapLoader, engine);
        this.ui = ui;
    }

    @Override
    public void run() {
        try {
            while (engine.isRunning()) {
                engine.checkPause(); // Ponto de bloqueio obrigatório

                if (!isExecuting) {
                    moveRandomly();
                } else {
                    // Lógica de cálculo pesada (Fibonacci, Primos, etc.)
                    // e comunicação com o BattleAutomaton
                }

                // Movimentação discreta: teleporte instantâneo, depois dorme
                Thread.sleep(500); // Controla a velocidade do monstro
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaura o status de interrupção
        }
    }

    private void moveRandomly() {
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}}; // UP, DOWN, LEFT, RIGHT
        int[] dir = directions[random.nextInt(4)];

        int newX = x + dir[0];
        int newY = y + dir[1];

        // Tenta mover utilizando o método herdado da Entity
        if (move(newX, newY)) {
            // Dispara evento de UI para repintar a tela, se necessário
        }
    }
}