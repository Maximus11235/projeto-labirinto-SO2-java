package core;

/**
 * GameEngine — o "Sistema Operacional" do jogo.
 *
 * Centraliza o estado global (rodando / pausado) e o monitor usado para
 * suspender e acordar todas as threads de ProcessMonster de uma vez,
 * sem busy waiting (nenhuma thread fica em loop apertado verificando
 * uma flag — todas dormem em wait() até serem notificadas).
 */
public class GameEngine {

    private final Object pauseLock = new Object();
    private volatile boolean running = true;
    private volatile boolean paused = false;

    public boolean isRunning() {
        return running;
    }

    public boolean isPaused() {
        return paused;
    }

    /** Alterna entre pausado/rodando e acorda todas as threads em espera. */
    public void togglePause() {
        synchronized (pauseLock) {
            paused = !paused;
            if (!paused) {
                pauseLock.notifyAll();
            }
        }
    }

    /**
     * Chamado pelas threads de background (ProcessMonster) a cada ciclo.
     * Bloqueia a thread chamadora enquanto o jogo estiver pausado, sem
     * consumir CPU, e retorna assim que o jogo for retomado ou encerrado.
     */
    public void awaitIfPaused() throws InterruptedException {
        synchronized (pauseLock) {
            while (paused && running) {
                pauseLock.wait();
            }
        }
    }

    /** Encerra o jogo e acorda qualquer thread presa em pausa para que possa sair do loop. */
    public void shutdown() {
        running = false;
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }
}
