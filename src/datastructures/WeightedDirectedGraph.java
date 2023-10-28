package datastructures;

public class WeightedDirectedGraph extends GraphMatrixRepresentation<Double> {

    public WeightedDirectedGraph(int size) {
        super(size);
    }

    @Override
    public Double noEdge() {
        return Double.POSITIVE_INFINITY;
    }


}
