package src;
// Cell.java

/**
 * Represents one cell on the BattleTetris board.
 * Tracks whether it's empty, a wall, or occupied by a fixed piece,
 * and if fixed, which player owns it and which shape.
 */
public class Cell {
    public enum State { EMPTY, FIXED, WALL }

    private State state;
    private int ownerId;   // 0 = none, 1 or 2 = player
    private int shapeId;   // 0–6 = Tetramino shape, or -1 if none
private int mergeCount = 0;
    /** Default: empty, no owner, no shape. */
    public Cell() {
        this.state   = State.EMPTY;
        this.ownerId = 0;
        this.shapeId = -1;
    }

    /**
     * Full constructor.
     * @param state    EMPTY, FIXED, or WALL
     * @param ownerId  which player (1 or 2) owns this fixed cell; 0 for none
     * @param shapeId  shape index 0–6 if FIXED; otherwise ignored
     */
    public Cell(State state, int ownerId, int shapeId) {
        this.state   = state;
        this.ownerId = ownerId;
        this.shapeId = shapeId;
    }

    // ─── Getters & Setters ──────────────────────────────────────

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public int getShapeId() {
        return shapeId;
    }

    public void setShapeId(int shapeId) {
        this.shapeId = shapeId;
    }

    public int getMergeCount() { return mergeCount; }
    public void setMergeCount(int m) { this.mergeCount = m; }
}
