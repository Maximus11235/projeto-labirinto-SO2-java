package entities;

import core.GameEngine;
import core.MapLoader;
import mechanics.BattleAutomaton;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ProcessMonster — a representação de um "processo" concorrente no
 * labirinto. Implementa Runnable e roda em sua própria Thread, movendo-se
 * autonomamente. A batalha pode ser disparada por qualquer um dos dois
 * lados: o próprio monstro, ao se mover para cima do herói, OU o herói,
 * ao se mover para cima do monstro (ver engageBattle()).
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

    // AtomicBoolean em vez de um simples volatile: a colisão pode ser
    // detectada quase ao mesmo tempo pela própria thread do monstro (em
    // wander()) e pela thread do herói (na EDT, ao processar uma tecla).
    // compareAndSet garante que a batalha ocorra exatamente uma vez,
    // não importa qual lado a detectou primeiro.
    private final AtomicBoolean alive = new AtomicBoolean(true);

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

    /** Finaliza este processo definitivamente (equivalente a "matar" o processo), sem batalha. */
    public void terminate() {
        alive.set(false);
        resumeProcess(); // acorda a thread caso esteja suspensa, para que note 'alive == false' e saia
    }

    /** Verdadeiro enquanto o processo não foi derrotado nem finalizado. */
    public boolean isAlive() {
        return alive.get();
    }

    @Override
    public void run() {
        try {
            while (engine.isRunning() && alive.get()) {
                engine.awaitIfPaused(); // pausa global orquestrada pelo GameEngine

                synchronized (suspendLock) {
                    while (suspended && alive.get()) {
                        suspendLock.wait(); // pausa individual deste processo
                    }
                }
                if (!alive.get()) {
                    break;
                }

                wander();
                if (listener != null) {
                    listener.onMove(this);
                }

                if (getRow() == hero.getRow() && getCol() == hero.getCol()) {
                    engageBattle();
                    break; // tarefa cumprida (ou já cumprida pelo herói): o processo encerra
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

    /**
     * Dispara a batalha contra o herói. Pode ser chamado tanto pela
     * própria thread deste monstro (colisão detectada em run()) quanto
     * pela thread do herói (colisão detectada em Hero.handleKeyPress,
     * rodando na EDT). O compareAndSet garante que, mesmo que os dois
     * lados detectem a colisão quase simultaneamente, a batalha só
     * aconteça uma vez.
     *
     * @return true se esta chamada foi quem efetivamente iniciou a
     *         batalha; false se o processo já estava morto/derrotado.
     */
    public boolean engageBattle() {
        if (!alive.compareAndSet(true, false)) {
            return false; // já derrotado ou finalizado por outra via
        }

        String move = automaton.nextMove();
        if (listener != null) {
            listener.onBattle(this, move);
        }
        hero.registerTaskCompleted();
        if (listener != null) {
            listener.onDefeated(this);
        }
        return true;
    }
}