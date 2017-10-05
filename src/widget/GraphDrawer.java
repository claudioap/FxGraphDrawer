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

import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import random.Selectable;
import tads.Graph;
import tads.Graph.Edge;
import tads.Graph.Vertex;

import java.time.LocalTime;
import java.util.*;
import java.util.function.Consumer;

/**
 * This widget will draw a graphical representation of an object implementing a graph interface.
 * The used implementation is the Force Directed approach, which is based on repulsion and attraction between nodes.
 * Nodes are cursor aware, being draggable and clickable.
 * The nodes will have different colors depending on the amount of edges connecting them.
 *
 * @param <V> Node data type
 * @param <E> Edge data type
 * @author Cláudio Pereira <cad.pereira@campus.fct.unl.pt>
 * @author José Pereira <jcpereira.dev@gmail.com>
 */
@SuppressWarnings("Duplicates")
public class GraphDrawer<V extends Selectable, E extends Selectable> extends AnchorPane {

    private Graph<V, E> graph;
    private Map<Vertex<V>, Point2D> nodeLocations = new HashMap<>();    // R² coordinates for each node
    // Node force vector for the current iteration
    private Map<Vertex<V>, Point2D> nodeTotalForce = new HashMap<>();

    public static final double SPRING_FORCE = 1;        //force = SPRING_FORCE*Math.log(distance/SPRING_SCALE);
    public static final double SPRING_SCALE = 1;
    public static final double REPULSION_SCALE = 5000;  //repulsion distance = REPULSION_SCALE/distance²;
    public static final double ANIMATION_SPEED = 1;

    private int minDegree = Integer.MAX_VALUE;
    private int maxDegree = 0;
    private int numVertices = 0;

    private Canvas canvas = new Canvas();
    private GraphicsContext gc = canvas.getGraphicsContext2D();

    private double canvasWidth = 500;
    private double canvasHeight = 500;

    private final Color NODE_COLOR = Color.web("hsl(120,100%,100%)");
    private final Color EDGE_COLOR = Color.WHITE;
    private final Color EDGE_BORDER_COLOR = Color.BLACK;
    private final Color TEXT_COLOR = Color.WHITE;
    private final Color TEXT_BORDER_COLOR = Color.BLACK;
    private final Color NODE_BORDER_COLOR = Color.BLACK;

    private final Color SELECTED_EDGE_COLOR = Color.RED;
    private final Color SELECTED_TEXT_COLOR = Color.RED;
    private final Color SELECTED_NODE_BORDER_COLOR = Color.RED;

    private double defaultNodeSize = 20;           //diameter, pixels
    private double nodeSize = defaultNodeSize;
    private final double NODE_DEGREE_SCALER = 5;   //extra node size per node degrees
    private boolean renderNodeBorders = true;
    private double nodeBorderSize = 2;             //pixels

    private AnimationTimer timer;                  //animation timer draws graph and executes simulation steps
    private double shiftX = 0;                     //initial displacement of the display on the canvas in x-direction
    private double shiftY = 0;                     //initial displacement of the display on the canvas in y-direction
    private double zoom = 1;                       //initial zoom factor (1=100%,0.5=50%,2=200%); zoom > 0
    private double stepsPerFrame = 20;             //simulation steps computed per rendered frame
    private boolean autoZoom = true;               //automatically fits every node in the canvas
    private final double PADDING_FACTOR = 0.2;     //ratio of the padding area on the canvas in auto zoom TYPE

    private boolean renderEdges = true;            //toggle for the rendering of edges

    private boolean graphDrawn = false;            //control whether the current graph frame has been completely drawn
    private boolean simulationActive = false;
    private boolean started = true;                //contains application state for tooltip logic

    private Vertex<V> draggedNode = null;          //Node currently being dragged
    private Vertex<V> tooltipNode = null;          //Node having the tooltip being drawn

    private double cursorPressedX = 0;              //Coordinates of the last mouse press event
    private double cursorPressedY = 0;

    private double shiftXBuffer = 0;               //buffers the x-shift between mouse pressed and dragged events
    private double shiftYBuffer = 0;               //buffers the y-shift between mouse pressed and dragged events

    private Map<Vertex<V>, Double> nodeColors = new HashMap<>();    //contains node colors

    private Consumer<Vertex<V>> clickConsumer = null;

    // Performance helpers
    private final boolean DISPLAY_FPS = false;
    private int frames = 0;
    private int frameDrawTime = 0; //Last frame timestamp


