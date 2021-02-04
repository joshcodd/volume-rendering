package models;

public class Vector {

    private int a;
    private int b;
    private int c;

    public Vector (int a, int b, int c){
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public void normalize(){
        double normalValue = Math.sqrt((getA()^2) + (getB()^2)
                + (getC()^2));
        setA((int) (getA() / normalValue));
        setB((int) (getB() / normalValue));
        setC((int) (getC() / normalValue));
    }

    public int dotProduct(Vector vector){
        int dotProduct;
        dotProduct = this.getA() * vector.getA();
        dotProduct = dotProduct + this.getB() * vector.getB();
        dotProduct = dotProduct + this.getC() * vector.getC();
        return dotProduct;
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }
}
