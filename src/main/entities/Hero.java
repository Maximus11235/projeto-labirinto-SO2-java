package src.main.entities;

import src.main.core.GameEngine;
import src.main.core.MapLoader;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Hero extends Entity {
    private boolean isExecutingProcess = false;

    public Hero(int startX, int startY, MapLoader mapLoader, GameEngine engine) {
        super(startX, startY, mapLoader, engine);
    }

    // Adaptador de teclado para ser adicionado ao JFrame principal
    public KeyAdapter getInputListener() {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!engine.isRunning() || isExecutingProcess) return;

                // Sistema de pausa global (Tecla P)
                if (e.getKeyCode() == KeyEvent.VK_P) {
                    engine.togglePause();
                    return;
                }

                if (engine.isPaused()) return; // Não move se estiver pausado

                int newX = x;
                int newY = y;

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W: case KeyEvent.VK_UP:    newY--; break;
                    case KeyEvent.VK_S: case KeyEvent.VK_DOWN:  newY++; break;
                    case KeyEvent.VK_A: case KeyEvent.VK_LEFT:  newX--; break;
                    case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: newX++; break;
                }

                // Utiliza a movimentação validada da classe base Entity
                if (move(newX, newY)) {
                    checkCollisionWithMonsters();
                    // TODO: Chamar atualização da UI (repintar JLabel)
                }
            }
        };
    }

    private void checkCollisionWithMonsters() {
        // Lógica para varrer a lista de monstros ativos no GameEngine
        // Se a posição (x,y) do Herói == posição de um Monstro:
        // isExecutingProcess = true;
        // Iniciar BattleAutomaton
    }
}