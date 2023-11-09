package org.example;

import java.util.*;
import java.util.stream.Collectors;

public class Main {

    record Mapping(String symbol, String mapping) {

    }

    public static final String WORD = "test123 test123 12";

    public static final String PLACEHOLDER = "_";
    public static final String MSG_ALGOS = """
            Method type is %s:
                - initial string was: %s,
                - after encoding it got this form: %s,
                - after decoding it is: %s.
            """;

    public static final String MSG_MEASURE = """
            The coef of compression is:\s
                - encoded:  %s,
                - with size:    %s
                - string:   %s,
                - with size:    %s
                - coef: %s
            """;
    public static final String ESCAPE_SYMBOL = "!";
    public static final String ZERO_PREF = "0.";

    private static Node findAndRemoveMin(List<Node> list) {
        if (list.isEmpty()) {
            return null;
        }

        var entry = list.get(0);
        for (int i = 0; i < list.size(); i++) {
            if (entry.weight() >= list.get(i).weight()) {
                entry = list.get(i);
            }
        }
        list.remove(entry);
        return entry;
    }

    public static List<Node> buildAlphabet(String codedWord) {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < codedWord.length(); i++) {
            var symbol = String.valueOf(codedWord.charAt(i));

            if (nodes.stream().anyMatch(n -> n.symbol().equals(symbol))) {
                var node = nodes.stream().filter(n -> n.symbol().equals(symbol)).findFirst().get();
                node.incrementWeight();
                continue;
            }
            nodes.add(new Node(symbol, 1, null, null));
        }
        return nodes;
    }

    public static Node buildTree(List<Node> nodes) {
        while (!nodes.isEmpty() && nodes.size() != 1) {
            var r = findAndRemoveMin(nodes);
            var l = findAndRemoveMin(nodes);
            nodes.add(new Node(l.symbol() + r.symbol(), l.weight() + r.weight(), l, r));
        }

        return nodes.get(0);
    }

    public static String encodeString(Node root, String codedWord) {
        String result = "";
        for (int i = 0; i < codedWord.length(); i++) {
            var symbol = String.valueOf(codedWord.charAt(i));
            result += root.findPath(symbol);

            if (i != codedWord.length() - 1) {
                result += PLACEHOLDER;
            }
        }


        return result;
    }

    public static String decodeString(Map<String, String> map, String encodedWord) {
        var result = encodedWord;


        var list = map.entrySet().stream()
                .map(e -> new Mapping(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        Collections.sort(list, (o1, o2) -> {
            return o2.mapping().length() - o1.mapping().length();
        });

        for (Mapping m : list) {
            var key = String.valueOf(m.symbol());
            result = result.replaceAll(map.get(key), key);
        }

        return result.replaceAll(PLACEHOLDER, "");

    }

    private static Map<String, String> createCoversationMap(Node root, String word) {
        var map = new HashMap<String, String>();
        for (int i = 0; i < word.length(); i++) {
            var symbol = String.valueOf(word.charAt(i));
            map.put(symbol, root.findPath(symbol));
        }
        return map;
    }

    private static Map<String, Segment> getProportionsMap(List<Node> alphabet, String codedWord) {
        Map<String, Segment> proportionsMap = new HashMap<>();
        var left = 0D;
        for (Node entry : alphabet) {
            var right = entry.weight() / (double) codedWord.length();
            proportionsMap.put(entry.symbol(), new Segment(left, right + left));
            left = left + right;
        }
        return proportionsMap;
    }

    public static Map<String, Segment> copy(Map<String, Segment> original) {
        Map<String, Segment> copy = new HashMap<>();
        original.forEach((key, value) -> copy.put(key, new Segment(value.getLeft(), value.getRight())));
        return copy;
    }

    public static String returnResult(double left, double right) {
        String l = String.valueOf(left).substring(2);
        String r = String.valueOf(right).substring(2);

        for (int i = 0; i < l.length(); i++) {
            if (l.charAt(i) != r.charAt(i)) {
                return r.substring(0, i + 1);
            }
        }

        return "";
    }

    public static void testHaffMethod() {
        var tree = buildTree(new ArrayList<>(buildAlphabet(WORD)));
        var encoded = encodeString(tree, WORD);
        var coef = encoded.split("_").length / (double) convertToBinary(WORD).split("_").length * 100.0;

        System.out.println(String.format(MSG_ALGOS, "HAFF", WORD,
                encoded.replace("_", " "),
                decodeString(createCoversationMap(tree, WORD), encoded)));
        System.out.println(String.format(MSG_MEASURE,
                encoded.replace("_", " "),
                encoded.length(),
                convertToBinary(WORD).replace("_", " "),
                convertToBinary(WORD).length(),
                coef));
    }

    public static void testArifMethod() {
        Map<String, Segment> proportionsMap = getProportionsMap(buildAlphabet(WORD + ESCAPE_SYMBOL), WORD + ESCAPE_SYMBOL);
        var encoded = arithmeticMethod(WORD + ESCAPE_SYMBOL, proportionsMap);
        var encodedBinary = Arrays.stream(encoded.chars().toArray()).mapToObj(e -> Integer.toBinaryString(e)).collect(Collectors.joining("_"));
        var coef = encodedBinary.split("_").length / (double) convertToBinary(WORD).split("_").length * 100.0;

        System.out.println(String.format(MSG_ALGOS, "ARIF", WORD,
                encoded,
                decodeArithmetic(encoded, proportionsMap)));
        System.out.println(String.format(MSG_MEASURE,
                encodedBinary.replace("_", " "),
                encodedBinary.replace("_", " ").length(),
                convertToBinary(WORD).replace("_", " "),
                convertToBinary(WORD).length(),
                coef));
    }

    private static String decodeArithmetic(String number, Map<String, Segment> proportionsMap) {
        var fullNumber = Double.valueOf(ZERO_PREF + number);

        var symbol = "";
        var result = "";
        Map<String, Segment> currentMap = copy(proportionsMap);
        while (!symbol.equals(ESCAPE_SYMBOL)) {
            for (var entry : currentMap.entrySet()) {
                var key = entry.getKey();
                var segment = entry.getValue();

                if (segment.getLeft() < fullNumber && segment.getRight() > fullNumber) {
                    symbol = key;
                    recalculateProportionsMap(proportionsMap, currentMap, segment);
                    if (symbol.equals(ESCAPE_SYMBOL)) {
                        break;
                    }

                    result += symbol;


                }
            }
        }

        return result;
    }

    public static void recalculateProportionsMap(Map<String, Segment> proportionsMap, Map<String, Segment> currentMap, Segment segment) {
        var leftStep = segment.getLeft();
        var newLength = segment.getRight() - segment.getLeft();
        for (var entry : currentMap.entrySet()) {
            var current = entry.getValue();
            var initialProps = proportionsMap.get(entry.getKey());
            var left = newLength * initialProps.getLeft() + leftStep;
            var right = newLength * initialProps.getRight() + leftStep;
            current.setLeft(left);
            current.setRight(right);
        }
    }

    private static String arithmeticMethod(String codedWord, Map<String, Segment> proportionsMap) {
        Map<String, Segment> currentMap = copy(proportionsMap);
        for (int i = 0; i < codedWord.length(); i++) {
            var symbol = String.valueOf(codedWord.charAt(i));
            var curSegment = currentMap.get(symbol);
            if (i == codedWord.length() - 1) {
                return returnResult(curSegment.getLeft(), curSegment.getRight());
            }
            recalculateProportionsMap(proportionsMap, currentMap, curSegment);
        }

        return "";
    }

    public static String convertToBinary(String input) {
        StringBuilder binary = new StringBuilder();
        for (int i = 0; i < input.toCharArray().length; ++i) {
            char c = input.charAt(i);
            binary.append(Integer.toBinaryString(c));

            if (i != input.length() - 1) {
                binary.append("_");
            }

        }
        return binary.toString();
    }

    public static void main(String[] args) {
        testHaffMethod();
        testArifMethod();
    }
}