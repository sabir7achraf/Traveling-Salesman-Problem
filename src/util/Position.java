package util;

public class Position {
    public double x;
    public double y;

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double distance(Position other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
