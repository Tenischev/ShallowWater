package ru.cttdev.tenischev.shallowwater;

import com.jogamp.opengl.util.Animator;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by kris13 on 06.01.17.
 */
public class Display extends Frame implements GLEventListener, KeyListener {

    private final Water water;
    private final ArrayBlockingQueue<VectorU[][]> queue;

    private Label manual1 = new Label();
    private Label manual2 = new Label();
    private Label energy = new Label();


    public Display(ArrayBlockingQueue<VectorU[][]> queue, Water water) {
        super("Shallow Water");
        this.queue = queue;
        this.water = water;

        addKeyListener(this);

        manual1.setText("P - Add pike in center");
        manual1.setBounds(100, 50, 200, 30);
        add(manual1);
        manual2.setText("R - Add pike in random point");
        manual2.setBounds(100, 80, 200, 30);
        add(manual2);
        energy.setBounds(100, 110, 200, 30);
        add(energy);

        setLayout(new BorderLayout());


        setSize(800, 800);
        setLocation(40, 40);

        setVisible(true);

        setupJOGL();
    }

    private void setEnergy() {
        int size = water.energies.size();
        double e = water.energies.get(0) - water.energies.get(size - 1);
        energy.setText(String.format("Energy loss: %f", e));
    }

    private void setupJOGL() {
        GLCanvas canvas = new GLCanvas();
        canvas.addGLEventListener(this);

        add(canvas, BorderLayout.CENTER);
        final Animator anim = new Animator(canvas);
        anim.start();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        anim.stop();
                        System.exit(0);
                    }
                }).start();
            }
        });
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL2 gl = (GL2) glAutoDrawable.getGL();

        gl.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {

    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL2 gl = (GL2) glAutoDrawable.getGL();

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity(); // Reset The View
        gl.glRotated(60, 1, 0, 0);
        gl.glRotated(60, 0, 0, 1);


        VectorU[][] u;
        double height = Water.INIT_HEIGHT;
        try {
            u = queue.take();
            setEnergy();
            gl.glScaled(1. / u.length, 1. / u.length, 1. / u.length);
            gl.glTranslated(-u.length / 2.0, -u.length / 2.0, 0);
            for (int i = 0; i < u.length - 1; i++) {
                for (int j = 0; j < u.length - 1; j++) {
                    gl.glBegin(GL2.GL_TRIANGLES);
                    double gap = (height - u[i][j].h) * 5.0;
                    gl.glColor3d(0.5 - gap, 0.5 - gap, 1);
                    gl.glVertex3d(i, j, gap);

                    gap = (height - u[i + 1][j].h) * 5.0;
                    gl.glColor3d(0.5 - gap, 0.5 - gap, 1);
                    gl.glVertex3d(i + 1, j, gap);

                    gap = (height - u[i + 1][j + 1].h) * 5.0;
                    gl.glColor3d(0.5 - gap, 0.5 - gap, 1);
                    gl.glVertex3d(i + 1, j + 1, gap);

                    gap = (height - u[i][j].h) * 5.0;
                    gl.glColor3d(0.5 - gap, 0.5 - gap, 1);
                    gl.glVertex3d(i, j, gap);

                    gap = (height - u[i][j + 1].h) * 5.0;
                    gl.glColor3d(0.5 - gap, 0.5 - gap, 1);
                    gl.glVertex3d(i, j + 1, gap);

                    gap = (height - u[i + 1][j + 1].h) * 5.0;
                    gl.glColor3d(0.5 - gap, 0.5 - gap, 1);
                    gl.glVertex3d(i + 1, j + 1, gap);
                    gl.glEnd();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        gl.glFlush();
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_P:
                water.createCenterPike();
                break;
            case KeyEvent.VK_R:
                water.createRandomPike();
                break;
            case KeyEvent.VK_F:
                water.createFront();
                break;
            case KeyEvent.VK_W:
                water.createWave();
                break;
        }
    }
}
