package models;

/**
 * Represents a vector of 3 values.
 * @author Josh Codd.
 */
public class Vector {
    private double a;
    private double b;
    private double c;

    /**
     * Creates a vector.
     * @param a The first value in the vector.
     * @param b The second value in the vector.
     * @param c The third value in the vector.
     */
    public Vector (double a, double b, double c){
        this.a = a;
        this.b = b;
        this.c = c;
    }

    /**
     * Normalises the vector.
     */
    public void normalize(){
        double normalValue = Math.sqrt((getA() * getA()) + (getB() * getB())
                + (getC() * getC()));
        setA((getA() / normalValue));
        setB((getB() / normalValue));
        setC((getC() / normalValue));
    }

    /**
     * Gets the dot product of this and another vector.
     * @param vector The other vector of the dot product.
     * @return The dot product of both vectors.
     */
    public double dotProduct(Vector vector){
        double dotProduct;
        dotProduct = (this.getA() * vector.getA())
                + (this.getB() * vector.getB())
                + (this.getC() * vector.getC());
        return dotProduct;
    }

    /**
     * Gets the addition of this and another vector.
     * @param vector The other vector of the addition.
     * @return The addition of both vectors.
     */
    public Vector add(Vector vector){
        return new Vector(this.getA() + vector.getA(),
                this.getB() + vector.getB(), this.getC() + vector.getC());
    }

    /**
     * Gets the subtraction of this and another vector.
     * @param vector The other vector of the subtraction.
     * @return The subtraction of both vectors.
     */
    public Vector subtract(Vector vector){
        return new Vector(this.getA() - vector.getA(),
                this.getB() - vector.getB(), this.getC() - vector.getC());
    }

    /**
     * Gets the division of this and another vector.
     * @param vector The other vector of the division.
     * @return The division of both vectors.
     */
    public Vector divide(Vector vector){
        return new Vector(this.getA() / vector.getA(),
                this.getB() / vector.getB(), this.getC() / vector.getC());
    }

    /**
     * Gets the first value of the vector.
     * @return The first value.
     */
    public double getA() {
        return a;
    }

    /**
     * Sets the first value of the vector.
     * @param a The value to set to.
     */
    public void setA(double a) {
        this.a = a;
    }

    /**
     * Gets the second value of the vector.
     * @return The second value.
     */
    public double getB() {
        return b;
    }

    /**
     * Sets the second value of the vector.
     * @param b The value to set to.
     */
    public void setB(double b) {
        this.b = b;
    }

    /**
     * Gets the third value of the vector.
     * @return The third value.
     */
    public double getC() {
        return c;
    }

    /**
     * Sets the third value of the vector.
     * @param c The value to set to.
     */
    public void setC(double c) {
        this.c = c;
    }
}
