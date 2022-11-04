package com.intro.extract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Extract data from given string
 * and arrange it by levels
 */
public class Solution {

    private static String testContent = "{ text1 { text2 } }"; // test data
    private static int testLevel = 1;

    public static void main(String[] args) throws Exception {
        // use string as input

        Pair pair = getConsoleContent();
        testLevel = pair.level;
        testContent = pair.text; // from console

        // testContent = Files.lines(Path.of("test.txt")).reduce(String::concat).orElse(""); // from file

        List<String> result = Result.extractData(testContent, testLevel);

        System.out.println(result);
    }

    public static void main1(String[] args) throws Exception {
        // use a stream as input to avoid OutOfMemory error, e.g. when we process a huge file

        // Stream<String> input = Files.lines(Path.of("test.txt")); // from file

        Stream<String> input = Pattern.compile("\\R").splitAsStream(testContent); // from string

        List<String> result = Result.extractData(input, testLevel);

        System.out.println(result);
    }

    public static void main2(String[] args) throws IOException {
        // use given string as input

        String content = testContent;
        int level = testLevel;

        // use console input
        // Pair pair = getConsoleContent();
        // level = pair.level;
        // content = pair.text;

        List<String> lines = Result.extractDataV1(content.replace("\\n", "\n"), level);
        List<String> finalLines = new ArrayList<>();
        for (String line : lines) {
            finalLines.add(line.trim());
        }

        System.out.print(finalLines);
    }

    private static Pair getConsoleContent() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String input = bufferedReader.readLine();
        String[] tokens = input.split("::");
        String content = tokens[0];
        int level = Integer.parseInt(tokens[1]);
        return new Pair(level, content);
    }

    static class Pair {
        int level;
        String text;

        public Pair(int level, String text) {
            this.level = level;
            this.text = text;
        }
    }

}
