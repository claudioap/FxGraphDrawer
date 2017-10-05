package random;

/**
 * An interface which declares the the implementor is something which has a selection state
 *
 * @author Cláudio Pereira <cad.pereira@campus.fct.unl.pt>
 */
public interface Selectable {
    /**
     * Checks if the current element is selected
     *
     * @return selection state
     */
    boolean isSelected();

    /**
     * Changes the current element selection state
     *
     * @param state new selection state
     */
    void setSelected(boolean state);
}
