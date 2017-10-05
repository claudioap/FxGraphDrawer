package tads;

import java.io.Serializable;
import java.util.*;

/**
 * This is a dumb graph implementation, based on a list of edges (which contain their vertices)
 * It is not the smartest nor the most efficient implementation, but it should be easy to understand.
 * NOTE: This implementation assumes that every vertex has a different object as its element
 *
 * @author patricia.macedo
 * @author Cláudio Pereira <cad.pereira@campus.fct.unl.pt>
 */
public class SimpleGraph<V, E> extends MutableGraph<V, E> implements Graph<V, E>, Serializable {

    private int edgesCount;
    private HashMap<V, Vertex<V>> vertexList;

    public SimpleGraph() {
        this.edgesCount = 0;
        vertexList = new HashMap<>();
    }

    private boolean containsVertex(Vertex<V> vertex) {
        return (vertex != null && this.vertexList.values().contains(vertex));
    }

    private boolean containsEdge(Edge<E, V> edge) {
        return (edge != null && edgeSet().contains(edge));
    }

    @Override
    public int vertexCount() {
        return vertexList.size();
    }

    @Override
    public int edgeCount() {
        return edgesCount;
    }

    @Override
    public Iterable<Vertex<V>> vertices() {
        return vertexList.values();
    }

    @Override
    public Collection<V> verticesCollection() {
        HashSet<V> vertexValues = new HashSet<>();
        for (Vertex<V> vertex : vertices()) {
            vertexValues.add(vertex.element());
        }
        return vertexValues;
    }

    private Set<Edge<E, V>> edgeSet() {
        Set<Edge<E, V>> edges = new HashSet<>();
        for (Vertex<V> vertex : vertices()) {
            edges.addAll(((SimpleVertex) vertex).edgeList);
        }
        return edges;
    }

    @Override
    public Iterable<Edge<E, V>> edges() {
        return edgeSet();
    }

    @Override
    public V replace(Vertex<V> vertex, V element) {
        if (!containsVertex(vertex))
            throw new IllegalArgumentException("Vertex not contained within graph");
        V oldElement = vertex.element();
        ((SimpleVertex) vertex).element = element;
        return oldElement;
    }

    @Override
    public E replace(Edge<E, V> edge, E element) {
        if (!containsEdge(edge))
            throw new IllegalArgumentException("Edge not contained within graph");
        SimpleEdge sEdge = (SimpleEdge) edge;
        E oldElement = sEdge.element();
        sEdge.element = element;
        return oldElement;
    }

    @Override
    public Iterable<Edge<E, V>> incidentEdges(Vertex<V> vertex) {
        if (!containsVertex(vertex))
            throw new IllegalArgumentException("Vertex not contained within graph");
        return ((SimpleVertex) vertex).edgeList;
    }

    @Override
    public Vertex<V> opposite(Vertex<V> vertex, Edge<E, V> edge) {
        if (!containsVertex(vertex))
            throw new IllegalArgumentException("Vertex not contained within graph");
        Vertex<V>[] vertices = edge.vertices();
        if (vertices[0] == vertex) {
            return vertices[1];
        }
        if (vertices[1] == vertex) {
            return vertices[0];
        }
        throw new RuntimeException("Inconsistent edge attributes found");
    }

    @Override
    public boolean areAdjacent(Vertex<V> originVertex, Vertex<V> destinationVertex) {
        for (Edge<E, V> e : edges()) {
            SimpleEdge edge = (SimpleEdge) e;
            if (edge.originVertex == originVertex && edge.destinationVertex == destinationVertex
                    || edge.destinationVertex == originVertex && edge.originVertex == destinationVertex) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Vertex<V> addVertex(V element) {
        if (vertexList.containsKey(element)) {
            throw new IllegalArgumentException(element + " already exists ");
        }
        SimpleVertex vertex = new SimpleVertex(element);
        vertexList.put(element, vertex);
        return vertex;

    }

    @Override
    public Edge<E, V> addEdge(Vertex<V> originVertex, Vertex<V> destinationVertex, E element) {
        if (!(vertexList.containsValue(originVertex) && vertexList.containsValue(destinationVertex)))
            throw new IllegalArgumentException("Vertices must be part of the graph before the edge is built");
        SimpleEdge edge = new SimpleEdge(element, originVertex, destinationVertex);
        ((SimpleVertex) originVertex).edgeList.add(edge);
        ((SimpleVertex) destinationVertex).edgeList.add(edge);
        edgesCount++;
        return edge;
    }

    @Override
    public Edge<E, V> addEdge(V u, V v, E element) {
        Vertex<V> v1 = vertexList.get(u);
        Vertex<V> v2 = vertexList.get(v);
        return addEdge(v1, v2, element);
    }

    @Override
    public V removeVertex(Vertex<V> vertex) {
        if (!containsVertex(vertex))
            throw new IllegalArgumentException("Vertex not contained within graph");
        vertexList.remove(vertex);
        return vertex.element();
    }

    @Override
    public E removeEdge(Edge<E, V> edge) {
        if (!containsEdge(edge))
            throw new IllegalArgumentException("Edge not contained within graph");
        Vertex<V>[] vertices = edge.vertices();
        ((SimpleVertex) vertices[0]).edgeList.remove(edge);
        ((SimpleVertex) vertices[1]).edgeList.remove(edge);
        edgesCount--;
        return edge.element();
    }

    @Override
    public int vertexDegree(Vertex<V> vertex) {
        if (!containsVertex(vertex))
            throw new IllegalArgumentException("Vertex not contained within graph");
        return ((SimpleVertex) vertex).edgeList.size();
    }

    private class SimpleVertex implements Vertex<V>, Serializable {
        private V element;
        private List<Edge<E, V>> edgeList;

        SimpleVertex(V element) {
            if (element == null) {
                throw new IllegalArgumentException("Attempted creation of vertex with null element");
            }
            this.element = element;
            this.edgeList = new ArrayList<>();
        }

        @Override
        public V element() {
            return element;
        }
    }

    private class SimpleEdge implements Edge<E, V>, Serializable {

        private E element;
        private Vertex<V> originVertex, destinationVertex;

        SimpleEdge(E element, Vertex<V> originVertex, Vertex<V> destinationVertex) {
            if (element == null)
                throw new IllegalArgumentException("Attempted creation of edge with null element");
            this.element = element;
            this.originVertex = originVertex;
            this.destinationVertex = destinationVertex;
        }

        @Override
        public E element() {
            return element;
        }

        @Override
        public Vertex<V>[] vertices() {
            Vertex[] vertices = new Vertex[2];
            vertices[0] = originVertex;
            vertices[1] = destinationVertex;
            return vertices;
        }

        @Override
        public String toString() {
            return "" + element;
        }
    }
}
