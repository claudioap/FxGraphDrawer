package random;

import java.io.Serializable;
import java.util.Objects;

/**
 * This is a sample possible implementation of a graph edge element
 *
 * @author Cláudio Pereira <cad.pereira@campus.fct.unl.pt>
 */
public class Path implements Serializable, Selectable {
    private String name;
    public final Point POINT1;
    public final Point POINT2;
    private boolean selected;

    /**
     * Builds a connection with the specified name, price, distance and type.
     *
     * @param name   Connection name, must be unique and not null nor empty.
     * @param point1 First point to connect.
     * @param point2 Second point to connect.
     */
    public Path(String name, Point point1, Point point2) {
        if (name == null || name.trim().equals("")) {
            throw new IllegalArgumentException("Invalid name!");
        }

        if (point1 == null || point2 == null) {
            throw new IllegalArgumentException("Received invalid point(s)");
        }

        if (point1 == point2) {
            throw new IllegalArgumentException("A path must have two diffent points");
        }

        this.name = name;
        this.POINT1 = point1;
        this.POINT2 = point2;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public void setSelected(boolean state) {
        this.selected = state;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Path other = (Path) obj;
        return Objects.equals(this.name, other.name);
    }

    @Override
    public String toString() {
        return this.name;
    }

}
