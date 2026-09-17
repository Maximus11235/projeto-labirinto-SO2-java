import core.GameEngine;
import core.MapLoader;
import entities.Hero;
import entities.ProcessMonster;
import mechanics.BattleAutomaton;
import ui.GameFrame;
import ui.MazePanel;
import ui.TerminalPanel;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Main — ponto de entrada. Monta todas as peças (mapa, motor, herói,
 * monstros, janela) e inicia uma Thread real para cada ProcessMonster,
 * demonstrando criação, movimento concorrente, colisão/batalha e
 * finalização de processos, além de pausa/retomada via GameEngine.
 */
public class Main {

    private static final String MAP_PATH = "resources/maps/map.txt";
    private static final String AUTOMATON_PATH = "resources/fsm/automaton.txt";
    private static final int TOTAL_MONSTERS = 3;

    // Pontos de partida (linha, coluna) dos monstros — devem ser células
    // de chão (0) no map.txt.
    private static final int[][] SPAWN_POINTS = {{3, 5}, {7, 8}, {9, 3}};

    public static void main(String[] args) {
        MapLoader mapLoader = new MapLoader(MAP_PATH);
        GameEngine engine = new GameEngine();
        BattleAutomaton automaton = new BattleAutomaton(AUTOMATON_PATH);

        Hero hero = new Hero(mapLoader, TOTAL_MONSTERS);

        // CopyOnWriteArrayList: a EDT lê esta lista a cada refresh() enquanto
        // as threads dos monstros só alteram sua própria posição (não a
        // lista em si), mas usamos uma coleção segura para concorrência
        // por robustez caso o design evolua para adicionar/remover monstros.
        List<ProcessMonster> monsters = new CopyOnWriteArrayList<>();

        MazePanel mazePanel = new MazePanel(mapLoader, hero, monsters);
        TerminalPanel terminalPanel = new TerminalPanel();
        GameFrame frame = new GameFrame(engine, hero, mazePanel, terminalPanel);

        hero.setExitListener(() ->
                frame.showVictory("O héroi escapou do labirinto! Todas as tarefas foram concluídas."));
        hero.setBlockedExitListener(remaining ->
                frame.appendLog("[HERÓI] A saída está bloqueada — ainda restam " + remaining + " tarefa(s)."));

        ProcessMonster.MonsterListener listener = new ProcessMonster.MonsterListener() {
            @Override
            public void onMove(ProcessMonster monster) {
                frame.refreshMaze();
            }

            @Override
            public void onBattle(ProcessMonster monster, String moveDescription) {
                frame.appendLog("[BATALHA] " + monster.getName() + ": " + moveDescription);
            }

            @Override
            public void onDefeated(ProcessMonster monster) {
                frame.appendLog("[SISTEMA] " + monster.getName() + " concluiu sua tarefa e foi finalizado ("
                        + hero.getTasksCompleted() + "/" + hero.getTasksRequired() + ").");
                frame.refreshMaze();
            }
        };

        for (int i = 0; i < TOTAL_MONSTERS; i++) {
            int[] spawn = SPAWN_POINTS[i % SPAWN_POINTS.length];
            ProcessMonster monster = new ProcessMonster(
                    "Processo-" + (i + 1), spawn[0], spawn[1],
                    mapLoader, engine, hero, automaton, listener);
            monsters.add(monster);
        }

        frame.appendLog("[SISTEMA] " + TOTAL_MONSTERS
                + " processo(s) inicializados. Derrote todos para liberar a saída.");
        frame.setStatus("Explorando o labirinto...");

        // Cria e inicia uma Thread real para cada processo (monstro).
        // Daemon = true para que a JVM não fique presa caso o usuário
        // feche a janela sem passar por confirmExit().
        List<Thread> monsterThreads = new ArrayList<>();
        for (ProcessMonster monster : monsters) {
            Thread thread = new Thread(monster, monster.getName());
            thread.setDaemon(true);
            monsterThreads.add(thread);
            thread.start();
        }

        SwingUtilities.invokeLater(() -> {
            frame.setVisible(true);
            frame.requestFocusInWindow();
        });
    }
}
