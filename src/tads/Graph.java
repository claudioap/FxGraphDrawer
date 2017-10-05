package tads;

import java.util.Collection;

/**
 * @param <V> Vertex data type
 * @param <E> Edge data type
 * @author Cláudio Pereira <cad.pereira@campus.fct.unl.pt>
 * @author José Pereira <jcpereira.dev@gmail.com>
 */
public interface Graph<V, E> {
    interface Edge<E, V> {
        E element();

        Vertex<V>[] vertices();
    }

    interface Vertex<V> {
        V element();
    }

    /**
     * Returns the number of vertices of the graph
     *
     * @return vertex count
     */
    int vertexCount();

    /**
     * Returns the number of edges of the graph
     *
     * @return edge count
     */
    int edgeCount();

    /**
     * Returns the graph vertices as an iterable collection.
     *
     * @return vertex iterable collection
     */
    Iterable<Vertex<V>> vertices();


    /**
     * Returns the graph vertices as a collection
     *
     * @return
     */
    Collection<V> verticesCollection();

    /**
     * Returns the edges of the graph as an iterable collection
     *
     * @return edge iterable collection
     */
    Iterable<Edge<E, V>> edges();

    /**
     * Returns the edges incident to a vertex as an iterable collection
     *
     * @param vertex vertex
     * @return incident edge iterable collection
     */
    Iterable<Edge<E, V>> incidentEdges(Vertex<V> vertex);

    /**
     * Returns the opposite vertex of an edge.
     *
     * @param vertex vertex
     * @param edge   edge
     * @return opposite vertex
     */
    Vertex<V> opposite(Vertex<V> vertex, Edge<E, V> edge);

    /**
     * Tests whether two verticesCollection are adjacent, i.e., connected by edge.
     *
     * @param originVertex      first vertex
     * @param destinationVertex second vertex
     * @return existence of an edge
     */
    boolean areAdjacent(Vertex<V> originVertex, Vertex<V> destinationVertex);

    /**
     * Calculates the degree (number of connected edges) of a vertex
     *
     * @param vertex vertex
     * @return vertex degree
     */
    int vertexDegree(Vertex<V> vertex);

}
