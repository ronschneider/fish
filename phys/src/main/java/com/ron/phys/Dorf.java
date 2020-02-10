package com.ron.phys;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import static java.util.Collections.*;


interface JobObserver {
    void completed(Actor completingActor);
}

class Loc {
    public static Loc START = new Loc(1, 1);
    private int x;
    private int y;

    public Loc(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Loc offset(int dx, int dy) {
        return new Loc(dx + this.x, dy + this.y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Loc loc = (Loc) o;
        return x == loc.x && y == loc.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Loc{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}

class Actor {
    private Loc loc;
    private Job currentJob;
    private String name;

    public Actor(String name, Loc loc) {
        this.name = name;
        this.currentJob = null;
        if (loc == null) {
            this.loc = Loc.START;
        } else {
            this.loc = loc;
        }
    }

    public Loc getLoc() {
        return loc;
    }

    public Loc setLoc(Loc loc) {
        return (this.loc = loc);
    }

    public Job getCurrentJob() {
        return currentJob;
    }

    public void setCurrentJob(Job currentJob) {
        this.currentJob = currentJob;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    void tick(World w) {
        if (currentJob == null) {
            currentJob = w.assignJob();
        }

        if (currentJob != null) {
            if (currentJob.tick(this)) {
                w.jobCompleted(currentJob);
                currentJob = null;
            }
        }
    }

    void completeJob() {
        this.currentJob = null;
    }

    @Override
    public String toString() {
        return "Actor{" + name + ": " +
                loc +
                ", currentJob=" + currentJob +
                '}';
    }
}

abstract class Job {
    abstract boolean tick(Actor a);
}

class MoveToJob extends Job {
    private Loc loc;

    public MoveToJob(int x, int y) {
        this.loc = new Loc(x, y);
    }

    public boolean tick(Actor a) {
        Loc s = a.getLoc();

        int dx = (int) Math.signum(loc.getX() - s.getX());
        int dy = (int) Math.signum(loc.getY() - s.getY());

        a.setLoc(s.offset(dx, dy));

        boolean e = (a.getLoc().equals(this.loc));

        return (a.getLoc().equals(this.loc));
    }

    @Override
    public String toString() {
        return "MoveToJob{" +
                "loc=" + loc +
                '}';
    }

    public Loc getDest() {
        return loc;
    }
}

class Terrain {
    private final Random r;
    private SQ[] sq;
    private int dimX;
    private int dimY;

    public Terrain(Random r, int dimX, int dimY) {
        this.dimX = dimX;
        this.dimY = dimY;
        this.sq = new SQ[dimX * dimY];
        this.r = r;

        for (int x = 0; x < dimX; x++) {
            for (int y = 0; y < dimY; y++) {
                sq[ind(x, y)] = SQ.EMPTY;
            }
        }

        for (int i = 0; i < dimX; i++) {
            sq[ind(i, 0)] = SQ.INDESTRUCTABLE;
            sq[ind(i, dimY - 1)] = SQ.INDESTRUCTABLE;
        }

        for (int i = 0; i < dimY; i++) {
            sq[ind(0, i)] = SQ.INDESTRUCTABLE;
            sq[ind(dimX - 1, i)] = SQ.INDESTRUCTABLE;
        }

        plantTrees();
    }

    public int[] getBounds() {
        return new int[]{dimX, dimY};
    }

    public SQ get(int x, int y) {
        return sq[ind(x, y)];
    }

    private void plantTrees() {
        for (int x = 0; x < dimX; x++) {
            for (int y = 0; y < dimY; y++) {
                if (sq[ind(x, y)] == SQ.EMPTY) {
                    if (r.nextInt(100) < 20) {
                        sq[ind(x, y)] = SQ.TREE;
                    }
                }
            }
        }
    }

    private int ind(int x, int y) {
        return y * dimX + x;
    }

    enum SQ {
        EMPTY(0),
        INDESTRUCTABLE(1),
        DIRT(2),
        WATER(3),
        ROCK(4),
        TREE(5),
        ;

        private static final List<SQ> VALUES = unmodifiableList(Arrays.asList(values()));
        private static final int SIZE = VALUES.size();
        private static final Random RANDOM = new Random();
        private final int code;

        SQ(int code) {
            this.code = code;
        }

        public static SQ randomSQ() {
            return VALUES.get(RANDOM.nextInt(SIZE));
        }

        public int getCode() {
            return this.code;
        }
    }
}

class World {
    private List<Actor> actors = new ArrayList<Actor>();
    private List<Job> todoList = new ArrayList<Job>();
    private List<Job> currentJobs = new ArrayList<Job>();
    private Terrain terrain;
    private Random r = new Random();

    public World(int width, int height) {
        this.terrain = new Terrain(r, width, height);
    }

    void jobCompleted(Job job) {
        assert currentJobs.indexOf(job) >= 0;

        System.out.println("Job complete " + job);
        currentJobs.remove(job);
    }

    void tick() {
        for (Actor a : this.actors) {
            a.tick(this);
        }
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public void addActor(Actor a) {
        actors.add(a);
    }

    public void addJob(Job job) {
        todoList.add(job);
    }

    public int jobListSize() {
        return todoList.size() + currentJobs.size();
    }

    public Job assignJob() {
        if (this.todoList.size() == 0) {
            return null;
        } else {
            Job job = this.todoList.remove(0);
            currentJobs.add(job);
            return job;
        }
    }

    @Override
    public String toString() {
        return "World{" +
                "actors=" + actors +
                ", todoList=" + todoList +
                ", currentJobs=" + currentJobs +
                '}';
    }

    public AStar getAStar() {
        return new AStar(terrain);
    }

    public List<Actor> getActors() {
        return this.actors;
    }
}

class Dorf {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                mainLoop();
            }
        });
    }

    private static void mainLoop() {
        World w = new World(20, 20);

        w.addActor(new Actor("Sally", new Loc(12, 15)));

        w.addJob(new MoveToJob(1, 1));
        w.addJob(new MoveToJob(4, 1));

        WorldViewer viewer = new WorldViewer(w, w.getTerrain(), w.getActors());
        viewer.show();
    }
}