import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class Main {
    
    public static void main(String[] args) {
        try {
            if (args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    System.out.println("=== Test Case " + (i + 1) + " (from " + args[i] + ") ===");
                    BigInteger secret = solveFromJsonFile(args[i]);
                    System.out.println("Secret (c): " + secret);
                    System.out.println();
                }
            } else {
                String[] testFiles = {"testcase1.json", "testcase2.json"};
                
                for (int i = 0; i < testFiles.length; i++) {
                    File file = new File(testFiles[i]);
                    if (file.exists()) {
                        System.out.println("=== Test Case " + (i + 1) + " (from " + testFiles[i] + ") ===");
                        BigInteger secret = solveFromJsonFile(testFiles[i]);
                        System.out.println("Secret (c): " + secret);
                        System.out.println();
                    } else {
                        System.out.println("File " + testFiles[i] + " not found, skipping...");
                    }
                }
                
                if (!new File("testcase1.json").exists() && !new File("testcase2.json").exists()) {
                    System.out.println("Usage:");
                    System.out.println("1. Create JSON files named 'testcase1.json' and 'testcase2.json' in the current directory");
                    System.out.println("2. Or run: java Main <file1.json> <file2.json> ...");
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static BigInteger solveFromJsonString(String jsonString) throws Exception {
        Map<String, Object> jsonData = parseJsonString(jsonString);
        return solve(jsonData);
    }
    
    public static BigInteger solveFromJsonFile(String filePath) throws Exception {
        String jsonContent = readFileAsString(filePath);
        return solveFromJsonString(jsonContent);
    }
    
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseJsonString(String json) {
        Map<String, Object> result = new HashMap<>();
        json = json.trim().replaceAll("\\s+", "");
        json = json.substring(1, json.length() - 1);
        List<String> keyValuePairs = splitByComma(json);
        
        for (String pair : keyValuePairs) {
            String[] keyValue = pair.split(":", 2);
            String key = keyValue[0].replaceAll("\"", "").trim();
            String value = keyValue[1].trim();
            
            if (value.startsWith("{") && value.endsWith("}")) {
                result.put(key, parseJsonString(value));
            } else {
                value = value.replaceAll("\"", "");
                result.put(key, value);
            }
        }
        
        return result;
    }
    
    private static List<String> splitByComma(String json) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int braceLevel = 0;
        boolean inQuotes = false;
        
        for (char c : json.toCharArray()) {
            if (c == '"' && (current.length() == 0 || current.charAt(current.length() - 1) != '\\')) {
                inQuotes = !inQuotes;
            }
            if (!inQuotes) {
                if (c == '{') {
                    braceLevel++;
                } else if (c == '}') {
                    braceLevel--;
                }
            }
            if (c == ',' && braceLevel == 0 && !inQuotes) {
                parts.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        if (current.length() > 0) {
            parts.add(current.toString().trim());
        }
        
        return parts;
    }
    
    private static String readFileAsString(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
    
    @SuppressWarnings("unchecked")
    public static BigInteger solve(Map<String, Object> data) throws Exception {
        Map<String, Object> keys = (Map<String, Object>) data.get("keys");
        int n = Integer.parseInt((String) keys.get("n"));
        int k = Integer.parseInt((String) keys.get("k"));
        
        List<Point> points = new ArrayList<>();
        
        for (String key : data.keySet()) {
            if (key.equals("keys")) {
                continue;
            }
            Map<String, Object> pointData = (Map<String, Object>) data.get(key);
            int x = Integer.parseInt(key);
            int base = Integer.parseInt((String) pointData.get("base"));
            String encodedValue = (String) pointData.get("value");
            BigInteger y = decodeValue(encodedValue, base);
            points.add(new Point(BigInteger.valueOf(x), y));
        }
        
        points.sort((p1, p2) -> p1.x.compareTo(p2.x));
        List<Point> selectedPoints = points.subList(0, Math.min(k, points.size()));
        BigInteger secret = lagrangeInterpolation(selectedPoints, BigInteger.ZERO);
        
        return secret;
    }
    
    public static BigInteger decodeValue(String value, int base) {
        return new BigInteger(value, base);
    }
    
    public static BigInteger lagrangeInterpolation(List<Point> points, BigInteger target) {
        BigInteger result = BigInteger.ZERO;
        
        for (int i = 0; i < points.size(); i++) {
            Point pi = points.get(i);
            BigInteger term = pi.y;
            for (int j = 0; j < points.size(); j++) {
                if (i != j) {
                    Point pj = points.get(j);
                    BigInteger numerator = target.subtract(pj.x);
                    BigInteger denominator = pi.x.subtract(pj.x);
                    term = term.multiply(numerator).divide(denominator);
                }
            }
            result = result.add(term);
        }
        
        return result;
    }
    
    static class Point {
        BigInteger x;
        BigInteger y;
        
        Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }
}
