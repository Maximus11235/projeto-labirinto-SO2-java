package src.main.core;

// core/MapLoader.java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MapLoader {
    private int[][] grid;

    public void loadMap(String filePath, int rows, int cols) {
        grid = new int[rows][cols];
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int row = 0;
            while ((line = br.readLine()) != null && row < rows) {
                String[] tokens = line.trim().split("\\s+"); // Separa por espaços
                for (int col = 0; col < tokens.length; col++) {
                    grid[row][col] = Integer.parseInt(tokens[col]);
                }
                row++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public boolean isWall(int x, int y) {
        return grid[y][x] == 1; // 1 = parede sólida
    }
}