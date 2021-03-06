package ru.cttdev.tenischev.shallowwater;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by kris13 on 06.01.17.
 */
public class Water {

    static final double G = 9.8;
    static final double INIT_HEIGHT = 2.0;

    private static final int NUMBER_OF_THREADS = 4;
    private static final boolean parallelism = true;

    private static double dt = .005;
    private static double dx = .2;
    private static double dy = .2;

    private ArrayList<FirstStep> firstSteps;
    private ArrayList<SecondStep> secondSteps;
    private CyclicBarrier barrier;
    private ExecutorService executorService;


    VectorU[][] u;
    List<Double> energies;

    private int size;

    private VectorU[][] uHalfX;
    private VectorU[][] uHalfY;

    private Random random = new Random();

    public Water(int size) {
        this.size = size;
        if (parallelism && size > 500) {
            final AtomicInteger counter = new AtomicInteger(0);
            executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS, new java.util.concurrent.ThreadFactory() {
                        @Override
                        public Thread newThread(Runnable r) {
                            return new Thread(r, String.valueOf(counter.getAndIncrement()));
                        }
                    });
            barrier = new CyclicBarrier(NUMBER_OF_THREADS);
            firstSteps = new ArrayList<>();
            secondSteps = new ArrayList<>();
            for (int i = 0; i < NUMBER_OF_THREADS; i++) {
                firstSteps.add(new FirstStep());
                secondSteps.add(new SecondStep());
            }
        }

        energies = new ArrayList<>();

        u = new VectorU[size][size];
        uHalfX = new VectorU[size - 1][size];
        uHalfY = new VectorU[size][size - 1];

