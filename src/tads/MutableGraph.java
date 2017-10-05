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

/**
 * Represents the base methods of a type of graph which can be mutated
 *
 * @param <V> Vertex data type
 * @param <E> Edge data type
 * @author Cláudio Pereira <cad.pereira@campus.fct.unl.pt>
 * @author José Pereira <jcpereira.dev@gmail.com>
 */
public abstract class MutableGraph<V, E> implements Graph<V, E> {
    /**
     * Inserts and returns a new vertex with a given element.
     *
     * @param element the element to store at the vertex
     * @return the newly created vertex
     */
    public abstract Vertex<V> addVertex(V element);

    /**
     * Inserts and returns a new edge with a given element between two vertices.
     * Whether it accepts more than one edge is up to the concrete implementation.
     *
     * @param u       first vertex
     * @param v       second vertex
     * @param element element to store at edge
     * @return the newly created edge
     */
    public abstract Edge<E, V> addEdge(Vertex<V> u, Vertex<V> v, E element);

    /**
     * Inserts an edge with an element between two stored elements (at vertices). Finding the
     * corresponding vertices is up to the equality between stored objects.
     *
     * @param u       first element
     * @param v       second element
     * @param element element to store at edge
     * @return the newly created edge
     */
    public abstract Edge<E, V> addEdge(V u, V v, E element);

    /**
     * Replaces the element of a given vertex with a new element
     *
     * @param vertex  Operation vertex
     * @param element New element
     * @return Previous element
     */
    public abstract V replace(Vertex<V> vertex, V element);

    /**
     * Replaces the element of a given edge with a new element
     *
     * @param edge    Operation edge
     * @param element New element
     * @return Previous element
     */
    public abstract E replace(Edge<E, V> edge, E element);

    /**
     * Removes a vertex and returns its element.
     *
     * @param vertex Vertex to remove
     * @return Element stored at the edge
     */
    public abstract V removeVertex(Vertex<V> vertex);

    /**
     * Removes an edge and returns its element.
     *
     * @param edge Edge to remove
     * @return Element stored at the edge
     */
    public abstract E removeEdge(Edge<E, V> edge);

}
