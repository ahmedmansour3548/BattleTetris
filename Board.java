// Board.java

import java.awt.Point;
import java.util.List;

/**
 * Logical game board: tracks empty cells and fixed pieces.
 * Collision, piece-fixing, and line-clearing are per-player-half.
 */
public class Board {
    private final int width, height;
    private final Cell[][] cells;
    public static final int TILE_SIZE = 26;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[width][height];
        clear();
    }

    /** Initialize all cells to EMPTY. */
    public void clear() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y] = new Cell();
            }
        }
    }

    public Cell getCell(int x, int y) {
        return cells[x][y];
    }

    /**
     * True if any tile is out-of-bounds or on this player's fixed cells.
     */
    public boolean isCollision(List<Point> tiles, int playerId) {
        for (Point p : tiles) {
            int x = p.x, y = p.y;
            if (x < 0 || x >= width || y < 0 || y >= height) {
                return true;
            }
            Cell c = cells[x][y];
            if (c.getState() == Cell.State.FIXED && c.getOwnerId() == playerId) {
                return true;
            }
        }
        return false;
    }

    /** Stamp a piece in place as FIXED. */
    public void fixPiece(List<Point> tiles, int ownerId, int shapeId) {
        for (Point p : tiles) {
            Cell c = cells[p.x][p.y];
            c.setState(Cell.State.FIXED);
            c.setOwnerId(ownerId);
            c.setShapeId(shapeId);
        }
    }

    /**
     * Clear full lines in the relevant half and return the count.
     */
    public int clearLines(int ownerId) {
        int count = 0;
        int mid = height / 2;
        if (ownerId == 1) {
            // bottom half: rows mid..height-1
            for (int y = mid; y < height; y++) {
                if (isLineFull(y, ownerId)) {
                    clearRowDown(y, mid);
                    count++;
                }
            }
        } else {
            // top half: rows 0..mid-1
            for (int y = 0; y < mid; y++) {
                if (isLineFull(y, ownerId)) {
                    clearRowUp(y, mid);
                    count++;
                }
            }
        }
        return count;
    }

    private boolean isLineFull(int y, int ownerId) {
        for (int x = 0; x < width; x++) {
            Cell c = cells[x][y];
            if (c.getState() != Cell.State.FIXED || c.getOwnerId() != ownerId) {
                return false;
            }
        }
        return true;
    }

    /**
     * Shift rows [mid..row-1] down by one, clear row mid.
     */
    private void clearRowDown(int row, int mid) {
        for (int y = row; y > mid; y--) {
            for (int x = 0; x < width; x++) {
                cells[x][y] = cells[x][y - 1];
            }
        }
        for (int x = 0; x < width; x++) {
            cells[x][mid] = new Cell();
        }
    }

    /**
     * Shift rows [row+1..mid-1] up by one, clear row mid-1.
     */
    private void clearRowUp(int row, int mid) {
        for (int y = row; y < mid - 1; y++) {
            for (int x = 0; x < width; x++) {
                cells[x][y] = cells[x][y + 1];
            }
        }
        for (int x = 0; x < width; x++) {
            cells[x][mid - 1] = new Cell();
        }
    }

    public int getNumCols()  { return width; }
    public int getNumRows()  { return height; }
    public int getTileSize() { return TILE_SIZE; }
}
