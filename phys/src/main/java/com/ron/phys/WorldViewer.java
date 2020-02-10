package com.ron.phys;
import javax.swing.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.sun.java.accessibility.util.AWTEventMonitor.addWindowListener;

public class WorldViewer {
    private final World world;
    private final Terrain terrain;
    private final List<Actor> actors;
    private JFrame frame;

    public WorldViewer(World w, Terrain terrain, List<Actor> actors) {
        this.world = w;
        this.terrain = terrain;
        this.actors = actors;
    }

    public void show() {
        JFrame f = new JFrame();
        this.frame = f;

        f.setLayout(new FlowLayout());
        f.setSize(new Dimension(500, 500));
        f.addComponentListener(new ComponentAdapter()
        {
            public void componentResized(ComponentEvent evt) {
                Component c = (Component)evt.getSource();
                c.invalidate();
            }
        });

        f.add(new MapFrame(world, terrain, actors));

        JButton b1 = new JButton("Step");
        b1.setSize(100, 20);
        b1.setVerticalTextPosition(AbstractButton.CENTER);
        b1.setHorizontalTextPosition(AbstractButton.CENTER);
        b1.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                tickButtonActionPerformed(actionEvent);
            }
        });
        f.add(b1);

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
    }


    public void tickButtonActionPerformed(ActionEvent e) {
        WorldViewer.this.world.tick();
        frame.repaint();
    };


    public class MapFrame extends JPanel {
        private final Terrain map;
        private final List<Actor> actors;
        private final World w;

        public MapFrame(World w, Terrain map, List<Actor> actors) {
            this.map = map;
            this.actors = actors;
            this.w = w;

            this.setPreferredSize(new Dimension(500,500));

//            Timer timer = new Timer(500, actionListener);
//            timer.setInitialDelay(0);
//            timer.start();
//
        }

        private Color[] colors = {
                Color.WHITE,
                Color.lightGray,
                Color.GRAY,
                Color.BLUE,
                Color.darkGray,
                Color.GREEN};

        private AlphaComposite makeComposite(float alpha) {
            int type = AlphaComposite.SRC_OVER;
            return(AlphaComposite.getInstance(type, alpha));
        }

        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            float alpha = 0.10f;

            double pix = Math.min(this.getWidth(), this.getHeight());
            int scale = (int) (pix/(double) map.getBounds()[0]);

            for (int x = 0; x < map.getBounds()[0]; x++) {
                for (int y = 0; y < map.getBounds()[1]; y++) {
                    paintSq(g, scale, x, y);
                }
            }

            for (Actor a : actors) {
                Job j = a.getCurrentJob();
                if (j instanceof MoveToJob) {
                    MoveToJob m = (MoveToJob) j;
                    Loc src = a.getLoc();
                    Loc dst = m.getDest();
                    AStar aStar = w.getAStar();

                    g.setColor(Color.BLACK);
                    drawTextAt(g, dst, scale, "X");
                    drawTextAt(g, a.getLoc(), scale, a.getName().substring(0,1));

                    Composite originalComposite = g2d.getComposite();
                    g2d.setComposite(makeComposite(alpha));
                    List<Loc> results = aStar.solve(src, dst);
                    if (results != null) {
                        for (Loc loc : results) {
                            paintSqColor(g, scale, loc.getX(), loc.getY(), Color.blue);
                        }
                    }

                    g2d.setComposite(originalComposite);
                }
            }
        }

        private void drawTextAt(Graphics g, Loc loc, int scale, String text) {
            Graphics2D g2d = (Graphics2D) g;
            FontMetrics fm = g.getFontMetrics();

            int x = loc.getX() * scale;
            int y = loc.getY() * scale;

            // Absolute...
            x += fm.stringWidth(text) + 2;
            y += ((scale - fm.getHeight()) / 2) + fm.getAscent();

            g2d.setPaint(Color.blue);
            g.drawString(text, x, y);
        }

        private void paintSq(Graphics g, int scale, int x, int y) {
            Color color = colors[map.get(x, y).getCode()];
            paintSqColor(g, scale, x, y, color);
        }

        private void paintSqColor(Graphics g, int scale, int x, int y, Color color) {
            Graphics2D g2d = (Graphics2D) g;

            g.setColor(color);
            g2d.fillRect(x * scale, y * scale, scale, scale);
        }
    }

}

