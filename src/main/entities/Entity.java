package src.main.entities;

import src.main.core.GameEngine;
import src.main.core.MapLoader;

public abstract class Entity {
    protected int x;
    protected int y;
    protected GameEngine engine;
    protected MapLoader mapLoader;

    public Entity(int startX, int startY, MapLoader mapLoader, GameEngine engine) {
        this.x = startX;
        this.y = startY;
        this.mapLoader = mapLoader;
        this.engine = engine;
    }

    // Tenta mover a entidade para uma nova posição considerando o labirinto
    public boolean move(int newX, int newY) {
        if (!mapLoader.isWall(newX, newY)) {
            this.x = newX;
            this.y = newY;
            return true;
        }
        return false;
    }

    // Getters e Setters
    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
}