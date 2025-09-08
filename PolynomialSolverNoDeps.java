import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;

public class PolynomialSolverNoDeps {
    public static void main(String[] args) throws IOException {
        String path = (args.length>0)? args[0] : "input.json";
        String content = new String(Files.readAllBytes(Paths.get(path)));

        // find n and k
        int n = findIntField(content, "\"n\"");
        int k = findIntField(content, "\"k\"");
        if (k <= 0) {
            System.err.println("Couldn't find 'k' in JSON.");
            return;
        }

        // find all entries like "1": { "base": "10", "value": "4" }
        Pattern p = Pattern.compile("\"(\\d+)\"\\s*:\\s*\\{[^}]*?\"base\"\\s*:\\s*\"(\\d+)\"[^}]*?\"value\"\\s*:\\s*\"([0-9a-zA-Z]+)\"[^}]*?\\}", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(content);

        TreeMap<Integer, String[]> map = new TreeMap<>();
        while (m.find()) {
            int idx = Integer.parseInt(m.group(1));
            String base = m.group(2);
            String value = m.group(3);
            map.put(idx, new String[]{base, value});
        }

        if (map.size() == 0) {
            System.err.println("No root entries found. Check JSON format.");
            return;
        }

        // decode first k roots (by ascending numeric key)
        List<BigInteger> roots = new ArrayList<>();
        for (Map.Entry<Integer, String[]> e : map.entrySet()) {
            if (roots.size() >= k) break;
            String baseS = e.getValue()[0];
            String valS = e.getValue()[1];
            try {
                int base = Integer.parseInt(baseS);
                BigInteger dec = new BigInteger(valS, base);
                roots.add(dec);
            } catch (NumberFormatException ex) {
                System.err.println("Failed to parse value '" + valS + "' with base " + baseS + " for entry " + e.getKey());
                return;
            }
        }

        if (roots.size() < k) {
            System.err.println("Found only " + roots.size() + " roots but k=" + k);
            return;
        }

        System.out.println("Decoded roots (first " + k + "): " + roots);

        // Build polynomial coefficients (descending order). Start with [1] (degree 0)
        List<BigInteger> coeffs = new ArrayList<>();
        coeffs.add(BigInteger.ONE); // represents polynomial 1

        for (BigInteger r : roots) {
            coeffs = multiplyByFactor(coeffs, r.negate()); // multiply by (x - r)
        }

        // print coefficients (descending: highest-degree ... constant)
        System.out.println("Polynomial coefficients (descending): " + coeffs);
        System.out.println("Constant term c = " + coeffs.get(coeffs.size()-1));
    }

    private static int findIntField(String s, String fieldName) {
        Pattern p = Pattern.compile(fieldName + "\\s*[:\"]*\\s*(\\d+)");
        Matcher m = p.matcher(s);
        if (m.find()) return Integer.parseInt(m.group(1));
        return -1;
    }

    // coeffs: list of size m+1 representing degree m, index 0 => coeff of x^m, last => const
    private static List<BigInteger> multiplyByFactor(List<BigInteger> coeffs, BigInteger negRoot) {
        int oldSize = coeffs.size(); // m+1
        List<BigInteger> next = new ArrayList<>(Collections.nCopies(oldSize + 1, BigInteger.ZERO));
        for (int i = 0; i < oldSize; i++) {
            // multiply by x contribution
            next.set(i, next.get(i).add(coeffs.get(i)));
            // multiply by -r contribution (negRoot = -r)
            next.set(i + 1, next.get(i + 1).add(coeffs.get(i).multiply(negRoot)));
        }
        return next;
    }
}
