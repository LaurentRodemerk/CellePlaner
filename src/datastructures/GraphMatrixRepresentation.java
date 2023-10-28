package datastructures;

import rowing.Heat;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class GraphMatrixRepresentation<Type> extends Graph<Type> {

    private final ArrayList<ArrayList<Type>> matrix; //adjacent matrix

    public GraphMatrixRepresentation(int size) {
        super(size);

        //representing graph as (size-1 x size-1) adjacent matrix
        matrix = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            matrix.add(new ArrayList<>());

            for (int j = 0; j < size; j++)
                matrix.get(i).add(noEdge());
        }
    }

    @Override
    public Type getWeight(int v1, int v2) {
        return matrix.get(v1).get(v2);
    }

    @Override
    public void setWeight(int v1, int v2, Type weight) {
        matrix.get(v1).set(v2, weight);
    }

    @Override
    public Iterator<Integer> getNeighbourIterator(int vertex) {
        return new NeighbourIterator(this, vertex);
    }
}
