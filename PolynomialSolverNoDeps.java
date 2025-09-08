import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;

public class PolynomialSolverNoDeps {

    public static void main(String[] args) throws IOException {
        // 1. Read JSON content
        String path = (args.length > 0) ? args[0] : "input.json";
        String content = new String(Files.readAllBytes(Paths.get(path)));

        // 2. Extract k
        int k = findIntField(content, "\"k\"");
        if (k <= 0) {
            System.err.println("Error: 'k' not found or invalid in JSON.");
            return;
        }

        // 3. Extract root entries
        Pattern entryPattern = Pattern.compile(
            "\"(\\d+)\"\\s*:\\s*\\{[^}]*?\"base\"\\s*:\\s*\"(\\d+)\"[^}]*?\"value\"\\s*:\\s*\"([0-9a-zA-Z]+)\"[^}]*?\\}",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        Matcher matcher = entryPattern.matcher(content);

        TreeMap<Integer, String[]> rootMap = new TreeMap<>();
        while (matcher.find()) {
            int idx = Integer.parseInt(matcher.group(1));
            String base = matcher.group(2);
            String value = matcher.group(3);
            rootMap.put(idx, new String[]{base, value});
        }

        if (rootMap.isEmpty()) {
            System.err.println("Error: No root entries found in JSON.");
            return;
        }

        // 4. Decode first k roots
        List<BigInteger> roots = new ArrayList<>();
        for (Map.Entry<Integer, String[]> entry : rootMap.entrySet()) {
            if (roots.size() >= k) break;
            try {
                int base = Integer.parseInt(entry.getValue()[0]);
                BigInteger decoded = new BigInteger(entry.getValue()[1], base);
                roots.add(decoded);
            } catch (NumberFormatException e) {
                System.err.println("Error: Failed to parse value '" +
                        entry.getValue()[1] + "' with base " + entry.getValue()[0]);
                return;
            }
        }

        if (roots.size() < k) {
            System.err.println("Error: Found only " + roots.size() + " roots but k=" + k);
            return;
        }

        // 5. Construct polynomial
        List<BigInteger> coefficients = new ArrayList<>();
        coefficients.add(BigInteger.ONE); // Start with "1"

        for (BigInteger root : roots) {
            coefficients = multiplyByFactor(coefficients, root);
        }

        // 6. Print only the constant term
        System.out.println(coefficients.get(coefficients.size() - 1));
    }

    private static int findIntField(String s, String fieldName) {
        Pattern p = Pattern.compile(fieldName + "\\s*[:\"]*\\s*(\\d+)");
        Matcher m = p.matcher(s);
        return (m.find()) ? Integer.parseInt(m.group(1)) : -1;
    }

    private static List<BigInteger> multiplyByFactor(List<BigInteger> coeffs, BigInteger root) {
        int oldSize = coeffs.size();
        List<BigInteger> result = new ArrayList<>(Collections.nCopies(oldSize + 1, BigInteger.ZERO));

        for (int i = 0; i < oldSize; i++) {
            result.set(i, result.get(i).add(coeffs.get(i))); // x term
            result.set(i + 1, result.get(i + 1).subtract(coeffs.get(i).multiply(root))); // -root term
        }
        return result;
    }
}
