// Player.java

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A single Player class for BattleTetris.
 * Handles piece generation, movement, rotation with SRS, scoring, and drawing.
 */
public class Player {
    private final int playerId;
    private final Direction dropDirection;
    private final Point spawnPoint;
    private final Board board;
    private final Tetris game;

    private Tetramino currentPiece;
    private final List<Integer> nextPieces = new ArrayList<>();
    private long score = 0;
    private boolean lost = false;

    private final List<Color> tetraminoColors;
    private static final Color PASS_OVER_COLOR = Color.WHITE;

    // ─── SRS Wall-Kick Tables ──────────────────────────────────
    // I-Piece kicks: from orientation N to N+1 (cw)
    private static final int[][][] I_KICKS = {
        {{0,0},{-2,0},{1,0},{-2,-1},{1,2}},
        {{0,0},{-1,0},{2,0},{-1,2},{2,-1}},
        {{0,0},{2,0},{-1,0},{2,1},{-1,-2}},
        {{0,0},{1,0},{-2,0},{1,-2},{-2,1}}
    };

    // J,L,S,T,Z kicks
    private static final int[][][] JLSTZ_KICKS = {
        {{0,0},{-1,0},{-1,1},{0,-2},{-1,-2}},
        {{0,0},{1,0},{1,-1},{0,2},{1,2}},
        {{0,0},{1,0},{1,1},{0,-2},{1,-2}},
        {{0,0},{-1,0},{-1,-1},{0,2},{-1,2}}
    };

    public Player(int playerId, Direction dropDirection, Point spawnPoint, Board board, Tetris game) {
        this.playerId      = playerId;
        this.dropDirection = dropDirection;
        this.spawnPoint    = new Point(spawnPoint);
        this.board         = board;
        this.game          = game;

        // Choose color palette
        if (playerId == 1) {
            this.tetraminoColors = Arrays.asList(
                Color.decode("#FF0000"),
                Color.decode("#FF7F00"),
                Color.decode("#FFFF00"),
                Color.decode("#00FF00"),
                Color.decode("#00FFFF"),
                Color.decode("#0000FF"),
                Color.decode("#8B00FF")
            );
        } else {
            this.tetraminoColors = Arrays.asList(
                Color.decode("#FF6347"),
                Color.decode("#FF8C00"),
                Color.decode("#FFD700"),
                Color.decode("#32CD32"),
                Color.decode("#40E0D0"),
                Color.decode("#1E90FF"),
                Color.decode("#DA70D6")
            );
        }

        newPiece();
    }

    // ─── Piece Management ─────────────────────────────────────
    /** Spawn a new random piece. */
    public void newPiece() {
        if (nextPieces.isEmpty()) {
            Collections.addAll(nextPieces, 0,1,2,3,4,5,6);
            Collections.shuffle(nextPieces);
        }
        int shape = nextPieces.remove(0);
        currentPiece = new Tetramino();
        currentPiece.generateNewPiece(shape, new Point(spawnPoint));
    }

    /** Soft drop: move piece one step; fix & spawn new if collision. */
    public void drop() {
        Tetramino test = currentPiece.copy();
        test.dropPiece(dropDirection);
        if (board.isCollision(test.getTiles(), playerId)) {
            board.fixPiece(currentPiece.getTiles(), playerId, currentPiece.getShapeID());
            int cleared = board.clearLines(playerId);
            score += computeScore(cleared);
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
        }
    }

    /** Hard drop: instantly land piece at ghost position. */
public void hardDrop() {
    List<Point> landing = getGhostTiles();
    board.fixPiece(landing, playerId, currentPiece.getShapeID());
    int cleared = board.clearLines(playerId);
    score += computeScore(cleared);
    checkLose(landing);
    // --- NEW: notify game to slam ---
    game.onSlam(landing);
    newPiece();
}

    // ─── Rotation with SRS kicks ──────────────────────────────
    /** Rotate CW (+1) or CCW (-1) using SRS wall kicks. */
public void rotate(int delta) {
    int oldO = currentPiece.getOrientation();
    Tetramino test = currentPiece.copy();
    test.rotatePiece(delta);

    int shape = currentPiece.getShapeID();
    int newO    = (oldO + delta + 4) % 4;
    int[][] kicks;
    if (shape == 0)      kicks = I_KICKS[oldO];
    else if (shape == 1) kicks = new int[][]{{0,0}};     // O‑piece
    else                 kicks = JLSTZ_KICKS[oldO];

    for (int[] k : kicks) {
        // apply both x and y kick:
        test.shift(k[0], k[1]);
        if (!board.isCollision(test.getTiles(), playerId)) {
            currentPiece = test;
            return;
        }
        // revert
        test.shift(-k[0], -k[1]);
    }
    // all kicks failed ⇒ do nothing
}


    // ─── Ghost / Scoring / Lose ──────────────────────────────
    public List<Point> getGhostTiles() {
        Tetramino ghost = currentPiece.copy();
        while (true) {
            Tetramino next = ghost.copy();
            next.dropPiece(dropDirection);
            if (board.isCollision(next.getTiles(), playerId)) break;
            ghost = next;
        }
        return ghost.getTiles();
    }

    public void drawGhost(Graphics2D g) {
        Color base = tetraminoColors.get(currentPiece.getShapeID());
        Color ghost = new Color(base.getRed(), base.getGreen(), base.getBlue(), 120);
        g.setColor(ghost);
        for (Point p : getGhostTiles()) {
            g.drawRect(p.x*board.getTileSize(), p.y*board.getTileSize(),
                       board.getTileSize()-1, board.getTileSize()-1);
        }
    }

    public void drawPiece(Graphics2D g) {
        for (Point p : currentPiece.getTiles()) {
            Color col = tetraminoColors.get(currentPiece.getShapeID());
            Cell c;
            if (p.x>=0&&p.x<board.getNumCols()&&p.y>=0&&p.y<board.getNumRows()) {
                c = board.getCell(p.x,p.y);
                if (c.getState()==Cell.State.FIXED && c.getOwnerId()!=playerId) {
                    col = PASS_OVER_COLOR;
                }
            }
            g.setColor(col);
            g.fillRect(p.x*board.getTileSize(), p.y*board.getTileSize(),
                       board.getTileSize()-1, board.getTileSize()-1);
        }
    }

    private int computeScore(int lines) {
        switch (lines) {
            case 1: return 100;
            case 2: return 300;
            case 3: return 500;
            case 4: return 800;
            default: return 0;
        }
    }

    private void checkLose(List<Point> tiles) {
        int mid = board.getNumRows()/2;
        for (Point p : tiles) {
            if (p.y==mid) { lost = true; return; }
        }
    }

    public void reset() {
    score = 0;
    lost  = false;
    nextPieces.clear();
    newPiece();
}


    public long getScore()   { return score; }
    public int getPlayerId() { return playerId; }
    public List<Color> getTetraminoColors() { return tetraminoColors; }
    public boolean hasLost() { return lost; }
}
