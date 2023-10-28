package datastructures;

import java.util.Iterator;

public class PathIterator implements Iterator<Integer> {

    public static final int END_OF_PATH = -1;

    private final Graph<?> graph;
    private int vCurrent;
    private int vPrev;


    public PathIterator(WeightedDirectedGraph graph, int start) {
        this.graph = graph;
        this.vCurrent = start;
        this.vPrev = -1;
    }

    @Override
    public boolean hasNext() {

        if (vCurrent >= graph.size || vCurrent < 0) return false;

        NeighbourIterator neighbourIterator = new NeighbourIterator(graph, vCurrent);
        return neighbourIterator.hasNext() && neighbourIterator.next() < graph.size;
    }

    @Override
    public Integer next() {

        if (!hasNext()) return END_OF_PATH;

        NeighbourIterator neighbourIterator = new NeighbourIterator(graph, vCurrent);

        vPrev = vCurrent;
        int tmp = neighbourIterator.next();

        if (tmp != vPrev) vCurrent = tmp;

        return vCurrent;
    }


}
