 

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;

public abstract class WackyPlayer {


    protected abstract void rotate(int i);
    protected abstract void move(int i);
    protected abstract void drop();
    protected abstract void newPiece();
    protected abstract void addScore(int scoreToAdd);
    protected abstract void drawPiece(Graphics g);
    protected abstract WackyTetramino getCurrentPiece();
    protected abstract ArrayList<Point> getPieceOffsets();
    protected abstract ArrayList<Point> getCurrentBoardPosition();
    protected abstract Point getPieceOrigin();
    protected abstract Direction getDirection();
    protected abstract int getOrientation();
    protected abstract Color getTetraminoColor();
    protected abstract ArrayList<Color> getTetraminoColors();
    protected abstract int getPlayerID();
    protected abstract void destroyTile(int position);
    protected abstract void setPassingOver(boolean passingOver);
    protected abstract void toggleDirection();
    protected abstract void updatePiecePosition();
}
