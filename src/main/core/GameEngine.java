package src.main.core;

// core/GameEngine.java
public class GameEngine {
    private final Object pauseLock = new Object();
    private volatile boolean isPaused = false;
    private volatile boolean isRunning = true;

    // Acionado pela tecla de pausa (ex: 'P' ou 'ESC')
    public void togglePause() {
        isPaused = !isPaused;
        if (!isPaused) {
            synchronized (pauseLock) {
                pauseLock.notifyAll(); // Acorda todos os processos simultaneamente
            }
        }
    }

    // Deve ser chamado dentro do loop (run) de TODAS as threads em background
    public void checkPause() throws InterruptedException {
        synchronized (pauseLock) {
            // O while previne o problema de "spurious wakeups" (despertares falsos)
            while (isPaused) {
                pauseLock.wait(); 
            }
        }
    }
    
    public boolean isRunning() { return isRunning; }
    public void stopGame() { this.isRunning = false; }
}