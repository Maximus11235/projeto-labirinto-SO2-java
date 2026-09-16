package src.main.mechanics;

// mechanics/BattleAutomaton.java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BattleAutomaton {
    private List<String> states; // As frases de golpe
    private int currentStateIndex = 0;

    public BattleAutomaton(String filePath) {
        states = new ArrayList<>();
        loadAutomaton(filePath);
    }

    private void loadAutomaton(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    states.add(line.trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar FSM: " + e.getMessage());
            // Fallback default
            states.add("Iniciando execução...");
            states.add("Processando dados pesados...");
            states.add("Golpe fatal na cabeça! Processo encerrado.");
        }
    }

    // Chamado pela thread do monstro/herói a cada turno da batalha
    public String executeNextTurn() {
        if (isFinished()) {
            return null;
        }
        String log = states.get(currentStateIndex);
        currentStateIndex++;
        return log;
    }

    public boolean isFinished() {
        return currentStateIndex >= states.size();
    }
}