 

import java.awt.Color;

public class WackyTile {
    private int x;
    private int y;
    private Color color;
    private int owner;

    public WackyTile(int x, int y, Color color, int owner) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.owner = owner;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public boolean isOwner(int playerID) {
        return playerID == this.owner;
    }

    public boolean isBorder() {
        return this.owner == -1;
    }

    public boolean isEmpty() {
        return this.owner == 0;
    }
}
