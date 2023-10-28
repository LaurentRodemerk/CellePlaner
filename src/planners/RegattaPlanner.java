package planners;

import datastructures.NeighbourIterator;
import datastructures.PathIterator;
import datastructures.WeightedDirectedGraph;
import datastructures.WeightedUndirectedGraph;
import rowing.Athlete;
import rowing.Boat;
import rowing.Heat;

import java.util.*;

public class RegattaPlanner {

    // Offsets between two ...
    public static final double BOOT_OFFSET = 20;
    public static final double ATHLETE_OFFSET = 40;
    public static final double COX_OFFSET = 20;
    public static final double HEAT_500_OFFSET = 10;
    public static final double HEAT_1000_OFFSET = 15;

    private final double maxAcceptableOffset;

    public RegattaPlanner(double maxAcceptableOffset) {
        this.maxAcceptableOffset = maxAcceptableOffset;
    }

    /*
     * Organizes Regatta, plans lunch break with certain duration.
     * Organisation starts at the end.
     *
     * Step 1: Get presorted list of heats using greedy method
     * Step 2: Embed remaining heats
     * Step 3: Add lunch break and embed remaining heats
     */

    public List<Heat> organizeRegatta(List<Heat> heats,
                                      Heat lastHeat,
                                      Date regattaStart,
                                      Date lunchBreak,
                                      double lunchOffset) {


        // completed graph
        final int N = heats.size();
        final WeightedUndirectedGraph graph = new WeightedUndirectedGraph(N);     // all heats

        // initializing graph
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                graph.setWeight(i, j, getMinOffset(heats.get(i), heats.get(j)));
            }
        }

        drawGraph(graph);

        System.out.printf("Anzahl Rennen: %d\n", heats.size());

        // finding start
        final int start = heats.indexOf(lastHeat);

        // Step 1:Presorting heats using greedy method
        WeightedDirectedGraph intermediateResult = greedyPresorted(start, graph);

        System.out.printf("Nach Greedy: %d\n", getRemainingVertices(intermediateResult, start).size());

        // Step 2: Embedding remaining heats
        embed(intermediateResult, getRemainingVertices(intermediateResult, start), start, graph);

        System.out.printf("Nach Einbettung: %d\n", getRemainingVertices(intermediateResult, start).size());

        // Step 3: Adding lunch break
        addLunchBreak(intermediateResult, graph, start, regattaStart, lunchBreak, lunchOffset);

        System.out.printf("Nach Mittagspause: %d\n\n", getRemainingVertices(intermediateResult, start).size());

        // Translate representing graph into list of heats
        return graphToList(reversedPath(intermediateResult), heats, getEndOfPath(intermediateResult, start), regattaStart);
    }

    /*
     * Converts a path in to a list of heats,
     * Adds time schedule
     */
    private List<Heat> graphToList(WeightedDirectedGraph path, List<Heat> heats, int start, Date regattaStart) {

        List<Heat> lsResult = new ArrayList<>();
        PathIterator iterator = new PathIterator(path, start);

        heats.get(start).setTime(regattaStart);
        lsResult.add(heats.get(start));

        int vPrev = start;
        int vNext = iterator.next();

        while (vNext != PathIterator.END_OF_PATH) {

            Date date = new Date();

            Date prevDate = heats.get(vPrev).getTime();
            date.setTime(prevDate.getTime() + (path.getWeight(vPrev, vNext).longValue() * 60000));

            Heat h = heats.get(vNext);
            h.setTime(date);

            lsResult.add(h);

            vPrev = vNext;
            vNext = iterator.next();
        }

        return lsResult;
    }

    public List<Integer> getRemainingVertices(WeightedDirectedGraph path, int start) {
        List<Integer> ls = new ArrayList<>();

        for (int i = 0; i < path.getSize(); i++) {
            if (doesNotContainVertex(path, i, start)) {
                ls.add(i);
            }
        }

        return ls;
    }

    /*
     * Presorts heats using greedy
     */
    private WeightedDirectedGraph greedyPresorted(int startVertex, WeightedUndirectedGraph completedGraph) {

        int N = completedGraph.getSize();
        final WeightedDirectedGraph path = new WeightedDirectedGraph(N);

        int vertex = startVertex; //last vertex of current path
        List<Integer> sortedVertexList = getSortedVertexList(completedGraph);

        // Using greedy, test every neighbours of "startVertex" until first edge is found
        for (int j : sortedVertexList) {

            if ((j != startVertex) && (completedGraph.getWeight(startVertex, j) <= maxAcceptableOffset)) {

                path.setWeight(startVertex, j, completedGraph.getWeight(startVertex, j));
                vertex = j;

                break;
            }
        }

        // Adding edges of result path graph
        for (int i = 0; i < (N - 1); i++) {

            // Using greedy, test every neighbour of "vertex" until an edges is found
            for (int j : sortedVertexList) {
                if (j == vertex) continue;

                if ((completedGraph.getWeight(vertex, j) <= maxAcceptableOffset)
                        && doesNotContainVertex(path, j, startVertex)) {

                    int end = getEndOfPath(path, startVertex);

                    if (isEdgeConfirm(j, end, path, completedGraph)) {

                        // adding found edge to path
                        path.setWeight(vertex, j, completedGraph.getWeight(vertex, j));
                        vertex = j;

                        break;
                    }
                }
            }
        }

        return path;
    }

    /*
     * Creates a presorted list of vertices of a completed graph for greedy.
     */
    private List<Integer> getSortedVertexList(WeightedUndirectedGraph graph) {

        // sort sums of vertices' weight
        HashMap<Integer, Double> priorities = new HashMap<>();
        List<Double> priorityList = new ArrayList<>();

        for (int i = 0; i < graph.getSize(); i++) {

            double d = 0;

            for (int j = 0; j < graph.getSize(); j++) {
                if (graph.getWeight(i, j) != Double.POSITIVE_INFINITY) {
                    d += graph.getWeight(i, j);
                }
            }

            priorities.put(i, d);

            if (!priorityList.contains(d)) priorityList.add(d);
        }

        priorityList.sort(Collections.reverseOrder());

        // create re-association from sums of weights to vertices
        HashMap<Double, List<Integer>> map = new HashMap<>();

        for (int i = 0; i < graph.getSize(); i++) {

            List<Integer> ls = new ArrayList<>();
            double d = priorities.get(i);

            map.put(d, ls);

            for (int j = 0; j < graph.getSize(); j++) {
                if (priorities.get(j) == d) map.get(d).add(j);
            }
        }

        // create final presorted list
        List<Integer> result = new ArrayList<>();

        for (double d : priorityList) {
            result.addAll(map.get(d));
        }

        return result;
    }

    /*
     * Embeds remaining vertices into presorted path
     */
    private void embed(WeightedDirectedGraph path,
                       List<Integer> remaining,
                       int start,
                       WeightedUndirectedGraph completedGraph) {

        for (int v : remaining) {

            PathIterator pathIterator = new PathIterator(path, start);


            int vNext = pathIterator.next();
            int vPrev = start;

            // finding a vPrev and vNext, where v can be embedded between them
            while (pathIterator.hasNext()) {

                if ((completedGraph.getWeight(vPrev, v) <= maxAcceptableOffset)
                        && (completedGraph.getWeight(v, vNext) <= maxAcceptableOffset)) {

                    if (((vNext == getEndOfPath(path, start))
                            || isEdgeConfirm(v, vNext, reversedPath(path), completedGraph))
                            && ((vPrev == start)
                            || isEdgeConfirm(v, vPrev, path, completedGraph))) {

                        // embedding v
                        path.setWeight(vPrev, v, completedGraph.getWeight(vPrev, v));
                        path.setWeight(v, vNext, completedGraph.getWeight(v, vNext));
                        path.setWeight(vPrev, vNext, path.noEdge());

                        break;
                    }
                }

                vPrev = vNext;
                vNext = pathIterator.next();
            }
        }
    }

    /*
     * Adds lunch break to given path and embeds remaining vertices
     */
    private void addLunchBreak(WeightedDirectedGraph path,
                               WeightedUndirectedGraph completeGraph,
                               int start,
                               Date regattaStart,
                               Date lunchBreak,
                               double offset) {

        // last vertex represents first heat of actual regatta, getting actual start vertex
        int end = start;
        PathIterator i = new PathIterator(path, start);

        while (i.hasNext()) {
            end = i.next();
        }

        // Calculating delay between regatta start and lunch break (in min)
        final double timeUntilBreak = (lunchBreak.getTime() - regattaStart.getTime()) / 60000.0;

        // Iterating to position of lunch break
        double delay = 0;
        int vPrev = end;
        int vNext = new NeighbourIterator(reversedPath(path), vPrev).next();

        PathIterator iterator = new PathIterator(reversedPath(path), end);
        iterator.next();

        final double eps = 15.0;

        while ((delay < (timeUntilBreak - eps)) && iterator.hasNext()) {
            delay += path.getWeight(vNext, vPrev);

            vPrev = vNext;
            vNext = iterator.next();
        }

        // Setting lunch break
        path.setWeight(vNext, vPrev, offset);

        // Embedding remaining vertices
        embed(path, getRemainingVertices(path, start), start, completeGraph);

    }

    private boolean doesNotContainVertex(WeightedDirectedGraph path, int vertex, int start) {

        if (vertex == start) return false;

        for (int i = 0; i < path.getSize(); i++) {
            if (path.isEdge(i, vertex) || path.isEdge(vertex, i)) return false;
        }

        return true;
    }

    private boolean isEdgeConfirm(int newVertex,
                                  int position,
                                  WeightedDirectedGraph path,
                                  WeightedUndirectedGraph completeGraph) {

        if (newVertex >= completeGraph.getSize()) return false;

        double offsetCounter = completeGraph.getWeight(position, newVertex);

        // Iterating backwards through path
        PathIterator iterator = new PathIterator(reversedPath(path), position);

        int vPrev = position;
        int vNext = iterator.next();

        while (iterator.hasNext() && (offsetCounter < ATHLETE_OFFSET)) {
            if ((offsetCounter += path.getWeight(vNext, vPrev)) < completeGraph.getWeight(vNext, newVertex))
                return false;

            vPrev = vNext;
            vNext = iterator.next();
        }


        return true;
    }


    private int getEndOfPath(WeightedDirectedGraph path, int start) {
        int end = 0;
        PathIterator it = new PathIterator(path, start);

        while (it.hasNext()) {
            end = it.next();
        }
        return end;
    }

    // Reversing graph, which is a directed Path
    private WeightedDirectedGraph reversedPath(WeightedDirectedGraph graph) {

        int N = graph.getSize();
        WeightedDirectedGraph g = new WeightedDirectedGraph(N);

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (graph.isEdge(i, j))
                    g.setWeight(j, i, graph.getWeight(i, j));
            }
        }

        return g;
    }


    /*
     * returns offset between two heats
     */
    private double getMinOffset(Heat h1, Heat h2) {

        if (h1.equals(h2))
            return Double.POSITIVE_INFINITY;

        List<Boat> boats1 = h1.getBoats();
        List<Boat> boats2 = h2.getBoats();

        List<Athlete> athletes1 = new ArrayList<>();
        List<Athlete> athletes2 = new ArrayList<>();

        List<Athlete> coxes1 = new ArrayList<>();
        List<Athlete> coxes2 = new ArrayList<>();

        for (Boat b : boats1) {
            Collections.addAll(athletes1, b.getAthletes());

            if (b.getCox() != null) {
                coxes1.add(b.getCox());
            }

        }

        for (Boat b : boats2) {
            Collections.addAll(athletes2, b.getAthletes());

            if (b.getCox() != null) {
                coxes2.add(b.getCox());
            }

        }

        for (Athlete a1 : athletes1) {
            for (Athlete a2 : athletes2) {
                if (a1.equals(a2)) {

                    return ATHLETE_OFFSET;
                }
            }
        }

        for (Athlete c1 : coxes1) {
            for (Athlete c2 : coxes2) {
                if (c1.equals(c2)) {

                    return COX_OFFSET;
                }
            }

        }

        for (Boat b1 : boats1) {
            for (Boat b2 : boats2) {
                if (b1.equals(b2)) {

                    return BOOT_OFFSET;
                }
            }

        }

        return (h2.getDistance() == Heat.DISTANCE_500) ? HEAT_500_OFFSET : HEAT_1000_OFFSET;
    }

    public void drawGraph(WeightedDirectedGraph graph) {

        System.out.println("\n################################\n");

        System.out.print("   \t\t");

        for (int i = 0; i < graph.getSize(); i++) {
            System.out.printf("%2d\t", i);
        }

        System.out.println();
        System.out.print("   \t");

        for (int i = 0; i <= graph.getSize(); i++) {
            System.out.print("----");
        }

        System.out.println();

        for (int i = 0; i < graph.getSize(); i++) {
            System.out.printf("%2d |\t", i);

            double delay = 0;

            for (int j = 0; j < graph.getSize(); j++) {

                if (graph.getWeight(i, j) != Double.POSITIVE_INFINITY) {
                    System.out.printf("%.0f\t", graph.getWeight(i, j));
                    delay += graph.getWeight(i, j);
                } else System.out.print("INF\t");
            }
            System.out.printf("\t%.0f", delay);
            System.out.println();
        }

        System.out.println("\n\n################################\n");

    }
}