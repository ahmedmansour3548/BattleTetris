package src;
// Tetramino.java

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Tetris piece: shape ID, orientation, tiles, and pivot.
 */
public class Tetramino {
    private List<Point> tiles; // absolute board positions
    private Point pivot; // absolute rotation pivot
    private int shapeID; // 0=I,1=O,2=T,3=J,4=L,5=S,6=Z
    private int orientation; // 0–3
    private int mergeCount = 0;
    // Spawn‑orientation offsets for each shape
    private static final Point[][] OFFSETS = {
            { new Point(0, -2), new Point(0, -1), new Point(0, 0), new Point(0, 1) }, // I
            { new Point(0, 0), new Point(1, 0), new Point(0, 1), new Point(1, 1) }, // O
            { new Point(0, -1), new Point(-1, 0), new Point(0, 0), new Point(1, 0) }, // T
            { new Point(-1, -1), new Point(-1, 0), new Point(0, 0), new Point(1, 0) }, // J
            { new Point(1, -1), new Point(-1, 0), new Point(0, 0), new Point(1, 0) }, // L
            { new Point(0, -1), new Point(1, -1), new Point(-1, 0), new Point(0, 0) }, // S
            { new Point(-1, -1), new Point(0, -1), new Point(0, 0), new Point(1, 0) } // Z
    };

    // Pivot offsets in spawn orientation (all zero here)
    private static final Point[] PIVOTS = {
            new Point(0, 0), // I
            new Point(0, 0), // O
            new Point(0, 0), // T
            new Point(0, 0), // J
            new Point(0, 0), // L
            new Point(0, 0), // S
            new Point(0, 0) // Z
    };

    public Tetramino() {
        tiles = new ArrayList<>(4);
    }

    /** Place a new piece of the given shape at spawnPoint. */
    public void generateNewPiece(int shapeID, Point spawnPoint) {
        this.shapeID = shapeID;
        this.orientation = 0;
        tiles.clear();

        // compute absolute pivot
        Point po = PIVOTS[shapeID];
        pivot = new Point(spawnPoint.x + po.x,
                spawnPoint.y + po.y);

        // place all tiles
        for (Point off : OFFSETS[shapeID]) {
            tiles.add(new Point(spawnPoint.x + off.x,
                    spawnPoint.y + off.y));
        }
    }

    /**
     * Rotate one step: delta>0 = CW, delta<0 = CCW.
     * All shapes rotate geometrically.
     * I‑piece gets an extra X‑shift when entering vertical (orient 2 or 0).
     */
    public void rotatePiece(int delta) {
        // quare: no rotation, no shift
        if (shapeID == 1)
            return;
        boolean cw = delta > 0;

        // geometric rotation around pivot
        for (Point t : tiles) {
            int rx = t.x - pivot.x;
            int ry = t.y - pivot.y;
            int nx = cw ? -ry : ry;
            int ny = cw ? rx : -rx;
            t.x = pivot.x + nx;
            t.y = pivot.y + ny;
        }

        // update orientation 0..3
        orientation = (orientation + delta + 4) % 4;

        // special I‑piece shift: only when new orientation is vertical
        if (shapeID == 0 && orientation % 2 == 0) {
            // orient==2 (vertical inverted): shift +1
            // orient==0 (vertical spawn): shift -1 from prior horiz
            int shift = (orientation == 2) ? +1 : -1;
            translate(shift, 0);
        }
    }

    /** Soft‑drop one step (down or up). */
    public void dropPiece(Direction dir) {
        int dy = dir == Direction.DOWN ? 1 : -1;
        translate(0, dy);
    }

    /** Move horizontally by dx and also move the pivot. */
    public void movePiece(int dx) {
        translate(dx, 0);
    }

    /** Translate all tiles and pivot by (dx,dy). */
    private void translate(int dx, int dy) {
        for (Point t : tiles) {
            t.x += dx;
            t.y += dy;
        }
        pivot.x += dx;
        pivot.y += dy;
    }

    /** Deep‑copy this Tetramino (including pivot and orientation). */
    public Tetramino copy() {
        Tetramino c = new Tetramino();
        c.shapeID = this.shapeID;
        c.orientation = this.orientation;
        c.pivot = new Point(this.pivot);
        c.mergeCount = this.mergeCount;
        for (Point t : this.tiles) {
            c.tiles.add(new Point(t));
        }
        return c;
    }

    /**
     * Shift the piece by (dx,dy) in board coordinates (also moves the pivot).
     */
    public void shift(int dx, int dy) {
        // reuse your internal translate
        translate(dx, dy);
    }

    /** Clear out the current tile list. */
    public void clearTiles() {
        tiles.clear();
    }

    /** Add a single tile (absolute coord). */
    public void addTile(Point p) {
        tiles.add(p);
    }

    /** Force set the pivot (for next rotations). */
    public void setPivot(Point p) {
        this.pivot = p;
    }

    public int getMergeCount() {
        return mergeCount;
    }

    public void setMergeCount(int count) {
        this.mergeCount = count;
    }

    // ─── Accessors ────────────────────────────────────────────

    public List<Point> getTiles() {
        return tiles;
    }

    public int getShapeID() {
        return shapeID;
    }

    public int getOrientation() {
        return orientation;
    }

}
