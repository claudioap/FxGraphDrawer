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

package testing;

import tads.SimpleGraph;
import random.Point;
import widget.FxMath;
import widget.GraphDrawer;
import javafx.geometry.Point2D;
import tads.Graph;
import org.junit.Before;
import org.junit.Test;

import static java.lang.Math.PI;
import static org.junit.Assert.assertEquals;

public class GraphDrawerTesting {
    public Graph twoPointConnectedGraph;

    @Before
    public void setUp() {

        SimpleGraph<Point, Integer> graph = new SimpleGraph<>();
        Point a = new Point("a");
        Point b = new Point("b");

        Graph.Vertex<Point> va = graph.addVertex(a);
        Graph.Vertex<Point> vb = graph.addVertex(b);
        graph.addEdge(va, vb, 1);
        twoPointConnectedGraph = graph;
    }


    /*
    * Tests assuming
    * SPRING_FORCE = 1
    * SPRING_SCALE = 1
    * REPULSION_SCALE = 5000
    * */
    @Test
    public void attractionTest() {
        Point2D point1 = new Point2D(
                239.46457081379975,
                239.46457081379975
        );
        Point2D point2 = new Point2D(
                239.46457081379975,
                518.3937124413992
        );
        Point2D result = FxMath.attractiveForce(
                point1, point2,
                4,
                GraphDrawer.SPRING_FORCE,
                GraphDrawer.SPRING_SCALE);
        assertEquals(result.getX(), 0, 0.01);
        assertEquals(result.getY(), 1.4077394442259785, 0.01);
    }

    @Test
    public void repulsionTest() {
        Point2D point1 = new Point2D(
                239.46457081379975,
                239.46457081379975
        );
        Point2D point2 = new Point2D(
                239.46457081379975,
                518.3937124413992
        );
        Point2D result = FxMath.repellingForce(point1, point2, GraphDrawer.REPULSION_SCALE);
        assertEquals(result.getX(), 0, 0.01);
        assertEquals(result.getY(), -0.06426614116556527, 0.01);
    }

    @Test
    public void mathTest() {
        Point2D point1 = new Point2D(
                0,
                0
        );
        Point2D point2 = new Point2D(
                2,
                2
        );
        Point2D middlePoint = FxMath.getMiddlePoint(point1, point2);
        assertEquals(middlePoint.getX(), 1, 0.0001);
        double angle = FxMath.getAngle(point1, point2);
        assertEquals(PI / 4, angle, 0.1);
    }

    @Test
    public void pointPlacementTest() {
        Point2D point1 = new Point2D(
                2,
                3
        );
        Point2D point2 = new Point2D(
                1,
                2
        );

        double angle = FxMath.getReciprocalAngle(point1, point2);
        Point2D middle = FxMath.getMiddlePoint(point1, point2);
        Point2D controlPoint = FxMath.shiftPoint(middle, angle, -1);
        assertEquals(0.7929, controlPoint.getX(), 0.0001);
        assertEquals(3.2071, controlPoint.getY(), 0.0001);
    }
}
