package basicMap;


public class Vector2d {
    // of course, also require the methods for adding
    // to these vectors

    public static void main(String[] args) {
        // main method for convenient testing
        Vector2d v = new Vector2d(10, 10);
        System.out.println(v.mag());
        v.normalise();
        System.out.println(v.mag());
    }

    public double x, y;

    public Vector2d() {
        this(0, 0);
    }

    public boolean equals(Object o) {
        if (o instanceof Vector2d) {
            Vector2d v = (Vector2d) o;
            return x == v.x && y == v.y;
        } else {
            return false;
        }
    }

    public Vector2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2d(Vector2d v) {
        this.x = v.x;
        this.y = v.y;
    }

    public Vector2d copy() {
        return new Vector2d(x, y);
    }

    public Vector2d set(Vector2d v) {
        this.x = v.x;
        this.y = v.y;
        return this;
    }

    public Vector2d set(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector2d zero() {
        x = 0.0;
        y = 0.0;
        return this;
    }

    public String toString() {
        return x + " : " + y;
    }

    public Vector2d add(Vector2d v) {
        this.x += v.x;
        this.y += v.y;
        return this;
    }

    public Vector2d add(double x, double y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vector2d add(Vector2d v, double w) {
        // weighted addition
        this.x += w * v.x;
        this.y += w * v.y;
        return this;
    }

    public Vector2d wrap(double w, double h) {
//        w = 2 * w;
//        h = 2 * h;
        x = (x + w) % w;
        y = (y + h) % h;
        return this;
    }

    public Vector2d subtract(Vector2d v) {
        this.x -= v.x;
        this.y -= v.y;
        return this;
    }

    public Vector2d subtract(double x, double y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Vector2d mul(double fac) {
        x *= fac;
        y *= fac;
        return this;
    }

    public Vector2d rotate(double theta) {
        // rotate this vector by the angle made to the horizontal by this line
        // theta is in radians
        double cosTheta = Math.cos(theta);
        double sinTheta = Math.sin(theta);

        double nx = x * cosTheta - y * sinTheta;
        double ny = x * sinTheta + y * cosTheta;

        x = nx;
        y = ny;
        return this;
    }

    //    public void rotate(Vector2d start, Vector2d end) {
//        // rotate this vector by the angle made to the horizontal by this line
//        double r = start.dist(end);
//        double cosTheta = (end.x - start.x) / r;
//        double sinTheta = (end.y - start.y) / r;
//
//        double nx = x * cosTheta - y * sinTheta;
//        double ny = x * sinTheta + y * cosTheta;
//
//        x = nx;
//        y = ny;
//    }
//
//    public void limit(double maxMag) {
//        double mag = this.mag();
//        if (mag > maxMag) {
//            this.mul(maxMag / mag);
//        }
//    }
//
//    public void setMag(double m) {
//        if (mag() != 0) { // can still blow up!
//            this.mul( m / mag() );
//        }
//    }
//
    public Vector2d contraRotate(Vector2d start, Vector2d end) {
        // rotate this vector by the opposite angle made to the horizontal by this line
        double r = start.dist(end);
        double cosTheta = (end.x - start.x) / r;
        double sinTheta = (end.y - start.y) / r;

        double nx = x * cosTheta + y * sinTheta;
        double ny = -x * sinTheta + y * cosTheta;

        x = nx;
        y = ny;
        return this;
    }

    public Vector2d contraRotate(Vector2d heading) {
        // rotate this vector by the opposite angle made to the vertical by this line
        double r = heading.mag();
        double cosTheta = heading.y / r;
        double sinTheta = heading.x / r;

        double nx = x * cosTheta + y * sinTheta;
        double ny = -x * sinTheta + y * cosTheta;

        x = nx;
        y = ny;
        return this;
    }

    public double scalarProduct(Vector2d v) {
        return x * v.x + y * v.y;
    }

    //    public void div(double den) {
//        x /= den;
//        y /= den;
//    }
//
    public static double sqr(double x) {
        return x * x;
    }

    public double sqDist(Vector2d v) {
        return sqr(x - v.x) + sqr(y - v.y);
    }

    public double mag() {
        return Math.sqrt(sqr(x) + sqr(y));
    }

    //    public double sqMag() {
//        return sqr( x ) + sqr( y );
//    }
//
    public double dist(Vector2d v) {
        return Math.sqrt(sqDist(v));
    }

    public double theta() {
        return Math.atan2(y, x);
    }

    public void normalise() {
        double mag = mag();
        x /= mag;
        y /= mag;
    }
}
