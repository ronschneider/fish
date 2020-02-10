package com.ron.phys;

import org.junit.Test;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class DorfTest {
    @Test
    public void getNeighborsCounts() {
        Terrain t = new Terrain(new Random(), 10, 10);
        AStar star = new AStar(t);

        assertEquals(8, star.getNeighbors(new Loc(5,5)).size());
        assertEquals(3, star.getNeighbors(new Loc(0,0)).size());
        assertEquals(5, star.getNeighbors(new Loc(0,1)).size());
        assertEquals(3, star.getNeighbors(new Loc(0,9)).size());
        assertEquals(3, star.getNeighbors(new Loc(9,0)).size());
        assertEquals(3, star.getNeighbors(new Loc(9,9)).size());
        assertEquals(5, star.getNeighbors(new Loc(1,9)).size());
        assertEquals(5, star.getNeighbors(new Loc(9,1)).size());
    }

    @Test
    public void getPath() {
        Terrain t = new Terrain(new Random(), 10, 10);
        AStar star = new AStar(t);

        assertEquals(2, star.solve(new Loc(1,1), new Loc(2,2)).size());
        assertEquals(5, star.solve(new Loc(1,1), new Loc(5,5)).size());
    }
}

