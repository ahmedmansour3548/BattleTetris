package src;
// Player.java

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles piece generation, movement, rotation with SRS, scoring, and drawing.
 */
public class Player {
    private final int playerId;
    private final Direction dropDirection;
    private final Point spawnPoint;
    private final Board board;
    private final Tetris game;
    private Tetramino holdPiece = null;
    private boolean holdUsed = false;
    private boolean holdErrorPlayed = false;
    private Tetramino currentPiece;
    private final List<Integer> nextPieces = new ArrayList<>();
    private long score = 0;
    private boolean lost = false;
    private final List<Color> tetraminoColors;

    List<Color> base = Arrays.asList(
            Color.decode("#FF0000"), // red
            Color.decode("#FF7F00"), // orange
            Color.decode("#FFFF00"), // yellow
            Color.decode("#00FF00"), // green
            Color.decode("#00FFFF"), // cyan
            Color.decode("#0000FF"), // blue
            Color.decode("#8B00FF") // purple
    );

    // SRS Wall-Kick Tables
    // I-Piece kicks: from orientation N to N+1 (cw)
    private static final int[][][] I_KICKS = {
            { { 0, 0 }, { -2, 0 }, { 1, 0 }, { -2, -1 }, { 1, 2 } },
            { { 0, 0 }, { -1, 0 }, { 2, 0 }, { -1, 2 }, { 2, -1 } },
            { { 0, 0 }, { 2, 0 }, { -1, 0 }, { 2, 1 }, { -1, -2 } },
            { { 0, 0 }, { 1, 0 }, { -2, 0 }, { 1, -2 }, { -2, 1 } }
    };

    // J,L,S,T,Z kicks
    private static final int[][][] JLSTZ_KICKS = {
            { { 0, 0 }, { -1, 0 }, { -1, 1 }, { 0, -2 }, { -1, -2 } },
            { { 0, 0 }, { 1, 0 }, { 1, -1 }, { 0, 2 }, { 1, 2 } },
            { { 0, 0 }, { 1, 0 }, { 1, 1 }, { 0, -2 }, { 1, -2 } },
            { { 0, 0 }, { -1, 0 }, { -1, -1 }, { 0, 2 }, { -1, 2 } }
    };

    public Player(int playerId, Direction dropDirection, Point spawnPoint, Board board, Tetris game) {
        this.playerId = playerId;
        this.dropDirection = dropDirection;
        this.spawnPoint = new Point(spawnPoint);
        this.board = board;
        this.game = game;

        // Choose color palette
        if (playerId == 1) {
            this.tetraminoColors = base;
        } else {
            // for Player 2, shift each hue just a bit
            this.tetraminoColors = base.stream()
                    .map(c -> shiftHSB(c, 1f, -0.3f, 0.1f))
                    .collect(Collectors.toList());
        }
        newPiece();
    }

    private static Color shiftHSB(Color in, float hueShift, float satShift, float briShift) {
        float[] hsb = Color.RGBtoHSB(in.getRed(), in.getGreen(), in.getBlue(), null);
        float h = (hsb[0] + hueShift) % 1f;
        if (h < 0)
            h += 1f;
        float s = Math.min(1f, Math.max(0f, hsb[1] + satShift));
        float b = Math.min(1f, Math.max(0f, hsb[2] + briShift));
        return Color.getHSBColor(h, s, b);
    }

    // Piece Management
    /** Spawn a new random piece. */
    public void newPiece() {
        // if we’re about to run out, add two full shuffled bags
        if (nextPieces.size() < 7) {
            List<Integer> bag = Arrays.asList(0, 1, 2, 3, 4, 5, 6);
            Collections.shuffle(bag);
            nextPieces.addAll(bag);
            Collections.shuffle(bag);
            nextPieces.addAll(bag);
        }
        this.holdUsed = false;
        this.holdErrorPlayed = false;
        // then pop one off to spawn
        int shape = nextPieces.remove(0);
        currentPiece = new Tetramino();
        currentPiece.generateNewPiece(shape, new Point(spawnPoint));
    }

    /** Soft drop: move piece one step; fix & spawn new if collision. */
    public void drop() {
        Tetramino test = currentPiece.copy();
        test.dropPiece(dropDirection);
        if (board.isCollision(test.getTiles(), playerId)) {
            game.stopAllMergeLoops();
            board.fixPiece(
                    currentPiece.getTiles(),
                    playerId,
                    currentPiece.getShapeID(),
                    currentPiece.getMergeCount());
            game.playSound("sfx/fix_click.wav", false);
            int cleared = board.clearLines(playerId);
            if (cleared > 0) {
                game.playSound("sfx/line_clear.wav", false);
                score += computeScore(cleared);
            }
            checkLose(currentPiece.getTiles());
            newPiece();

        } else {
            currentPiece.dropPiece(dropDirection);
        }
    }

    /** Move left/right; undo on collision. */
    public void move(int dx) {
        currentPiece.movePiece(dx);
        if (board.isCollision(currentPiece.getTiles(), playerId)) {
            currentPiece.movePiece(-dx);
        } else {
            game.playSound("sfx/move_click.wav", false);
        }
    }

