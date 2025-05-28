package src;
// Board.java

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Logical game board: tracks empty cells and fixed pieces.
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

    public void fixPiece(List<Point> tiles, int ownerId, int shapeId, int mergeCount) {
        for (Point p : tiles) {
            Cell c = cells[p.x][p.y];
            c.setState(Cell.State.FIXED);
            c.setOwnerId(ownerId);
            c.setShapeId(shapeId);
            c.setMergeCount(mergeCount);    // store merge level
        }
    }


public int clearLines(int ownerId) {
        int mid = height / 2;
        int cleared = 0;

        if (ownerId == 1) {
            // bottom half: rows [mid .. height-1]
            List<Cell[]> keep = new ArrayList<>();
            // 1) collect non full rows
            for (int y = mid; y < height; y++) {
                if (isLineFull(y, ownerId)) {
                    cleared++;
                } else {
                    Cell[] row = new Cell[width];
                    for (int x = 0; x < width; x++)
                        row[x] = cells[x][y];
                    keep.add(row);
                }
            }
            // 2) rebuild from bottom up: first keep rows
            int yPtr = height - 1;
            for (int i = keep.size() - 1; i >= 0; i--, yPtr--) {
                System.arraycopy(keep.get(i), 0, cells, 0, 0); // placeholder
                for (int x = 0; x < width; x++)
                    cells[x][yPtr] = keep.get(i)[x];
            }
            // 3) fill the rest with new empty rows
            while (yPtr >= mid) {
                for (int x = 0; x < width; x++)
                    cells[x][yPtr] = new Cell();
                yPtr--;
            }

        } else {
            // top half: rows [0 .. mid-1]
            List<Cell[]> keep = new ArrayList<>();
            // 1) collect non full rows
            for (int y = 0; y < mid; y++) {
                if (isLineFull(y, ownerId)) {
                    cleared++;
                } else {
                    Cell[] row = new Cell[width];
                    for (int x = 0; x < width; x++)
                        row[x] = cells[x][y];
                    keep.add(row);
                }
            }
            // 2) rebuild from top down
            int yPtr = 0;
            for (Cell[] row : keep) {
                for (int x = 0; x < width; x++)
                    cells[x][yPtr] = row[x];
                yPtr++;
            }
            // 3) fill the rest up to mid
            while (yPtr < mid) {
                for (int x = 0; x < width; x++)
                    cells[x][yPtr] = new Cell();
                yPtr++;
            }
        }

        return cleared;
    }

    private boolean isLineFull(int y, int ownerId) {
        for (int x = 0; x < width; x++) {
            Cell c = cells[x][y];
            if (c.getState() != Cell.State.FIXED || c.getOwnerId() != ownerId)
                return false;
        }
        return true;
    }

    public int getNumCols()  { return width; }
    public int getNumRows()  { return height; }
    public int getTileSize() { return TILE_SIZE; }
}