        initWater();
        energies.add(calculateEnergy(u));
    }

    private void initWater() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                u[i][j] = new VectorU();
                if (i + 1 < size)
                    uHalfX[i][j] = new VectorU();
                if (j + 1 < size)
                    uHalfY[i][j] = new VectorU();
                u[i][j].h = INIT_HEIGHT;
                u[i][j].uh = 0;
                u[i][j].vh = 0;
            }
        }
    }

    void nextStep() throws InterruptedException {
        energies.add(calculateEnergy(u));

        if (executorService != null) {
            executorService.invokeAll(firstSteps);
            barrier.reset();
        } else {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (i + 1 < size) {
                        uHalfX[i][j] = u[i][j].add(u[i + 1][j]).multiply(0.5).subtract(u[i + 1][j].f().subtract(u[i][j].f()).multiply(dt / (2 * dx)));
                    }
                    if (j + 1 < size) {
                        uHalfY[i][j] = u[i][j].add(u[i][j + 1]).multiply(0.5).subtract(u[i][j + 1].g().subtract(u[i][j].g()).multiply(dt / (2 * dy)));
                    }
                }
            }
        }

        if (executorService != null) {
            executorService.invokeAll(secondSteps);
            barrier.reset();
        } else {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (i + 1 == size) {
                        u[i][j] = u[0][j];
                    } else if (j + 1 == size) {
                        u[i][j] = u[i][0];
                    } else {
                        u[i][j] = u[i][j].subtract(uHalfX[i][j].f().subtract(uHalfX[(i - 1 + uHalfX.length) % uHalfX.length][j].f()).multiply(dt / dx))
                                .subtract(uHalfY[i][j].g().subtract(uHalfY[i][(j - 1 + uHalfX.length) % uHalfX.length].g()).multiply(dt / dy));
                    }
                }
            }
        }
    }

    private class FirstStep implements Callable<Object> {
        @Override
        public Object call() throws Exception {
            double shift = Integer.parseInt(Thread.currentThread().getName()) * (size * 1d / NUMBER_OF_THREADS);
            for (int i = (int) shift; i < shift + (size * 1d / NUMBER_OF_THREADS); i++) {
                for (int j = 0; j < size; j++) {
                    if (i + 1 < size) {
                        uHalfX[i][j] = u[i][j].add(u[i + 1][j]).multiply(0.5).subtract(u[i + 1][j].f().subtract(u[i][j].f()).multiply(dt / (2 * dx)));
                    }
                    if (j + 1 < size) {
                        uHalfY[i][j] = u[i][j].add(u[i][j + 1]).multiply(0.5).subtract(u[i][j + 1].g().subtract(u[i][j].g()).multiply(dt / (2 * dy)));
                    }
                }
            }
            barrier.await();
            return null;
        }
    }

    private class SecondStep implements Callable<Object> {
        @Override
        public Objects call() throws Exception {
            double shift = Integer.parseInt(Thread.currentThread().getName()) * (size * 1d / NUMBER_OF_THREADS);
            for (int i = (int) shift; i < shift + (size * 1d / NUMBER_OF_THREADS); i++) {
                for (int j = 0; j < size; j++) {
                    if (i + 1 == size) {
                        u[i][j] = u[0][j];
                    } else if (j + 1 == size) {
                        u[i][j] = u[i][0];
                    } else {
                        u[i][j] = u[i][j].subtract(uHalfX[i][j].f().subtract(uHalfX[(i - 1 + uHalfX.length) % uHalfX.length][j].f()).multiply(dt / dx))
                                .subtract(uHalfY[i][j].g().subtract(uHalfY[i][(j - 1 + uHalfX.length) % uHalfX.length].g()).multiply(dt / dy));
                    }
                }
            }
            barrier.await();
            return null;
        }
    }

    /**
     * Create pike on center of water surface.
     */
    public void createCenterPike() {
        int point = size / 2;
        createPike(point, point);
    }

    /**
     * Create pike on random place of water surface.
     */
    public void createRandomPike() {
        int point1 = random.nextInt(u.length);
        int point2 = random.nextInt(u.length);
        createPike(point1, point2);
    }

    private void createPike(int point1, int point2) {
        upInPoint(point1, point2, .3);

        upInPoint(point1 + 1, point2, .2);
        upInPoint(point1, point2 + 1, .2);
        upInPoint(point1 - 1, point2, .2);
        upInPoint(point1, point2 - 1, .2);

        upInPoint(point1 + 1, point2 + 1, .1);
        upInPoint(point1 - 1, point2 - 1, .1);
        upInPoint(point1 + 1, point2 - 1, .1);
        upInPoint(point1 - 1, point2 + 1, .1);
    }

    private void upInPoint(int x, int y, double z) {
        double p = (INIT_HEIGHT + z) * (INIT_HEIGHT + z) * G - u[x][y].h * u[x][y].h * G;
        u[x][y].h = INIT_HEIGHT + z;
        energies.set(0, energies.get(0) + p);
    }

    /**
     * Create line wave front on water surface.
     */
    public void createFront() {
        for (int i = 0; i < size; i++) {
            upInPoint(1, i, .1);
        }
    }

    /**
     * Create periodic wave on water surface.
     */
    public void createWave() {
        for (int i = 1; i < size; i ++) {
            for (int j = 0; j < size; j++) {
                upInPoint(i, j, Math.sin(i) * 0.1);
            }
        }
    }

    /**
     * Print current height of water in system output.
     *
     * @param h vector U
     */
    private static void printHeight(VectorU[][] h) {
        for (int i = 0; i < h.length; i++) {
            for (int j = 0; j < h[i].length; j++) {
                System.out.printf("%f ", h[i][j].h);
            }
            System.out.println();
        }
    }


    /**
     * Calculate energy of system. Assumption mass equals to height of water.
     *
     * @param u vector U
     * @return sum of kinetic and potential energy.
     */
    private static double calculateEnergy(VectorU[][] u) {
        double p = 0;
        double k = 0;
        for (int i = 0; i < u.length; i++) {
            for (int j = 0; j < u.length; j++) {
                p += u[i][j].h * u[i][j].h * G;
                k += (u[i][j].uh * u[i][j].uh / u[i][j].h + u[i][j].vh * u[i][j].vh / u[i][j].h) * 0.5;
            }
        }
        return p + k;
    }
}
