package entities;

import core.GameEngine;
import core.MapLoader;
import mechanics.BattleAutomaton;

import java.util.Random;

/**
 * ProcessMonster — a representação de um "processo" concorrente no
 * labirinto. Implementa Runnable e roda em sua própria Thread, movendo-se
 * autonomamente até colidir com o Hero, momento em que consulta o
 * BattleAutomaton e encerra sua própria execução (simulando que cumpriu
 * sua tarefa).
 *
 * Suporta os quatro verbos exigidos pelo enunciado a nível de processo
 * individual: criar (construtor + start() feito pelo Main), finalizar
 * (terminate()), suspender (suspendProcess()) e retomar (resumeProcess()).
 */
public class ProcessMonster extends Entity implements Runnable {

    /** Callbacks para a UI ser notificada sem que este processo toque em Swing diretamente. */
    public interface MonsterListener {
        void onMove(ProcessMonster monster);
        void onBattle(ProcessMonster monster, String moveDescription);
        void onDefeated(ProcessMonster monster);
    }

    private final String name;
    private final MapLoader mapLoader;
    private final GameEngine engine;
    private final Hero hero;
    private final BattleAutomaton automaton;
    private final MonsterListener listener;
    private final Random random = new Random();

    private final Object suspendLock = new Object();
    private volatile boolean suspended = false;
    private volatile boolean alive = true;

    public ProcessMonster(String name, int startRow, int startCol, MapLoader mapLoader,
                           GameEngine engine, Hero hero, BattleAutomaton automaton,
                           MonsterListener listener) {
        super(startRow, startCol);
        this.name = name;
        this.mapLoader = mapLoader;
        this.engine = engine;
        this.hero = hero;
        this.automaton = automaton;
        this.listener = listener;
    }

    public String getName() {
        return name;
    }

    /** Suspende este processo individualmente (sem afetar os demais). */
    public void suspendProcess() {
        suspended = true;
    }

    /** Retoma este processo individualmente. */
    public void resumeProcess() {
        synchronized (suspendLock) {
            suspended = false;
            suspendLock.notifyAll();
        }
    }

    /** Finaliza este processo definitivamente (equivalente a "matar" o processo). */
    public void terminate() {
        alive = false;
        resumeProcess(); // acorda a thread caso esteja suspensa, para que note 'alive == false' e saia
    }

    @Override
    public void run() {
        try {
            while (engine.isRunning() && alive) {
                engine.awaitIfPaused(); // pausa global orquestrada pelo GameEngine

                synchronized (suspendLock) {
                    while (suspended && alive) {
                        suspendLock.wait(); // pausa individual deste processo
                    }
                }
                if (!alive) {
                    break;
                }

                wander();
                if (listener != null) {
                    listener.onMove(this);
                }

                if (getRow() == hero.getRow() && getCol() == hero.getCol()) {
                    battle();
                    break; // tarefa cumprida: o processo encerra sua própria execução
                }

                Thread.sleep(300 + random.nextInt(500));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void wander() {
        int[] deltaRow = {-1, 1, 0, 0};
        int[] deltaCol = {0, 0, -1, 1};

        for (int attempt = 0; attempt < 4; attempt++) {
            int direction = random.nextInt(4);
            int newRow = getRow() + deltaRow[direction];
            int newCol = getCol() + deltaCol[direction];
            if (!mapLoader.isWall(newRow, newCol)) {
                setPosition(newRow, newCol);
                return;
            }
        }
        // cercado por paredes neste passo: permanece parado
    }

    private void battle() {
        String move = automaton.nextMove();
        if (listener != null) {
            listener.onBattle(this, move);
        }
        hero.registerTaskCompleted();
        alive = false;
        if (listener != null) {
            listener.onDefeated(this);
        }
    }
}
