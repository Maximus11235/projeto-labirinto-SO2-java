package src.main.entities;
import src.main.core.*;
// entities/ProcessMonster.java;
public class ProcessMonster implements Runnable {
    private int x, y;
    private GameEngine engine;
    private boolean isExecuting = false; // Fica true quando o Herói colide
    
    public ProcessMonster(int startX, int startY, GameEngine engine, faltaAlgoAqui) {
        this.x = startX;
        this.y = startY;
        this.engine = engine;
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
        // Lógica de grid: checa colisão com paredes do MapLoader e altera x,y
        // Dispara evento de UI para repintar a tela
    }
}
