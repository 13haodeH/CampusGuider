
import java.util.Objects;

public class Location {
    private String name;
    private int x;
    private int y;
    private String imagePath;
    private int index;


    public Location(String name, int x, int y, String imagePath) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.imagePath = imagePath;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getImagePath() {
        return imagePath;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Location other = (Location) obj;
        return Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {

        this.index = index;
    }
    public String toString() {
        return name + " (" + x + ", " + y + ")";

    }


}
