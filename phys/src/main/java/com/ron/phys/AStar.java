package com.ron.phys;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class AStar {
    private Terrain terrain;
    private int terrainX;
    private int terrainY;

    public AStar(Terrain terrain) {
        this.terrain = terrain;
        this.terrainX = terrain.getBounds()[0];
        this.terrainY = terrain.getBounds()[1];
    }

    public List<Loc> solve(Loc start, Loc end) {
        Logger log = Logger.getLogger("astar");

        List<Loc> openSet = new ArrayList<>(); // The set of nodes that need to be examined
        openSet.add(start);

        Map<Loc, Loc> cameFrom = new HashMap<>(); // For node n, cameFrom[n] is the node immediately preceding it on the cheapest path from start to n currently known.

        Map<Loc, Integer> gScore = new DefaultHashMap<>(Integer.MAX_VALUE); // For node n, gScore[n] is the cost of the cheapest path from start to n currently known.
        gScore.put(start, 0);

        // For node n, fScore[n] := gScore[n] + h(n).
        Map<Loc, Integer> fScore = new DefaultHashMap<>(Integer.MAX_VALUE);
        fScore.put(start, estimate(start, end));
//        log.severe("openSet: " + openSet);

        while (openSet.size() > 0) {
//            log.severe("openSet: " + openSet);
            // current := the node in openSet having the lowest fScore[] value
            Map.Entry<Loc, Integer> min = Collections.min(fScore.entrySet(), new Comparator<Map.Entry<Loc, Integer>>() {
                public int compare(Map.Entry<Loc, Integer> entry1, Map.Entry<Loc, Integer> entry2) {
                    return entry1.getValue().compareTo(entry2.getValue());
                }
            });

            Loc current = min.getKey();
//            log.severe("current: " + current);
            if (current.equals(end)) {
                return reconstructPath(cameFrom, current);
            }

            openSet.remove(current);
            for (Loc neighbor : getNeighbors(current)) {
                // d(current,neighbor) is the weight of the edge from current to neighbor
                // tentative_gScore is the distance from start to the neighbor through current
                int tentative_gScore = gScore.get(current) + pathCost(current, neighbor);
                if (tentative_gScore < gScore.get(neighbor)) {
                    // This path to neighbor is better than any previous one. Record it!
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentative_gScore);
                    fScore.put(neighbor, gScore.get(neighbor) + estimate(neighbor, end));

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return null;
    }

    public List<Loc> reconstructPath(Map<Loc, Loc> cameFrom, Loc current) {
        List<Loc> path = new ArrayList<>();

        path.add(current);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(0, current);
        }
        return path;
    }

    public int pathCost(Loc start, Loc end) {
        return 1;
    }

    public List<Loc> getNeighbors(Loc loc) {
        int x = loc.getX();
        int y = loc.getY();

        List<Loc> result = new ArrayList<Loc>();

        int[][] offsets = {
                {1, -1}, {1, 0}, {1, 1},
                {0, -1}, {0, 1},
                {-1, -1}, {-1, 0}, {-1, 1}};
        for (int i = 0; i < 8; i++) {
            int nx = x + offsets[i][0];
            int ny = y + offsets[i][1];

            if ((nx >= 0) && (ny >= 0) && (nx < terrainX) && (ny < terrainY)) {
                result.add(new Loc(nx, ny));
            }
        }

        return result;
    }

    private int estimate(Loc start, Loc end) {
        return Math.abs(end.getX() - start.getX()) + Math.abs(end.getY() - start.getY());
    }
}