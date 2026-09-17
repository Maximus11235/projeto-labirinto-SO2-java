package mechanics;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * BattleAutomaton — máquina de estados finitos simples. Lê a lista de
 * falas de golpe de resources/fsm/automaton.txt uma única vez, na
 * inicialização, e depois apenas cicla por elas a cada colisão.
 *
 * Como várias threads de ProcessMonster podem colidir com o Hero em
 * momentos próximos, nextMove() usa um AtomicInteger para ser thread-safe
 * sem precisar de um bloco synchronized.
 */
public class BattleAutomaton {

    private final List<String> moves = new ArrayList<>();
    private final AtomicInteger cursor = new AtomicInteger(0);

    public BattleAutomaton(String path) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    moves.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("[BattleAutomaton] Não foi possível ler '" + path
                    + "'. Usando golpes padrão.");
        }
        if (moves.isEmpty()) {
            moves.addAll(defaultMoves());
        }
    }

    private List<String> defaultMoves() {
        return Arrays.asList(
                "O processo trava o mutex e ataca com um SIGKILL!",
                "Uma condição de corrida abre uma brecha na defesa!",
                "O semáforo é liberado e o golpe final é desferido!",
                "Deadlock evitado por pouco — contra-ataque certeiro!",
                "O escalonador prioriza o golpe crítico!"
        );
    }

    /** Retorna a próxima fala/golpe, ciclando pela lista. Thread-safe. */
    public String nextMove() {
        int index = cursor.getAndIncrement() % moves.size();
        return moves.get(index);
    }
}