    private Set<Set<Edge<E, V>>> edgeSpotsCache = new HashSet<>();

    /**
     * Builds the GraphDrawer with his default values.
     */
    public GraphDrawer() {
        setHeight(canvasHeight);
        setWidth(canvasWidth);
        getChildren().addAll(canvas);
        setTopAnchor(canvas, 0.0);
        setLeftAnchor(canvas, 0.0);
        initMouseEvents();
        timer = new AnimationTimer() {

            @Override
            public void handle(long now) {
                if (simulationActive) {
                    for (int i = 0; i < stepsPerFrame; i++) {
                        simulateSingleStep(draggedNode);
                    }
                }
                renderGraph();
            }
        };
    }

    /**
     * Builds the GraphDrawer for a graph.
     *
     * @param graph Graph to draw.
     */
    public GraphDrawer(Graph<V, E> graph) {
        this();
        setGraph(graph);
    }

    /**
     * Sets a graph to draw.
     *
     * @param graph Graph to draw.
     */
    public void setGraph(Graph<V, E> graph) {
        nodeLocations.clear();
        nodeTotalForce.clear();
        if (graph != null) {
            this.graph = graph;
            numVertices = graph.vertexCount();
            generateInitialSpawns(canvasWidth, canvasHeight,
                    PADDING_FACTOR * canvasWidth,
                    PADDING_FACTOR * canvasHeight);
            cacheVertexEdges();
            computeExtremeDegrees();
        } else {
            stopAnimation();
            this.graph = null;
        }
    }

    /**
     * Resize the drawing canvas.
     *
     * @param width  X value.
     * @param height Y value.
     */
    public void resize(double width, double height) {
        this.canvasWidth = width;
        this.canvasHeight = height;
        this.setPrefSize(width, height);
        this.setMaxSize(width, height);
        this.setWidth(width);
        canvas.setWidth(width);
        this.setHeight(height);
        canvas.setHeight(height);
    }

    /**
     * Generates initial spawn locations for all nodes with an area constrained
     * by the areas boundaries and the padding.
     *
     * @param xBoundary x boundary of the drawing area in the model space.
     * @param yBoundary y boundary of the drawing area in the model space.
     * @param xPadding  x padding.
     * @param yPadding  y padding.
     */
    private void generateInitialSpawns(double xBoundary, double yBoundary, double xPadding, double yPadding) {
        for (Vertex<V> vertex : graph.vertices()) {
            nodeLocations.put(
                    vertex,
                    new Point2D(
                            Math.random()
                                    * (Math.pow(numVertices, .3)
                                    * xBoundary - 2 * xPadding)
                                    + xPadding,
                            Math.random() * (Math.pow(numVertices, .3)
                                    * yBoundary - 2 * yPadding)
                                    + yPadding
                    )
            );
        }
    }

    /**
     * Changes a node location location.
     *
     * @param node     ID of the node.
     * @param location New location.
     */
    private void setNodeLocation(Vertex<V> node, Point2D location) {
        nodeLocations.replace(node, location);
    }

    /**
     * Computes the forces that are applied to every node by looping all node maps.
     */
    private void computeForces() {
        for (Map.Entry<Vertex<V>, Point2D> outerNode : nodeLocations.entrySet()) {
            for (Map.Entry<Vertex<V>, Point2D> innerNode : nodeLocations.entrySet()) {
                if (outerNode.getKey() == innerNode.getKey()) {
                    continue;
                }
                Point2D repellingForce = FxMath.repellingForce(outerNode.getValue(), innerNode.getValue(), REPULSION_SCALE);
                Point2D outerNodeForce = nodeTotalForce.get(outerNode.getKey());
                if (graph.areAdjacent(outerNode.getKey(), innerNode.getKey())) {
                    Point2D attractiveForce = FxMath.attractiveForce(outerNode.getValue(), innerNode.getValue(), numVertices, SPRING_FORCE, SPRING_SCALE);

                    nodeTotalForce.replace(outerNode.getKey(), new Point2D(
                            outerNodeForce.getX() + attractiveForce.getX() + repellingForce.getX(),
                            outerNodeForce.getY() + attractiveForce.getY() + repellingForce.getY()
                    ));
                } else {
                    nodeTotalForce.replace(outerNode.getKey(), new Point2D(
                            outerNodeForce.getX() + repellingForce.getX(),
                            outerNodeForce.getY() + repellingForce.getY()
                    ));
                }
            }
        }
    }

