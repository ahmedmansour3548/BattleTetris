 

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;

public class WackyPlayer1 extends WackyPlayer {
    private int initalX = 5;
    private int initalY = 4;
    private Point pieceOrigin = new Point(initalX, initalY);
    private WackyTetramino currentPiece;
    private Direction dropDirection;
    private int orientation;
    private ArrayList<Integer> nextPieces;
    private long score;
    private boolean passingOver;

    private ArrayList<Point> pieceOffsets;
    private ArrayList<Point> boardPosition;

    private final int playerID = 1;

    private WackyTetris game;
    private final ArrayList<Color> tetraminoColors = new ArrayList<>(Arrays.asList(
        Color.decode("#FF0000"), Color.decode("#FF7F00"), Color.decode("#FFFF00"), Color.decode("#00FF00"), Color.decode("#00FFFF"), Color.decode("#0000FF"), Color.decode("#8B00FF")
    ));

    private final Color passOverColor = Color.WHITE;

    WackyPlayer1(WackyTetris game, Direction dropDirection) {
        this.game = game;
        this.dropDirection = dropDirection;
        this.nextPieces = new ArrayList<Integer>();
    }

    protected void drop() {
        if (!this.game.collidesAt(pieceOrigin.x, pieceOrigin.y + 1, this.pieceOffsets, playerID)) {
            pieceOrigin.y += 1;
            updatePiecePosition();
        } else {
            game.fixToBoard(playerID);
        }
        game.repaint();
    }

    @Override
    // Put a new, random piece into the dropping position
    protected void newPiece() {
        pieceOrigin = new Point(this.game.getNumTilesWidth() / 2, 4);
        this.boardPosition = new ArrayList<Point>();
        orientation = 0;
        
        if (nextPieces.isEmpty()) {
            Collections.addAll(nextPieces, 0, 1, 2, 3, 4, 5, 6);
            //Collections.addAll(nextPieces, 3);
            Collections.shuffle(nextPieces);
        }
        this.currentPiece = WackyTetramino.values()[nextPieces.get(0)];
        this.currentPiece.additional = new ArrayList<Point>();
        this.pieceOffsets = this.currentPiece.orientations().get(orientation);
        updatePiecePosition();
        nextPieces.remove(0);
    }

    @Override
    // Rotate the piece clockwise or counterclockwise
    protected void rotate(int i) {
        int newOrientation = (this.orientation + i) % 4;
        if (newOrientation < 0) {
            newOrientation = 3;
        }
        ArrayList<Point> newPosition = this.currentPiece.orientations().get(newOrientation);
        if (!game.collidesAt(pieceOrigin.x, pieceOrigin.y, newPosition, playerID)) {
            this.pieceOffsets = newPosition;
            this.orientation = newOrientation;
            updatePiecePosition();
        }
        game.repaint();
    }

    @Override
    // Move the piece left or right
    protected void move(int i) {
        if (!game.collidesAt(this.pieceOrigin.x + i, this.pieceOrigin.y, this.pieceOffsets, playerID)) {
            this.pieceOrigin.x += i;
            updatePiecePosition();
        }
        this.game.repaint();
    }

    // Draw the falling piece
    protected void drawPiece(Graphics g) {
        if (this.currentPiece.additionalTiles() != null && !this.currentPiece.additionalTiles().isEmpty())
            this.pieceOffsets.addAll(this.currentPiece.additionalTiles());
        try {
            for (Point p : this.pieceOffsets) {
                int newX = p.x + this.pieceOrigin.x;
                int newY = p.y + this.pieceOrigin.y;
                if (this.passingOver
                        && this.game.getP2TetraminoColors().contains(this.game.getBoard()[newX][newY].getColor())) {
                    g.setColor(passOverColor);
                } else {
                    g.setColor(tetraminoColors.get(currentPiece.ordinal()));
                }
                g.fillRect((newX) * this.game.getTileSize(),
                        (newY) * this.game.getTileSize(),
                        this.game.getTileSize() - 1, this.game.getTileSize() - 1);
            }
        } catch (ConcurrentModificationException cme) {
            return;
        }
    }

    @Override
    protected void updatePiecePosition() {
        this.boardPosition.clear();
        try {
            for (Point p : this.pieceOffsets) {
                this.boardPosition.add(new Point(p.x + this.pieceOrigin.x, p.y + this.pieceOrigin.y));
            }
        } catch (ConcurrentModificationException cme) {
            System.err.println("OOPS, got A CME!");
        }
    }

    @Override
    protected void setPassingOver(boolean passingOver) {
        this.passingOver = passingOver;
    }

    @Override
    protected void addScore(int scoreToAdd) {
        this.score += scoreToAdd;
    }

    protected long getScore() {
        return score;
    }

    @Override
    protected Direction getDirection() {
        return dropDirection;
    }

    @Override
    protected int getOrientation() {
        return orientation;
    }

    @Override
    protected ArrayList<Point> getPieceOffsets() {
        return this.pieceOffsets;
    }

    @Override
    protected ArrayList<Point> getCurrentBoardPosition() {
        return boardPosition;
    }

    @Override
    protected void destroyTile(int tile) {
        this.pieceOffsets.remove(tile);
    }


    @Override
    protected Color getTetraminoColor() {
        return tetraminoColors.get(this.currentPiece.ordinal());
    }

    @Override
    protected WackyTetramino getCurrentPiece() {
        return this.currentPiece;
    }

    @Override
    protected Point getPieceOrigin() {
        return this.pieceOrigin;
    }

    @Override
    protected ArrayList<Color> getTetraminoColors() {
        return this.tetraminoColors;
    }

    @Override
    protected int getPlayerID() {
        return this.playerID;
    }

    @Override
    protected void toggleDirection() {
        this.dropDirection = (this.dropDirection == Direction.DOWN) ? Direction.UP : Direction.DOWN;
    }

    public void addPieceOffsets(ArrayList<Point> newOffsets, Point origin) {
        for (Point offset : newOffsets) {
            pieceOffsets.add(new Point(offset.x + origin.x, offset.y + origin.y));
        }
    }

    public void resetPiece () {
        
        this.currentPiece.clearAdditional();
    }
}
