

import java.awt.geom.Point2D;
import java.util.Objects;

class Point {
    int x;
    int y;
    String label;
    private int index;

    Point(int x, int y, String label,int index) {
        this.x = x;
        this.y = y;
        this.label = label;
        this.index = index;
    }


    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return  label ;
    }

    public void setIndex(int index) {
        this.index = index;
    }


    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;

        if (x != point.x) return false;
        if (y != point.y) return false;
        if (index != point.index) return false;
        return Objects.equals(label, point.label);
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + index;
        return result;
    }
}
