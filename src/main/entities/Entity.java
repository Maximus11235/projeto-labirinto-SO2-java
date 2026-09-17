package entities;

/**
 * Entity — base para qualquer ator com posição no labirinto (Hero,
 * ProcessMonster). A posição é lida por múltiplas threads (a própria
 * entidade, a EDT ao repintar o mapa, outros monstros ao checar colisão),
 * por isso os acessos são sincronizados.
 */
public abstract class Entity {

    private int row;
    private int col;

    protected Entity(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public synchronized int getRow() {
        return row;
    }

    public synchronized int getCol() {
        return col;
    }

    public synchronized void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }
}
