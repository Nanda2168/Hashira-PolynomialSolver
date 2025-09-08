import java.io.*;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.*;
import java.util.*;

public class PolynomialSolver {

    public static void main(String[] args) throws Exception {
        // 1. Read JSON file
        String path = (args.length > 0) ? args[0] : "input.json";
        String content = new String(Files.readAllBytes(Paths.get(path)));

        // 2. Parse n and k
        int n = extractInt(content, "\"n\"");
        int k = extractInt(content, "\"k\"");

        // 3. Parse points
        List<BigInteger> xs = new ArrayList<>();
        List<BigInteger> ys = new ArrayList<>();

        for (int i = 1; i <= n; i++) {
            String baseKey = "\"base\"";
            String valueKey = "\"value\"";
            String section = extractSection(content, "\"" + i + "\"");
            int base = Integer.parseInt(extractString(section, baseKey));
            String valStr = extractString(section, valueKey);
            BigInteger value = new BigInteger(valStr, base);
            xs.add(BigInteger.valueOf(i));
            ys.add(value);
        }

        // 4. Lagrange interpolation to find c
        BigInteger c = lagrangeInterpolation(xs.subList(0, k), ys.subList(0, k));
        System.out.println("c = " + c);
    }

    // Extract integer after key
    private static int extractInt(String s, String key) {
        int idx = s.indexOf(key) + key.length() + 1;
        int end = s.indexOf(",", idx);
        if (end == -1) end = s.indexOf("}", idx);
        return Integer.parseInt(s.substring(idx, end).replaceAll("[^0-9]", ""));
    }

    // Extract string between quotes after key
    private static String extractString(String s, String key) {
        int idx = s.indexOf(key) + key.length() + 2;
        int start = s.indexOf("\"", idx) + 1;
        int end = s.indexOf("\"", start);
        return s.substring(start, end);
    }

    // Extract section for a given key
    private static String extractSection(String s, String key) {
        int start = s.indexOf(key);
        int braceCount = 0;
        int end = start;
        for (int i = start; i < s.length(); i++) {
            if (s.charAt(i) == '{') braceCount++;
            if (s.charAt(i) == '}') braceCount--;
            if (braceCount == 0 && i > start) { end = i + 1; break; }
        }
        return s.substring(start, end);
    }

    // Lagrange interpolation at x=0
    private static BigInteger lagrangeInterpolation(List<BigInteger> xs, List<BigInteger> ys) {
        BigDecimal result = BigDecimal.ZERO;
        for (int i = 0; i < xs.size(); i++) {
            BigInteger xi = xs.get(i);
            BigInteger yi = ys.get(i);
            BigInteger num = BigInteger.ONE;
            BigInteger den = BigInteger.ONE;
            for (int j = 0; j < xs.size(); j++) {
                if (i == j) continue;
                BigInteger xj = xs.get(j);
                num = num.multiply(xj.negate());
                den = den.multiply(xi.subtract(xj));
            }
            BigDecimal term = new BigDecimal(yi).multiply(new BigDecimal(num))
                    .divide(new BigDecimal(den), MathContext.DECIMAL128);
            result = result.add(term);
        }
        return result.toBigInteger();
    }
}
