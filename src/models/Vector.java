package models;

/**
 * Represents a vector of 3 values.
 * @author Josh Codd.
 */
public class Vector {
    private int a;
    private int b;
    private int c;

    /**
     * Creates a vector.
     * @param a The first value in the vector.
     * @param b The second value in the vector.
     * @param c The third value in the vector.
     */
    public Vector (int a, int b, int c){
        this.a = a;
        this.b = b;
        this.c = c;
    }

    /**
     * Normalises the vector.
     */
    public void normalize(){
        double normalValue = Math.sqrt((getA()^2) + (getB()^2)
                + (getC()^2));
        setA((int) (getA() / normalValue));
        setB((int) (getB() / normalValue));
        setC((int) (getC() / normalValue));
    }

    /**
     * Gets the dot product of this and another vector.
     * @param vector The second vector of the dot product.
     * @return The dot product of both vectors.
     */
    public int dotProduct(Vector vector){
        int dotProduct;
        dotProduct = this.getA() * vector.getA();
        dotProduct = dotProduct + this.getB() * vector.getB();
        dotProduct = dotProduct + this.getC() * vector.getC();
        return dotProduct;
    }

    /**
     * Gets the first value of the vector.
     * @return The first value.
     */
    public int getA() {
        return a;
    }

    /**
     * Sets the first value of the vector.
     * @param a The value to set to.
     */
    public void setA(int a) {
        this.a = a;
    }

    /**
     * Gets the second value of the vector.
     * @return The second value.
     */
    public int getB() {
        return b;
    }

    /**
     * Sets the second value of the vector.
     * @param b The value to set to.
     */
    public void setB(int b) {
        this.b = b;
    }

    /**
     * Gets the third value of the vector.
     * @return The third value.
     */
    public int getC() {
        return c;
    }

    /**
     * Sets the third value of the vector.
     * @param c The value to set to.
     */
    public void setC(int c) {
        this.c = c;
    }
}