    /**
     * Applies computed forces.
     *
     * @param ignoreNode Optional ID of a node to be ignored (eg. being dragged)
     */
    private void applyForce(Vertex<V> ignoreNode) {
        Set<Map.Entry<Vertex<V>, Point2D>> entries = nodeLocations.entrySet();
        for (Map.Entry<Vertex<V>, Point2D> node : entries) {
            Vertex<V> currentNode = node.getKey();
            if (currentNode == ignoreNode) {
                continue;
            }
            Point2D point = node.getValue();
            Point2D forceVector = nodeTotalForce.get(currentNode);
            node.setValue(new Point2D(
                    point.getX() + ANIMATION_SPEED * forceVector.getX(),
                    point.getY() + ANIMATION_SPEED * forceVector.getY()
            ));
        }
    }

    /**
     * Computes a box around the graph that contains all of its nodes.
     *
     * @return xMin, yMin, xMax, yMax (upper left and lower right corners of the box).
     */
    private double[] getBoundaries() {
        double xMin, xMax, yMin, yMax;
        xMin = yMin = Double.MAX_VALUE;
        xMax = yMax = Double.MIN_VALUE;
        for (Point2D position : nodeLocations.values()) {
            double x = position.getX();
            double y = position.getY();
            xMin = (x < xMin) ? x : xMin;
            yMin = (y < yMin) ? y : yMin;
            xMax = (x > xMax) ? x : xMax;
            yMax = (y > yMax) ? y : yMax;
        }

        return new double[]{xMin, yMin, xMax, yMax};
    }

    /**
     * Computes the maximum and minimum degree of the graph vertices.
     */
    private void computeExtremeDegrees() {
        for (Vertex<V> vertex : graph.vertices()) {
            int degree = graph.vertexDegree(vertex);
            if (degree < minDegree) {
                minDegree = degree;
            }
            if (degree > maxDegree) {
                maxDegree = degree;
            }
        }
    }

    /**
     * Applies a simulation step
     *
     * @param ignoreNode Optional ID of a node to be ignored (eg. being dragged)
     */
    public void simulateSingleStep(Vertex<V> ignoreNode) {
        initForces();
        computeForces();
        applyForce(ignoreNode);
    }

    /**
     * Resets node forces
     */
    private void initForces() {
        nodeLocations.forEach((vert, vect) -> nodeTotalForce.put(vert, new Point2D(0, 0)));
    }

    /**
     * Advances the simulation a determined amount of steps.
     *
     * @param steps Amount of steps to advance.
     */
    public void advanceSteps(int steps) {
        for (int i = 0; i < steps; i++) {
            simulateSingleStep(draggedNode);
        }
    }

    /**
     * Adds listeners to the canvas for supported mouse events
     */
    private void initMouseEvents() {
        setOnMouseDragged(event -> {
            if (draggedNode != null) { // drag node
                Point2D location = drawingSpaceToCoordinateSpace(event.getX(), event.getY());
                setNodeLocation(draggedNode, location);
            } else {
                shiftX = shiftXBuffer + (event.getX() - cursorPressedX);
                shiftY = shiftYBuffer + (event.getY() - cursorPressedY);
            }

        });

        setOnMousePressed(event -> {
            draggedNode = checkForMouseNodeCollision(event.getX(), event.getY());
            if (draggedNode == null) {
                cursorPressedX = event.getX();
                cursorPressedY = event.getY();
                shiftXBuffer = shiftX;
                shiftYBuffer = shiftY;
            }
        });

        setOnMouseReleased(event -> draggedNode = null);

        setOnMouseMoved(event -> {
            tooltipNode = checkForMouseNodeCollision(event.getX(), event.getY());
            if (tooltipNode != null) {
                this.getScene().setCursor(Cursor.HAND);
            } else {
                this.getScene().setCursor(Cursor.DEFAULT);
            }
        });

        setOnMouseClicked(event -> {
            Vertex<V> node = checkForMouseNodeCollision(event.getX(), event.getY());
            if (clickConsumer != null && node != null) {
                clickConsumer.accept(node);
            }
        });
    }

    /**
     * Sets the lambda consumer to be triggered on click.
     *
     * @param consumer Consumer.
     */
    public void setClickAction(Consumer<Vertex<V>> consumer) {
        this.clickConsumer = consumer;
    }

    /**
     * Starts the simulation behind the animation
     */
    public void startAnimation() {
        simulationActive = true;
        timer.start();
    }

