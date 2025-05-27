 

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;

public enum WackyTetramino {
    I(new ArrayList<>(Arrays.asList(
        new ArrayList<>(Arrays.asList(new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1))),
        new ArrayList<>(Arrays.asList(new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3))),
        new ArrayList<>(Arrays.asList(new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1))),
        new ArrayList<>(Arrays.asList(new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3)))
    ))),

    J(new ArrayList<>(Arrays.asList(
        new ArrayList<>(Arrays.asList(new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 0))),
        new ArrayList<>(Arrays.asList(new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 2))),
        new ArrayList<>(Arrays.asList(new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 2))),
        new ArrayList<>(Arrays.asList(new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 0)))
    ))),

    L(new ArrayList<>(Arrays.asList(
        new ArrayList<>(Arrays.asList(new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 2))),
        new ArrayList<>(Arrays.asList(new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 2))),
        new ArrayList<>(Arrays.asList(new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 0))),
        new ArrayList<>(Arrays.asList(new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 0)))
    ))),

    O(new ArrayList<>(Arrays.asList(
        new ArrayList<>(Arrays.asList(new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1))),
        new ArrayList<>(Arrays.asList(new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1))),
        new ArrayList<>(Arrays.asList(new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1))),
        new ArrayList<>(Arrays.asList(new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1)))
    ))),

    S(new ArrayList<>(Arrays.asList(
        new ArrayList<>(Arrays.asList(new Point(1, 0), new Point(2, 0), new Point(0, 1), new Point(1, 1))),
        new ArrayList<>(Arrays.asList(new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2))),
        new ArrayList<>(Arrays.asList(new Point(1, 0), new Point(2, 0), new Point(0, 1), new Point(1, 1))),
        new ArrayList<>(Arrays.asList(new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2)))
    ))),

    T(new ArrayList<>(Arrays.asList(
        new ArrayList<>(Arrays.asList(new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1))),
        new ArrayList<>(Arrays.asList(new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2))),
        new ArrayList<>(Arrays.asList(new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(1, 2))),
        new ArrayList<>(Arrays.asList(new Point(1, 0), new Point(1, 1), new Point(2, 1), new Point(1, 2)))
    ))),

    Z(new ArrayList<>(Arrays.asList(
        new ArrayList<>(Arrays.asList(new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1))),
        new ArrayList<>(Arrays.asList(new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(0, 2))),
        new ArrayList<>(Arrays.asList(new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1))),
        new ArrayList<>(Arrays.asList(new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(0, 2)))
    )));

    private final ArrayList<ArrayList<Point>> rotations;

    public ArrayList<Point> additional;

    WackyTetramino(ArrayList<ArrayList<Point>> rotations) {
        this.rotations = rotations;
    }

    public ArrayList<ArrayList<Point>> orientations() {
        return rotations;
    }

    public ArrayList<Point> additionalTiles() {
        return additional;
    }

    public void addTiles(ArrayList<Point> pieces, int collisionX, int collisionY) {
        for (Point p : pieces) {
            additional.add(new Point(p.x + collisionX, p.y + collisionY));
        }
    }

    public void clearAdditional() {
        additional.clear();
    }
}
