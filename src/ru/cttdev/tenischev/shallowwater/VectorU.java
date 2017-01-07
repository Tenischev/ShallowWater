package ru.cttdev.tenischev.shallowwater;

import static ru.cttdev.tenischev.shallowwater.Water.G;

/**
 * Vector U for describe current system status. For more details see report.
 * <p>
 * Created by kris13 on 06.01.17.
 */
public class VectorU {
    public double h;
    public double uh;
    public double vh;

    public VectorU add(VectorU v) {
        VectorU u = new VectorU();
        u.h = this.h + v.h;
        u.uh = this.uh + v.uh;
        u.vh = this.vh + v.vh;
        return u;
    }

    public VectorU multiply(double value) {
        VectorU u = new VectorU();
        u.h = this.h * value;
        u.uh = this.uh * value;
        u.vh = this.vh * value;
        return u;
    }

    public VectorU subtract(VectorU v) {
        VectorU u = new VectorU();
        u.h = this.h - v.h;
        u.vh = this.vh - v.vh;
        u.uh = this.uh - v.uh;
        return u;
    }

    /**
     * Calculate vector F(U) where U is this.
     *
     * @return new vector F(U)
     */
    public VectorU f() {
        VectorU f = new VectorU();
        f.h = this.uh;
        f.uh = this.uh * this.uh / this.h + 0.5 * G * this.h * this.h;
        f.vh = this.vh * this.uh / this.h;
        return f;
    }


    /**
     * Calculate vector G(U) where U is this.
     *
     * @return new vector G(U)
     */
    public VectorU g() {
        VectorU g = new VectorU();
        g.h = this.vh;
        g.uh = this.vh * this.uh / this.h;
        g.vh = this.vh * this.vh / this.h + 0.5 * G * this.h * this.h;
        return g;
    }
}
