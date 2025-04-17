package frostnox.nightfall.util.math;

public class Spline {
    private final Point[] points; //Spline points
    private final double[] inputsDiff, outputsDiff; //Differences between points
    
    public Spline(Point... points) {
        this.points = points;
        this.inputsDiff = new double[points.length == 0 ? 0 : points.length - 1];
        this.outputsDiff = new double[points.length == 0 ? 0 : points.length - 1];
        for(int i = 1; i < points.length; i++) {
            inputsDiff[i - 1] = Math.abs(points[i].input - points[i - 1].input);
            outputsDiff[i - 1] = points[i].output - points[i - 1].output;
        }
    }

    public double fit(double input) {
        for(int i = 1; i < points.length; i++) {
            if(input >= points[i - 1].input && input <= points[i].input) {
                //Start height + end height * progress
                return (points[i - 1].output + this.outputsDiff[i - 1] * points[i].easing.apply((input - points[i - 1].input) / this.inputsDiff[i - 1]));
            }
        }
        return input;
    }

    public static Point point(double noise, double height) {
        return point(noise, height, Easing.none);
    }

    public static Point point(double noise, double height, Easing easing) {
        return new Point(noise, height, easing);
    }

    public record Point(double input, double output, Easing easing) {

    }
}