    /**
     * Stops the simulation behind the animation.
     */
    public void stopAnimation() {
        simulationActive = false;
        timer.stop();
    }

    /**
     * Initiates the rendering procedure for the entire graph including background, nodes and edges.
     */
    public void renderGraph() {
        gc.clearRect(0, 0, this.getWidth(), this.getHeight());
        if (autoZoom) {
            fitContent();
        }
        if (started) {
            if (graph != null) {
                drawEdges();
                drawNodes();
                graphDrawn = true;
            } else {
                graphDrawn = false;
            }
        }
        if (DISPLAY_FPS) {
            int currentSecond = LocalTime.now().getSecond();
            if (frameDrawTime != currentSecond) {
                frameDrawTime = currentSecond;
                System.out.println(frames + " FPS");
                frames = 0;
            }
            frames++;
        }
    }

    /**
     * Draws all graph nodes(vertices) into the graphics context.
     * Also, computes the size of any given node by its degree and sets its color.
     */
    private void drawNodes() {
        gc.setFill(NODE_COLOR);
        for (Vertex<V> vertex : graph.vertices()) {
            if (maxDegree != minDegree) {
                if (!nodeColors.containsKey(vertex)) {
                    nodeColors.put(vertex, Math.random() * 360);
                }
                gc.setFill(Color.web("hsl(" + nodeColors.get(vertex) + ",100%,100%)"));
                drawSingleNode(
                        nodeLocations.get(vertex),
                        vertex.element().toString(),
                        nodeSize + graph.vertexDegree(vertex) * NODE_DEGREE_SCALER,
                        vertex.element().isSelected());
            } else {
                drawSingleNode(
                        nodeLocations.get(vertex),
                        vertex.element().toString(),
                        nodeSize,
                        vertex.element().isSelected());
            }
        }
    }

    /**
     * Computes the hitbox of a node.
     * The size of the hitbox depends both on the screen position of the node and its size.
     *
     * @param node Node to be computed
     * @return Vector with node hitbox
     */
    private Vector<Double> nodeHitbox(Vertex<V> node) {
        double size;
        if (minDegree != maxDegree) {
            size = nodeSize + graph.vertexDegree(node);
        } else {
            size = nodeSize;
        }
        Point2D location = nodeLocations.get(node);
        double x = location.getX();
        double y = location.getY();
        Vector<Double> vec = new Vector<>(4);
        if (renderNodeBorders) {
            vec.add(x * zoom + shiftX - (size + nodeBorderSize) / 2);
            vec.add(y * zoom + shiftY - (size + nodeBorderSize) / 2);
            vec.add(x * zoom + shiftX + (size + nodeBorderSize) / 2);
            vec.add(y * zoom + shiftY + (size + nodeBorderSize) / 2);
        } else {
            vec.add(x * zoom + shiftX - size / 2);
            vec.add(y * zoom + shiftY - size / 2);
            vec.add(x * zoom + shiftX + size / 2);
            vec.add(y * zoom + shiftY + size / 2);
        }
        return vec;
    }


    /**
     * Sets the drawer placement(both shift and zoom) to values that fit the content
     */
    private void fitContent() {
        double[] vec;
        if (numVertices > 1) {
            vec = getBoundaries();
            zoom = Math.min(
                    (1 - PADDING_FACTOR) * canvasWidth / (vec[2] - vec[0]),
                    (1 - PADDING_FACTOR) * canvasHeight / (vec[3] - vec[1]));
            shiftX = canvasWidth / 2 - zoom * (vec[0] + vec[2]) / 2;
            shiftY = canvasHeight / 2 - zoom * (vec[1] + vec[3]) / 2;
        }
    }

