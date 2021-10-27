package org.superbiz.lockdemo.model;

/**
 * This is an immutable object that represents a single row in this database table:
 *
 * CREATE TABLE t (a INT NOT NULL, b INT) ENGINE = InnoDB;
 */
public class Row {

    private final int a;
    private final int b;

    public Row(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public int getA() {
        return a;
    }

    public int getB() {
        return b;
    }

    @Override
    public String toString() {
        return "Row{" +
                "a=" + a +
                ", b=" + b +
                '}';
    }
}
