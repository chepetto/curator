package org.curator.core.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class VectorUtils {

    private VectorUtils() {
    }

    public static Double dot(final Double[] a, final Double[] b) {
        Double sum = 0d;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    public static Double norm(final Double[] v) {
        Double norm = 0d;
        for (int i = 0; i < v.length; i++) {
            norm += v[i] * v[i];
        }
        return Math.sqrt(norm);
    }

    public static Double cosine(final Double[] d, final Double[] q) {
        final Double result = dot(d, q) / (norm(d) * norm(q));
        if (result.isNaN()) {
            return 0d;
        }
        return result;
    }

    public static Double tanimoto(final Double[] d, final Double[] q) {
        final Double dot = dot(d, q);
        return dot / ((Math.pow(norm(d), 2) * Math.pow(norm(d), 2)) - dot);
    }

    public static Double distance(final Double[] d, final Double[] q) {
        return 1 - length(substract(d, q)) / length(substract(getZeros(d.length), getOnes(d.length)));
    }

    private static final Map<Integer, Double[]> ZEROS = new HashMap<Integer, Double[]>(3);
    private static final Map<Integer, Double[]> ONES = new HashMap<Integer, Double[]>(3);

    private static Double[] getZeros(final int length) {
        if (!ZEROS.containsKey(length)) {
            ZEROS.put(length, getValues(length, 0d));
        }
        return ZEROS.get(length);
    }

    private static Double[] getOnes(final int length) {
        if (!ONES.containsKey(length)) {
            ONES.put(length, getValues(length, 1d));
        }
        return ONES.get(length);
    }

    private static Double[] getValues(final int length, final Double d) {
        Double[] zeros = new Double[length];
        for (int i = 0; i < length; i++) {
            zeros[i] = d;
        }
        return zeros;
    }

    private static Double length(final Double[] a) {
        Double sum = 0d;
        for (Double v : a) {
            sum += Math.pow(v, 2d);
        }
        return Math.sqrt(sum);
    }

    private static Double[] substract(final Double[] a, final Double[] b) {
        Double[] d = new Double[a.length];
        for (int i = 0; i < a.length; i++) {
            d[i] = a[i] - b[i];
        }
        return d;
    }

    /*
      public static void main(String[] args) {
        Double[] max = new Double[] { 1d, 1d };
    //    System.out.println(cosine(max, new Double[] { 0.0, 0.7 }));
    //    System.out.println(cosine(max, new Double[] { 0.0, 0.5 }));
    //    System.out.println(cosine(max, new Double[] { 0.0, 0.3 }));
    //    System.out.println(cosine(max, new Double[] { 0.0, 0.1 }));
    //    System.out.println(distance(max, new Double[] { 0.0, 0.7 }));
    //    System.out.println(distance(max, new Double[] { 0.0, 0.5 }));
    //    System.out.println(distance(max, new Double[] { 0.0, 0.3 }));
    //    System.out.println(distance(max, new Double[] { 0.0, 0.1 }));
        System.out.println(distance(max, new Double[] { 1d, 1d }));
        System.out.println(distance(max, new Double[] { 0.9, 0.9 }));
        System.out.println(distance(max, new Double[] { 0.0, 0.9 }));
        System.out.println(distance(max, new Double[] { 0.7, 0.7 }));
        System.out.println(distance(max, new Double[] { 0.0, 0.7 }));
        System.out.println(distance(max, new Double[] { 0.5, 0.5 }));
        System.out.println(distance(max, new Double[] { 0.0, 0.5 }));
        System.out.println(distance(max, new Double[] { 0.3, 0.3 }));
        System.out.println(distance(max, new Double[] { 0.0, 0.3 }));
        System.out.println(distance(max, new Double[] { 0.1, 0.1 }));
        System.out.println(distance(max, new Double[] { 0.0, 0.1 }));
      }
    */

    public static String toString(final Double[] vector) {
        final StringBuffer b = new StringBuffer(vector.length * 4);
        b.append('[');
        final Iterator<Double> i = Arrays.asList(vector).iterator();
        while (i.hasNext()) {
            final Double d = i.next();
            b.append(((int) (d * 100)) / 100d);
            if (i.hasNext()) {
                b.append(", ");
            }
        }
        b.append(']');
        return b.toString();
    }
}
