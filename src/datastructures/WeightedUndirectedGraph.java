package datastructures;

public class WeightedUndirectedGraph extends WeightedDirectedGraph {

    public WeightedUndirectedGraph(int size) {
        super(size);
    }

    @Override
    public void setWeight(int v1, int v2, Double weight) {
        super.setWeight(v1, v2, weight);
        super.setWeight(v2, v1, weight);
    }
}
