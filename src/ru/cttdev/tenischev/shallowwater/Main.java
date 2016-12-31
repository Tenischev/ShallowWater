package ru.cttdev.tenischev.shallowwater;


public class Main {

    private static final double G = 9.8;
    private static double dt = .01;
    private static double dx = .2;
    private static double dy = .2;

    public static void main(String[] args) {
        int steps = 100;
        int N = 11;
        U[][] u = new U[N][N];
        U[][] uHalfX = new U[N - 1][N];
        U[][] uHalfY = new U[N][N - 1];

        // init
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                u[i][j] = new U();
                if (i + 1 < N)
                uHalfX[i][j] = new U();
                if (j + 1 < N)
                uHalfY[i][j] = new U();
                u[i][j].h = 1;
                u[i][j].uh = 0;
                u[i][j].vh = 0;
            }
        }

        // Create pike
        int point = N / 2;
        u[point][point].h = 1.1;

        // Create front
        /*for (int i = 0; i < N; i++) {
            u[1][i].h = 1.1;
        }*/

        // Create wave
        /*for (int i = 1; i < N; i+=2) {
            for (int j = 0; j < N; j++) {
                u[i][j].h = 1.1;
            }
        }*/

        double[] energies = new double[steps];

        for (int step = 0; step < steps; step++) {
            energies[step] = calculateEnergy(u);

            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (i + 1 < N) {
                        uHalfX[i][j] = u[i][j].add(u[i + 1][j]).multiply(0.5).subtract(u[i + 1][j].f().subtract(u[i][j].f()).multiply(dt/(2*dx)));
                    }
                    if (j + 1 < N) {
                        uHalfY[i][j] = u[i][j].add(u[i][j + 1]).multiply(0.5).subtract(u[i][j + 1].g().subtract(u[i][j].g()).multiply(dt/(2*dy)));
                    }
                }
            }

            //printHeight(uHalfX);
            //printHeight(uHalfY);

            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (i + 1 == N) {
                        u[i][j] = u[0][j];
                    } else if (j + 1 == N) {
                        u[i][j] = u[i][0];
                    } else {
                        u[i][j] = u[i][j].subtract(uHalfX[i][j].f().subtract(uHalfX[(i - 1 + uHalfX.length) % uHalfX.length][j].f()).multiply(dt/dx))
                                .subtract(uHalfY[i][j].g().subtract(uHalfY[i][(j - 1 + uHalfX.length) % uHalfX.length].g()).multiply(dt/dy));
                    }
                }
            }
            printHeight(u);
            System.out.println();
        }
        for (int i = 0; i < energies.length; i++) {
            System.out.printf("%f ", energies[i]);
        }
        System.out.println();
        System.out.printf("Total Energy diff: %f", energies[steps - 1] - energies[0]);
    }

    private static double calculateEnergy(U[][] u) {
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

    private static void printHeight(U[][] h) {
        for (int i = 0; i < h.length; i++) {
            for (int j = 0; j < h[i].length; j++) {
                System.out.printf("%f ", h[i][j].h);
            }
            System.out.println();
        }
    }

    static class U {
        public double h;
        public double uh;
        public double vh;

        public U add(U v) {
            U u = new U();
            u.h = this.h + v.h;
            u.uh = this.uh + v.uh;
            u.vh = this.vh + v.vh;
            return u;
        }

        public U multiply(double value) {
            U u = new U();
            u.h = this.h * value;
            u.uh = this.uh * value;
            u.vh = this.vh * value;
            return u;
        }

        public U f() {
            U f = new U();
            f.h = this.uh;
            f.uh = this.uh * this.uh / this.h + 0.5 * G * this.h * this.h;
            f.vh = this.vh * this.uh / this.h;
            return f;
        }

        public U subtract(U v) {
            U u = new U();
            u.h = this.h - v.h;
            u.vh = this.vh - v.vh;
            u.uh = this.uh - v.uh;
            return u;
        }

        public U g() {
            U g = new U();
            g.h = this.vh;
            g.uh = this.vh * this.uh / this.h;
            g.vh = this.vh * this.vh / this.h + 0.5 * G * this.h * this.h;
            return g;
        }
    }
}
