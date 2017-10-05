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

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import random.Path;
import random.Point;
import tads.Graph;
import tads.Graph.Edge;
import tads.Graph.Vertex;
import tads.ImmutableGraph;
import tads.SimpleGraph;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a sample usage of the GraphDrawer widget.
 * It attempts to show off most of its possibilities without going into implementation details.
 *
 * @author Cláudio Pereira <cad.pereira@campus.fct.unl.pt>
 */
public class GraphDrawerDemo extends Application {

    @Override
    public void start(Stage primaryStage) {
        SimpleGraph<Point, Path> graph = new SimpleGraph<>();
        /* Populate the graph with sample data */
        Point a = new Point("a");
        Point b = new Point("b");
        Point c = new Point("c");
        Point d = new Point("d");
        Point e = new Point("e");

        Vertex<Point> va = graph.addVertex(a);
        Vertex<Point> vb = graph.addVertex(b);
        Vertex<Point> vc = graph.addVertex(c);
        Vertex<Point> vd = graph.addVertex(d);
        Vertex<Point> ve = graph.addVertex(e);

        Path abPath = new Path("1", a, b);
        graph.addEdge(va, vb, abPath);
        graph.addEdge(vb, vc, new Path("2", b, c));
        graph.addEdge(vc, vd, new Path("3", c, d));
        graph.addEdge(vd, va, new Path("4", d, a));
        graph.addEdge(vd, ve, new Path("5", d, e));
        graph.addEdge(ve, vd, new Path("6", e, d));
        graph.addEdge(vd, va, new Path("7", d, a));

        d.setSelected(true);
        abPath.setSelected(true);
        ImmutableGraph<Point, Path> immutableGraph = new ImmutableGraph<>(graph);

        /*Setup the drawing widget*/
        GraphDrawer<Point, Path> drawer = new GraphDrawer<>(immutableGraph);
        drawer.setClickAction(clickedNode -> {
            // Sample menu showing incident edges on node click (yes, dragging is also a click)
            // Fetches node incident edges and put them into a list (not ideal, but this is just a demo...)
            Iterable<Edge<Path, Point>> incidentEdges = immutableGraph.incidentEdges(clickedNode);
            List<Edge<Path, Point>> incidentEdgesList = new ArrayList<>();
            incidentEdges.forEach(incidentEdgesList::add);
            ObservableList<Graph.Edge<Path, Point>> edgeObservableList = FXCollections.observableList(incidentEdgesList);
            ListView listView = new ListView<>(edgeObservableList);
            System.out.println("Node " + clickedNode.element().toString() + " has been clicked");
            final Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(primaryStage);

            VBox dialogVbox = new VBox(20);
            dialogVbox.getChildren().add(
                    new Text("You have clicked " + clickedNode.element().toString() + ". Its edges are:"));
            dialogVbox.getChildren().add(listView);
            Scene dialogScene = new Scene(dialogVbox, 300, 200);
            dialog.setScene(dialogScene);
            dialog.show();
        });
        drawer.resize(500, 500);


        Pane pane = new Pane();
        pane.getChildren().add(drawer);
        Scene scene = new Scene(pane, 500, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Force-Directed Graph Drawing");
        primaryStage.show();
        drawer.startAnimation();
        /* In case the full drawer takes too many resources, a single frame can be drawn this way
        drawer.advanceSteps(1000);
        drawer.renderGraph();
        Note that nodes can still be dragged even if the image is static
        In this case you'd either want to re-render on click(with the consumer above) or
        change the click listeners (at GraphDrawer, under initMouseEvents())
        */
    }

    public static void main(String[] args) {
        launch(args);
    }
}
