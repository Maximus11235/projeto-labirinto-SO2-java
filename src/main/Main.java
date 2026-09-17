package src.main;

import src.main.core.*;
import src.main.ui.*;
import src.main.entities.*;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Executa a UI na thread correta do Swing
        SwingUtilities.invokeLater(() -> {
            int rows = 15;
            int cols = 15;

            GameEngine engine = new GameEngine();
            MapLoader mapLoader = new MapLoader();
            mapLoader.loadMap("resources/maps/map.txt", rows, cols);

            GameFrame ui = new GameFrame(rows, cols);
            
            Hero hero = new Hero(1, 1, mapLoader, engine);
            ui.addKeyListener(hero.getInputListener());
            ui.setFocusable(true); // Obrigatório para receber eventos de teclado

            // Criação de um monstro repassando mapLoader no construtor
            ProcessMonster monster1 = new ProcessMonster(5, 5, mapLoader, engine, ui);
            Thread monsterThread = new Thread(monster1);
            monsterThread.start();

            ui.appendLog("Sistema Operacional iniciado.");
            ui.appendLog("Aguardando processos na fila de aptos...");
        });
    }
}