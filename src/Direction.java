package src;
 

public enum Direction {
    UP (true),
    DOWN (false);
    private final boolean value;
    Direction(boolean newValue) {
        value = newValue;
    }
    public boolean getValue() { return value; }
}
