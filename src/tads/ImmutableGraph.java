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

package tads;

import java.util.Collection;

/**
 * A wrapper around a graph which prevents modifications.
 * This is a safe way to send a graph while preventing data races or unwanted modifications.
 *
 * @param <V> Vertex data type
 * @param <E> Edge data type
 * @author Cláudio Pereira <cad.pereira@campus.fct.unl.pt>
 * @author José Pereira <jcpereira.dev@gmail.com>
 */
public class ImmutableGraph<V, E> implements Graph<V, E> {

    SimpleGraph<V, E> graph = new SimpleGraph<>();

    /**
     * Constructs an immutable graph based off an existing graph
     *
     * @param graph Source graph to build from
     */
    public ImmutableGraph(Graph<V, E> graph) {
        for (Vertex<V> vertex : graph.vertices()) {
            this.graph.addVertex(vertex.element());
        }
        for (Edge<E, V> edge : graph.edges()) {
            Vertex<V>[] vertices = edge.vertices();
            this.graph.addEdge(
                    vertices[0].element(),
                    vertices[1].element(),
                    edge.element());
        }
    }

    @Override
    public int vertexCount() {
        return graph.vertexCount();
    }

    @Override
    public int edgeCount() {
        return graph.edgeCount();
    }

    @Override
    public Iterable<Vertex<V>> vertices() {
        return graph.vertices();
    }

    @Override
    public Collection<V> verticesCollection() {
        return graph.verticesCollection();
    }

    @Override
    public Iterable<Edge<E, V>> edges() {
        return graph.edges();
    }

    @Override
    public Iterable<Edge<E, V>> incidentEdges(Vertex<V> vertex) {
        return graph.incidentEdges(vertex);
    }

    @Override
    public Vertex<V> opposite(Vertex<V> vertex, Edge<E, V> edge) {
        return graph.opposite(vertex, edge);
    }

    @Override
    public boolean areAdjacent(Vertex<V> originVertex, Vertex<V> destinationVertex) {
        return graph.areAdjacent(originVertex, destinationVertex);
    }

    @Override
    public int vertexDegree(Vertex<V> vertex) {
        return graph.vertexDegree(vertex);
    }
}
