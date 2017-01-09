package ru.cttdev.tenischev.shallowwater;


import java.util.concurrent.ArrayBlockingQueue;

public class Main {


    public static void main(String[] args) {
        ArrayBlockingQueue<VectorU[][]> queue = new ArrayBlockingQueue<>(3);
        Water water = new Water(21);

        new Thread(() -> {
            try {
                while (true) {
                    long time = System.nanoTime();
                    queue.put(water.u);
                    water.nextStep();
                    System.out.println("Step by " + (System.nanoTime() - time) / 1000000.0 + " ms");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }).start();

        Display display = new Display(queue, water);
        display.setVisible(true);
    }
}
