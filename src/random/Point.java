package random;

import java.io.Serializable;

/**
 * This is a sample possible implementation of a graph vertex element
 *
 * @author Cláudio Pereira <cad.pereira@campus.fct.unl.pt>
 */
public class Point implements Serializable, Selectable {

    private String id;
    private boolean selected;

    /**
     * Builds a point with an id.
     *
     * @param id Point id.
     */
    public Point(String id) {
        if (id == null || id.trim().equals("")) {
            throw new IllegalArgumentException("Id can't be null or empty");
        }
        this.id = id;
        this.selected = false;
    }

    @Override
    public boolean isSelected() {
        return this.selected;
    }

    @Override
    public void setSelected(boolean state) {
        this.selected = state;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
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
        final Point other = (Point) obj;
        return this.id.equals(other.id);
    }

}