    /**
     * Draws graph edges onto the canvas
     */
    private void drawEdges() {
        if (!renderEdges)
            return;
        for (Set<Edge<E, V>> edgeSpot : edgeSpotsCache) {
            //If there is only one edge between two points
            if (edgeSpot.size() == 1) {
                Edge<E, V> edge = edgeSpot.iterator().next();
                Point2D to = nodeLocations.get(edge.vertices()[0]);
                Point2D from = nodeLocations.get(edge.vertices()[1]);
                gc.save();
                //Draw edge
                gc.setLineWidth(4);
                gc.setLineDashes(30);
                gc.setStroke(EDGE_BORDER_COLOR);
                gc.strokeLine(
                        to.getX() * zoom + shiftX,
                        to.getY() * zoom + shiftY,
                        from.getX() * zoom + shiftX,
                        from.getY() * zoom + shiftY
                );

                gc.setLineWidth(2);
                if (edge.element().isSelected()) {
                    gc.setStroke(SELECTED_EDGE_COLOR);
                } else {
                    gc.setStroke(EDGE_COLOR);
                }
                gc.strokeLine(
                        to.getX() * zoom + shiftX,
                        to.getY() * zoom + shiftY,
                        from.getX() * zoom + shiftX,
                        from.getY() * zoom + shiftY
                );

                //Draw edge text
                if (edge.element().isSelected()) {
                    gc.setFill(SELECTED_TEXT_COLOR);
                } else {
                    gc.setFill(TEXT_COLOR);
                }
                gc.setFont(new Font(20));
                gc.setStroke(TEXT_BORDER_COLOR);
                gc.setLineDashes(0);
                gc.setLineWidth(2);
                Point2D textLocation = FxMath.getMiddlePoint(from, to);
                gc.strokeText(
                        edge.element().toString(),
                        textLocation.getX() * zoom + shiftX + 10,
                        textLocation.getY() * zoom + shiftY);
                gc.fillText(
                        edge.element().toString(),
                        textLocation.getX() * zoom + shiftX + 10,
                        textLocation.getY() * zoom + shiftY);
                gc.restore();
                //In case there are multiple edges
            } else {
                int edgesNumber = edgeSpot.size();
                if (edgesNumber == 0) //This shouldn't be an issue
                    continue;
                int edgeIndex = 0;
                int shiftMax = edgesNumber * 5 + 10;
                int edgeShift = shiftMax * 2 / edgesNumber;
                for (Edge<E, V> edge : edgeSpot) {
                    Point2D to = nodeLocations.get(edge.vertices()[0]);
                    Point2D from = nodeLocations.get(edge.vertices()[1]);
                    double angle = FxMath.getReciprocalAngle(from, to);
                    Point2D middle = FxMath.getMiddlePoint(from, to);

                    double shift = shiftMax - edgeIndex * edgeShift * 2;
                    Point2D controlPoint = FxMath.shiftPoint(middle, angle, shift);
                    //Draw edge
                    gc.save();
                    gc.setLineWidth(4);
                    gc.setLineDashes(30);
                    gc.setStroke(EDGE_BORDER_COLOR);
                    gc.beginPath();
                    gc.moveTo(from.getX() * zoom + shiftX, from.getY() * zoom + shiftY);
                    gc.quadraticCurveTo(
                            controlPoint.getX() * zoom + shiftX,
                            controlPoint.getY() * zoom + shiftY,
                            to.getX() * zoom + shiftX,
                            to.getY() * zoom + shiftY);
                    gc.stroke();
                    gc.setLineWidth(2);
                    if (edge.element().isSelected()) {
                        gc.setStroke(SELECTED_EDGE_COLOR);
                    } else {
                        gc.setStroke(EDGE_COLOR);
                    }
                    gc.beginPath();
                    gc.moveTo(from.getX() * zoom + shiftX, from.getY() * zoom + shiftY);
                    gc.quadraticCurveTo(
                            controlPoint.getX() * zoom + shiftX,
                            controlPoint.getY() * zoom + shiftY,
                            to.getX() * zoom + shiftX,
                            to.getY() * zoom + shiftY);
                    gc.stroke();

                    //Draw edge text
                    Point2D textLocation = FxMath.getMiddlePoint(middle, controlPoint);
                    if (edge.element().isSelected()) {
                        gc.setFill(SELECTED_TEXT_COLOR);
                    } else {
                        gc.setFill(TEXT_COLOR);
                    }
                    gc.setFont(new Font(20));
                    gc.setStroke(TEXT_BORDER_COLOR);
                    gc.setLineDashes(0);
                    gc.setLineWidth(2);
                    gc.strokeText(
                            edge.element().toString(),
                            textLocation.getX() * zoom + shiftX,
                            textLocation.getY() * zoom + shiftY);
                    gc.fillText(
                            edge.element().toString(),
                            textLocation.getX() * zoom + shiftX,
                            textLocation.getY() * zoom + shiftY);
                    gc.restore();
                    edgeIndex++;
                }
            }
        }
    }

