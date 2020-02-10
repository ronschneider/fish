package com.ron.phys;

import java.util.List;

class Pos {
    private double x;
    private double y;

    public Pos(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Pos{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}

class Velocity {
    private double x;
    private double y;
    private double magnitude;

    public Velocity(double x, double y) {
        if ((x == 0) && (y == 0)) {
            x = y = 0;
        }
        this.magnitude = Math.sqrt(x*x + y*y);

        if (this.magnitude != 0) {
            this.x = x / this.magnitude;
            this.y = y / this.magnitude;
        }
    }

    @Override
    public String toString() {
        return "Velocity{" +
                "x=" + x +
                ", y=" + y +
                ", v=" + magnitude +
                '}';
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public static Velocity AT_REST = new Velocity(0, 0);
}

class Entity {
    String      label;
    Pos         pos;
    Velocity    vel;
    double      mass;

    public Entity(String label, double mass, Pos pos, Velocity vel) {
        this.label = label;
        this.mass = mass;
        this.pos = pos;
        this.vel = vel;
    }

    public Pos getPos() {
        return pos;
    }

    public void setPos(Pos pos) {
        this.pos = pos;
    }

    public Velocity getVel() {
        return vel;
    }

    public void setVel(Velocity vel) {
        this.vel = vel;
    }
}

class Force {

}

class Gravity extends Force {
    public static final double G = -9.807;          //Gravitational constant, same for everything

    void apply(Entity e, double timeslice) {
        // change in velocity due to gravity
        double velocity_delta = G * timeslice;

        // compute new velocity vector
        double x = e.vel.getX() * e.vel.getMagnitude() + 0;
        double y = e.vel.getY() * e.vel.getMagnitude() + velocity_delta;
        e.vel = new Velocity(x, y);

        // Update position
        double px = e.pos.getX() + timeslice * e.vel.getMagnitude() * e.vel.getX();
        double py = e.pos.getY() + timeslice * e.vel.getMagnitude() * e.vel.getY();
        e.pos = new Pos(px, py);

    }
}

class Simulation {
    List<Entity> entities;

}

class Rock extends Entity {
    public Rock(Pos x, Velocity y)
    {
        super("Rock", 1000, x, y);
    }
}

public class Phys {
    public static final double G = -9.807;          //Gravitational constant, same for everything

    public static void main(String[] args) {
        Phys main = new Phys();
        main.run();
    }

    public void run() {
        Rock rock = new Rock( new Pos(1000, 1000), new Velocity(0, 0));
        Gravity g = new Gravity();

        double expected = Math.sqrt(Math.abs(2 * 1000.0/G));

        System.out.println("We expect this to take " + String.format("%.2f", expected ));

        double time = 0;
        double dt = 0.1;
        int count = 10000;
        System.out.println(String.format("%.2f: %s", time, rock.getPos()));

        while ((rock.pos.getY() > 0) && (count > 0)){
            g.apply(rock, dt);

            count--;
            time += dt;

            System.out.println(String.format("%.2f: %s", time, rock.getPos()));
        }
    }
};





/*

distance d traveled by an object falling for time t == 0.5 * gt^2
time t taken to fall d, t == sqrt(2d/g)
 */