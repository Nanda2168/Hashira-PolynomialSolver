import java.io.*;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.*;
import java.util.*;
import org.json.JSONObject;

public class PolynomialSolverr {

    public static void main(String[] args) throws Exception {
        // 1. Read JSON file (from argument or default)
        String path = (args.length > 0) ? args[0] : "input.json";
        String content = new String(Files.readAllBytes(Paths.get(path)));
        JSONObject json = new JSONObject(content);

        // 2. Extract n, k
        int n = json.getJSONObject("keys").getInt("n");
        int k = json.getJSONObject("keys").getInt("k");

        // 3. Collect points (x, y)
        List<BigInteger> xs = new ArrayList<>();
        List<BigInteger> ys = new ArrayList<>();

        for (String key : json.keySet()) {
            if (key.equals("keys")) continue;
            int base = Integer.parseInt(json.getJSONObject(key).getString("base"));
            String valStr = json.getJSONObject(key).getString("value");
            BigInteger value = new BigInteger(valStr, base);
            xs.add(new BigInteger(key));   // x = the index itself (1,2,3,...)
            ys.add(value);                 // y = decoded value
        }

        // 4. Use first k points to interpolate polynomial
        BigInteger c = lagrangeInterpolation(xs.subList(0, k), ys.subList(0, k));

        System.out.println("c = " + c);
    }

    // Lagrange interpolation at x = 0 to find constant term
    private static BigInteger lagrangeInterpolation(List<BigInteger> xs, List<BigInteger> ys) {
        BigDecimal result = BigDecimal.ZERO;

        for (int i = 0; i < xs.size(); i++) {
            BigInteger xi = xs.get(i);
            BigInteger yi = ys.get(i);

            // Compute L_i(0)
            BigInteger num = BigInteger.ONE;
            BigInteger den = BigInteger.ONE;

            for (int j = 0; j < xs.size(); j++) {
                if (i == j) continue;
                BigInteger xj = xs.get(j);
                num = num.multiply(xj.negate());
                den = den.multiply(xi.subtract(xj));
            }

            // Use BigDecimal division for large numbers
            BigDecimal term = new BigDecimal(yi)
                    .multiply(new BigDecimal(num))
                    .divide(new BigDecimal(den), MathContext.DECIMAL128);

            result = result.add(term);
        }

        return result.toBigInteger(); // convert back to BigInteger
    }
}