    private void cacheVertexEdges() {
        Map<Vertex<V>, Set<Edge<E, V>>> vertexEdges = new HashMap<>();

        for (Edge<E, V> edge : graph.edges()) {
            Vertex<V>[] vertices = edge.vertices();
            if (!vertexEdges.containsKey(vertices[0])) {
                vertexEdges.put(vertices[0], new HashSet<>());
            }
            if (!vertexEdges.containsKey(vertices[1])) {
                vertexEdges.put(vertices[1], new HashSet<>());
            }
            vertexEdges.get(vertices[0]).add(edge);
            vertexEdges.get(vertices[1]).add(edge);
        }

        edgeSpotsCache = new HashSet<>();

        for (Map.Entry<Vertex<V>, Set<Edge<E, V>>> entry : vertexEdges.entrySet()) {
            for (Vertex<V> vertex : graph.vertices()) {
                if (entry.getKey() == vertex) continue;
                Set<Edge<E, V>> edgeSpot = new HashSet<>();
                for (Edge<E, V> edge : entry.getValue()) {
                    Vertex<V>[] vertices = edge.vertices();
                    if ((vertices[0] == entry.getKey() && vertices[1] == vertex)
                            || (vertices[1] == entry.getKey() && vertices[0] == vertex)) {
                        edgeSpot.add(edge);
                    }
                }
                edgeSpotsCache.add(edgeSpot);
            }
        }
    }

    /**
     * Draws a single node onto the canvas with the given parameters
     *
     * @param point    Node location
     * @param size     Node diameter
     * @param selected Selection markings
     */
    private void drawSingleNode(Point2D point, double size, boolean selected) {
        double x = point.getX();
        double y = point.getY();
        if (renderNodeBorders) {
            Color colorbuffer = (Color) gc.getFill();
            if (selected) {
                gc.setFill(SELECTED_NODE_BORDER_COLOR);
            } else {
                gc.setFill(NODE_BORDER_COLOR);
            }
            gc.fillOval(
                    x * zoom + shiftX - (size + nodeBorderSize) / 2,
                    y * zoom + shiftY - (size + nodeBorderSize) / 2,
                    size + nodeBorderSize,
                    size + nodeBorderSize);
            gc.setFill(colorbuffer);
        }
        gc.fillOval(
                x * zoom + shiftX - size / 2,
                y * zoom + shiftY - size / 2,
                size, size);
    }

    /**
     * Draws a single node and it's border centered around a point given the
     * size.
     *
     * @param point    Node diameter
     * @param text     Adjacent text label
     * @param size     Node diameter
     * @param selected Selection markings
     */
    private void drawSingleNode(Point2D point, String text, double size, boolean selected) {
        drawSingleNode(point, size, selected);
        gc.setFont(new Font(20));
        if (selected) {
            gc.setStroke(TEXT_BORDER_COLOR);
            gc.setFill(SELECTED_TEXT_COLOR);
        } else {
            gc.setStroke(TEXT_BORDER_COLOR);
            gc.setFill(TEXT_COLOR);
        }
        gc.setLineDashes(0);
        gc.setLineWidth(2);
        gc.strokeText(
                text,
                point.getX() * zoom + shiftX + size / 2 + 2,
                point.getY() * zoom + shiftY + 4);
        gc.fillText(
                text,
                point.getX() * zoom + shiftX + size / 2 + 2,
                point.getY() * zoom + shiftY + 4);
    }

    /**
     * Tests whether the mouse pointer clicked a drawn and returns the vertex of the node in case of a collision.
     *
     * @param x X coordinate of the mouse relative to the canvas.
     * @param y Y coordinate of the mouse relative to the canvas.
     * @return The node where the mouse clicked, or null if no node in the location.
     */
    private Vertex<V> checkForMouseNodeCollision(double x, double y) {
        Vertex<V> found = null;
        if (graphDrawn) {
            for (Vertex<V> vertex : graph.vertices()) {
                Vector<Double> nodeHitbox = nodeHitbox(vertex);
                if (nodeHitbox.remove(0) < x
                        && nodeHitbox.remove(0) < y
                        && nodeHitbox.remove(0) > x
                        && nodeHitbox.remove(0) > y) {
                    found = vertex;
                }
            }
        }
        return found;
    }

    /**
     * Converts a coordinate pair (x,y) from the drawing space (location relative to the canvas) to the model space.
     *
     * @param x Relative to the canvas.
     * @param y Relative to the canvas.
     * @return Point in the model space.
     */
    private Point2D drawingSpaceToCoordinateSpace(double x, double y) {
        return new Point2D(
                (zoom == 0) ? x - shiftX : (x - shiftX) / zoom,
                (zoom == 0) ? y - shiftY : (y - shiftY) / zoom
        );
    }

}