package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * MapLoader — le o labirinto de um arquivo externo em vez de deixá-lo
 * hardcoded no código Java, permitindo criar novas fases sem recompilar.
 *
 * Formato do arquivo (resources/maps/map.txt): uma matriz de inteiros
 * separados por espaço, onde 0 = chão, 1 = parede, 2 = saída.
 * Se o arquivo não existir ou estiver mal formatado, um mapa padrão
 * embutido é usado, para que o jogo sempre possa ser testado.
 */
public class MapLoader {

    private final int[][] grid;
    private int startRow;
    private int startCol;
    private int exitRow;
    private int exitCol;

    public MapLoader(String path) {
        List<int[]> rows = readFromFile(path);
        if (rows == null || rows.isEmpty()) {
            System.out.println("[MapLoader] Não foi possível ler '" + path
                    + "'. Usando mapa padrão embutido.");
            rows = defaultMap();
        }
        this.grid = rows.toArray(new int[0][]);
        locateSpecialCells();
    }

    private List<int[]> readFromFile(String path) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(path))) {
            List<int[]> rows = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] tokens = line.split("\\s+");
                int[] row = new int[tokens.length];
                for (int i = 0; i < tokens.length; i++) {
                    row[i] = Integer.parseInt(tokens[i]);
                }
                rows.add(row);
            }
            return rows;
        } catch (IOException | NumberFormatException e) {
            return null;
        }
    }

    private List<int[]> defaultMap() {
        int[][] fallback = {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1},
            {1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1},
            {1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 1},
            {1, 0, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1},
            {1, 1, 1, 1, 1, 0, 1, 0, 1, 1, 0, 1},
            {1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1},
            {1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 0, 1},
            {1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 2, 1},
            {1, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
        };
        List<int[]> rows = new ArrayList<>();
        for (int[] row : fallback) {
            rows.add(row);
        }
        return rows;
    }

    private void locateSpecialCells() {
        startRow = 1;
        startCol = 1;
        exitRow = -1;
        exitCol = -1;
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                if (grid[r][c] == 2) {
                    exitRow = r;
                    exitCol = c;
                }
            }
        }
        if (exitRow == -1) {
            exitRow = grid.length - 2;
            exitCol = grid[exitRow].length - 2;
        }
    }

    public int getRows() {
        return grid.length;
    }

    public int getCols() {
        return grid[0].length;
    }

    public boolean isWall(int row, int col) {
        if (row < 0 || row >= grid.length || col < 0 || col >= grid[row].length) {
            return true;
        }
        return grid[row][col] == 1;
    }

    public boolean isExit(int row, int col) {
        return row == exitRow && col == exitCol;
    }

    public int getStartRow() {
        return startRow;
    }

    public int getStartCol() {
        return startCol;
    }
}
