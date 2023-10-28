package datastructures;

import java.util.Iterator;

public abstract class Graph<Type> {

    protected int size; //number of vertices

    public Graph(int size) {
        this.size = size;
    }

    // returns number of vertices
    public int getSize() {
        return size;
    }

    // deletes all edges
    protected void initialize() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                deleteEdge(i, j);
            }
        }
    }

    public boolean isEdge(int v1, int v2) {
        return !getWeight(v1, v2).equals(noEdge());
    }

    public void deleteEdge(int v1, int v2) {
        setWeight(v1, v2, noEdge());
    }

    public abstract Type noEdge();

    public abstract Type getWeight(int v1, int v2);

    public abstract void setWeight(int v1, int v2, Type w);

    public abstract Iterator<Integer> getNeighbourIterator(int vertex);
}
