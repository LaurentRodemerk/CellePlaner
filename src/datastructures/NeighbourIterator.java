package datastructures;

import java.util.Iterator;

public class NeighbourIterator implements Iterator<Integer> {

    private final Graph<?> graph;
    private final int v1;
    private int v2; //vertices

    public NeighbourIterator(Graph<?> graph, int vertex) {
        this.graph = graph;
        this.v1 = vertex;
        this.v2 = -1;

        tryNext();
    }

    @Override
    public boolean hasNext() {
        return v2 < graph.getSize();
    }

    @Override
    public Integer next() {
        int k = v2;
        tryNext();
        return k;
    }

    private void tryNext() {
        v2++;
        while (v2 < graph.getSize())
            if (graph.isEdge(v1, v2))
                return;
            else
                v2++;
    }
}