    /** Hard drop: instantly land piece at ghost position. */
    public void hardDrop() {
        List<Point> landing = getGhostTiles();
        board.fixPiece(
                landing,
                playerId,
                currentPiece.getShapeID(),
                currentPiece.getMergeCount());
        game.playSound("sfx/fix_click.wav", false);
        int cleared = board.clearLines(playerId);
        if (cleared > 0) {
            game.playSound("sfx/line_clear.wav", false);
            score += computeScore(cleared);
        }
        checkLose(landing);
        game.stopAllMergeLoops();
        game.onSlam(landing);
        newPiece();
        this.holdUsed = false;

    }

    // Rotation with SRS kicks
    /** Rotate CW (+1) or CCW (-1) using SRS wall kicks. */
    public void rotate(int delta) {
        int oldO = currentPiece.getOrientation();
        Tetramino test = currentPiece.copy();
        test.rotatePiece(delta);

        int shape = currentPiece.getShapeID();
        int[][] kicks;
        if (shape == 0)
            kicks = I_KICKS[oldO];
        else if (shape == 1)
            kicks = new int[][] { { 0, 0 } }; // O‑piece
        else
            kicks = JLSTZ_KICKS[oldO];

        for (int[] k : kicks) {
            // apply both x and y kick:
            test.shift(k[0], k[1]);
            if (!board.isCollision(test.getTiles(), playerId)) {
                currentPiece = test;
                game.playSound("sfx/rotate.wav", false);
                return;
            }
            // revert
            test.shift(-k[0], -k[1]);
        }
        // all kicks failed -> do nothing
    }

    public void hold() {
        if (currentPiece.getMergeCount() > 0 || holdUsed) {
            if (!holdErrorPlayed) {
                game.playSound("sfx/error.wav", false);
                holdErrorPlayed = true;
            }
            return;
        }

        holdErrorPlayed = false;
        Tetramino oldHold = holdPiece;
        // store the current into hold
        holdPiece = currentPiece.copy();
        holdPiece.generateNewPiece(currentPiece.getShapeID(), new Point(spawnPoint));
        holdPiece.setMergeCount(currentPiece.getMergeCount());

        if (oldHold == null) {
            // first time hold: pull the next piece
            newPiece();
        } else {
            // swap oldHold back into play
            currentPiece = oldHold.copy();
            currentPiece.generateNewPiece(currentPiece.getShapeID(), new Point(spawnPoint));
        }
        holdUsed = true;
    }

    /** Returns the currently held piece (or null). */
    public Tetramino getHoldPiece() {
        return holdPiece;
    }

    /** Peek at the next n shapes without consuming them. */
    public List<Integer> peekNext(int n) {
        return nextPieces.subList(0, Math.min(n, nextPieces.size()));
    }

    public void addScore(int delta) {
        this.score += delta;
    }

    // Ghost / Scoring / Lose
    public List<Point> getGhostTiles() {
        Tetramino ghost = currentPiece.copy();
        while (true) {
            Tetramino next = ghost.copy();
            next.dropPiece(dropDirection);
            if (board.isCollision(next.getTiles(), playerId))
                break;
            ghost = next;
        }
        return ghost.getTiles();
    }

public Color getCurrentColor() {
        int m = currentPiece.getMergeCount();
        if (m == 1)   return new Color(0xAA,0x00,0xAA);
        if (m == 2)   return Color.YELLOW;
        if (m == 3)   return Color.RED;
        if (m >= Tetris.MAX_MERGES) {
            boolean on = ((System.currentTimeMillis()/100)%2)==0;
            return on ? Color.RED : Color.WHITE;
        }
        return tetraminoColors.get(currentPiece.getShapeID());
    }

    /** draw only the ghost outline in *this* color */
    public void drawGhost(Graphics2D g, Color c) {
        Stroke old = g.getStroke();
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 120));
        g.setStroke(new BasicStroke(2));
        for (Point p : getGhostTiles()) {
            g.drawRect(p.x*board.getTileSize(),
                       p.y*board.getTileSize(),
                       board.getTileSize()-1,
                       board.getTileSize()-1);
        }
        g.setStroke(old);
    }

    /** draw only the filled blocks in *this* color */
    public void drawPiece(Graphics2D g, Color c) {
        for (Point p : currentPiece.getTiles()) {
            g.setColor(c);
            g.fillRect(p.x*board.getTileSize(),
                       p.y*board.getTileSize(),
                       board.getTileSize()-1,
                       board.getTileSize()-1);
        }
    }


    public List<Point> getCurrentTiles() {
        return currentPiece.getTiles();
    }

    public Tetramino getCurrentPiece() {
        return currentPiece;
    }

    public void setCurrentPiece(Tetramino t) {
        this.currentPiece = t;
    }

    public boolean hasHoldUsed() {
        return holdUsed;
    }

    private int computeScore(int lines) {
        switch (lines) {
            case 1:
                return 100;
            case 2:
                return 300;
            case 3:
                return 500;
            case 4:
                return 800;
            default:
                return 0;
        }
    }

    private void checkLose(List<Point> tiles) {
        int mid = board.getNumRows() / 2;
        for (Point p : tiles) {
            if (p.y == mid) {
                lost = true;
                return;
            }
        }
    }

    public void reset() {
        score = 0;
        lost = false;
        nextPieces.clear();
        newPiece();
    }

    public long getScore() {
        return score;
    }

    public int getPlayerId() {
        return playerId;
    }

    public List<Color> getTetraminoColors() {
        return tetraminoColors;
    }

    public boolean hasLost() {
        return lost;
    }
}
