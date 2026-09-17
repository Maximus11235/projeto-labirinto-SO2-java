package ui;

import core.MapLoader;
import entities.Hero;
import entities.ProcessMonster;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * MazePanel — representação visual do labirinto usando GridLayout, onde
 * cada célula é um JLabel colorido. Simples, mas suficiente para mostrar
 * os processos percorrendo caminhos diferentes em tempo real.
 */
public class MazePanel extends JPanel {

    private static final int CELL_SIZE = 32;

    private static final Color WALL_COLOR = new Color(40, 40, 40);
    private static final Color FLOOR_COLOR = new Color(235, 235, 235);
    private static final Color EXIT_COLOR = new Color(80, 200, 120);
    private static final Color HERO_COLOR = new Color(50, 110, 220);
    private static final Color MONSTER_COLOR = new Color(210, 60, 60);

    private final MapLoader mapLoader;
    private final Hero hero;
    private final List<ProcessMonster> monsters;
    private final JLabel[][] cells;

    public MazePanel(MapLoader mapLoader, Hero hero, List<ProcessMonster> monsters) {
        this.mapLoader = mapLoader;
        this.hero = hero;
        this.monsters = monsters;

        int rows = mapLoader.getRows();
        int cols = mapLoader.getCols();

        setLayout(new GridLayout(rows, cols, 1, 1));
        setBackground(Color.BLACK);

        cells = new JLabel[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                JLabel cell = new JLabel();
                cell.setOpaque(true);
                cell.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
                cells[r][c] = cell;
                add(cell);
            }
        }

        refresh();
    }

    /**
     * Recalcula as cores de cada célula a partir do estado atual (mapa,
     * herói, monstros) e solicita o redesenho. O GameFrame garante que
     * este método só é chamado na EDT (via SwingUtilities.invokeLater).
     */
    public void refresh() {
        int rows = mapLoader.getRows();
        int cols = mapLoader.getCols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Color color;
                if (mapLoader.isWall(r, c)) {
                    color = WALL_COLOR;
                } else if (mapLoader.isExit(r, c)) {
                    color = EXIT_COLOR;
                } else {
                    color = FLOOR_COLOR;
                }
                cells[r][c].setBackground(color);
            }
        }

        for (ProcessMonster monster : monsters) {
            int r = monster.getRow();
            int c = monster.getCol();
            if (withinBounds(r, c, rows, cols)) {
                cells[r][c].setBackground(MONSTER_COLOR);
            }
        }

        int heroRow = hero.getRow();
        int heroCol = hero.getCol();
        if (withinBounds(heroRow, heroCol, rows, cols)) {
            cells[heroRow][heroCol].setBackground(HERO_COLOR);
        }

        revalidate();
        repaint();
    }

    private boolean withinBounds(int row, int col, int rows, int cols) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }
}
