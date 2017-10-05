/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package widget;

import javafx.geometry.Point2D;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Mathematical operations used for drawing
 *
 * @author Cláudio Pereira <cad.pereira@campus.fct.unl.pt>
 * @author José Pereira <jcpereira.dev@gmail.com>
 */
public final class FxMath {

    //to avoid complications when nodes randomly spawn on top of each other
    private static final double stabilizer1 = 1;
    private static final double stabilizer2 = 1;

    /**
     * Computes the connecting vector between node1 and node2.
     *
     * @param node1 Point of the first node.
     * @param node2 Point of the second node.
     * @return The connecting vector between node1 and node2.
     */
    static Point2D computeVector(Point2D node1, Point2D node2) {
        return new Point2D(
                node2.getX() - node1.getX(),
                node2.getY() - node1.getY()
        );
    }

    /**
     * Computes the normalized vector to a vector given by x and y.
     *
     * @param x X coordinate of the vector.
     * @param y Y coordinate of the vector.
     * @return Normalized vector.
     */
    static Point2D normalizeVector(double x, double y) {
        double length = Math.sqrt(
                Math.pow(Math.abs(x), 2) + Math.pow(Math.abs(y), 2)
        );
        return new Point2D(
                x / length,
                y / length);
    }

    /**
     * Computes the vector of the attractive force between two nodes.
     *
     * @param from Coordinates of the first node.
     * @param to   Coordinates of the second node.
     * @return Force vector.
     */
    public static Point2D attractiveForce(Point2D from, Point2D to, int numVertices, double force, double scale) {
        Point2D vec;
        double distance = getDistance(from, to);
        vec = computeVector(from, to);
        vec = normalizeVector(vec.getX(), vec.getY());
        double factor = attractiveFunction(distance, numVertices, force, scale);
        vec = new Point2D(
                vec.getX() * factor,
                vec.getY() * factor);
        return vec;
    }

    /**
     * Computes the value of the scalar attractive force function based on
     * the given distance of a group of nodes.
     *
     * @param distance    The distance between the two nodes.
     * @param numVertices Amount of vertices.
     * @return Attractive force.
     */
    static double attractiveFunction(double distance, int numVertices, double force, double scale) {
        if (distance < stabilizer1) {
            distance = stabilizer1;
        }
        return force * Math.log(distance / scale) * (1 / (stabilizer2 * numVertices));
    }

    /**
     * Computes the vector of the repelling force between two node.
     *
     * @param from Point of the first node.
     * @param to   Point of the second node.
     * @return Force point.
     */
    public static Point2D repellingForce(Point2D from, Point2D to, double scale) {
        Point2D vec;
        double distance = getDistance(from, to);
        vec = computeVector(from, to);
        vec = normalizeVector(vec.getX(), vec.getY());
        double factor = -repellingFunction(distance, scale);
        vec = new Point2D(
                vec.getX() * factor,
                vec.getY() * factor);
        return vec;
    }

    /**
     * Computes the value of the scalar repelling force function based on
     * the given distance of two nodes.
     *
     * @param distance The distance between the two nodes.
     * @return Repelling force.
     */
    static double repellingFunction(double distance, double scale) {
        if (distance < stabilizer1) {
            distance = stabilizer1;
        }
        return scale / Math.pow(distance, 2);
    }

    /**
     * Computes the Euclidean distance between two given nodes.
     *
     * @param node1 Point for the first node.
     * @param node2 Point of the second node.
     * @return Euclidean distance between the nodes.
     */
    static double getDistance(Point2D node1, Point2D node2) {
        return Math.sqrt(Math.pow(Math.abs(node1.getX() - node2.getX()), 2)
                + Math.pow(Math.abs(node1.getY() - node2.getY()), 2));
    }

    /**
     * Obtains the point in the middle of two reference points
     *
     * @param point1 first point
     * @param point2 second point
     * @return middle point
     */
    public static Point2D getMiddlePoint(Point2D point1, Point2D point2) {
        return new Point2D(
                (point1.getX() + point2.getX()) / 2,
                (point1.getY() + point2.getY()) / 2

        );
    }

    /**
     * Obtains the shorter angle between two points
     *
     * @param point1 first point
     * @param point2 second point
     * @return angle
     */
    public static double getAngle(Point2D point1, Point2D point2) {
        double deltaX = (point1.getX() - point2.getX());
        double deltaY = (point1.getY() - point2.getY());
        return Math.atan(deltaY / deltaX);
    }

    /**
     * Obtains the reciprocal angle between two points
     *
     * @param point1 first point
     * @param point2 second point
     * @return reciprocal angle
     */
    public static double getReciprocalAngle(Point2D point1, Point2D point2) {
        return getAngle(point1, point2) - Math.PI / 2;
    }

    /**
     * Shifts a point with polar coordinates
     *
     * @param point original point
     * @param angle polar angle
     * @param shift vector modulus
     * @return shifted point
     */
    public static Point2D shiftPoint(Point2D point, double angle, double shift) {
        return new Point2D(
                point.getX() + cos(angle) * shift,
                point.getY() + sin(angle) * shift);
    }
}