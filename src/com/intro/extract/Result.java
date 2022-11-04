package com.intro.extract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


// Task: parse text
// Text sample:
//         { // level 1 block can be single only.
//              money Heist info // [level 1 text piece]
//              { // level 2 block. could be multiple
//                  the most important char: Professor // [level 2 text piece]
//              }
//              { // level 2 block
//                  another char is that of Berlin // [level 2 text piece]
//                  { // level 3 block
//                         Berlin is in charge of the money heist // [level 3 text piece]
//                  }
//                  another char is that of Moscow // [level 2 text piece]
//                  { // level 3 block
//                      Moscow is Danver's dad // [level 3 text piece]
//                  }
//                  another char is that of Rio // [level 2 text piece]
//                  { // level 3 block
//                      Rio is a programmer // [level 3 text piece]
//                      { // level 4 block
//                           Rio is also a decent hacker // [level 4 text piece]
//                           { // level 5 block
//                              Rio is quite happy to be a part of the heist // [level 5 text piece]
//                            }
//                      }
//                  }
//              }
//              { // level 2 block
//                  Another character is that of Denver // [level 2 text piece]
//              }
//          Jovanny geist info // [level 1 text piece]
//      }
//
// Input: text and level number
// Output: list of text pieces of correspond level
//
// Cases:
//      Input: above string and 1
//      Result: [money Heist info, Jovanny geist info]
//
//      Input: above string and 2
//      Result: [the most important char: Professor, another char is that of Berlin,
//               another char is that of Moscow, another char is that of Rio, Another character is that of Denver]
//
//      Input: the given string and 3
//      Result: [Berlin is in charge of the money heist, Moscow is Danver's dad, Rio is a programmer]
//
//      Input: the given string and 4
//      Result: [Rio is also a decent hacker]
//
//      Input: the given string and 5
//      Result: [Rio is quite happy to be a part of the heist]


public class Result {


    private static String CATCH_GROUP_PLAIN_TEXT_PIECE = "PLAINTEXTPIECE";
    private static String CATCH_GROUP_PLAIN_TEXT_FIRST_PIECE = "PLAINTEXTFIRSTPIECE";
    private static String CATCH_GROUP_TEXT = "TEXT";
    private static String CATCH_GROUP_BOX = "BOX";

    private static String devideByBlocksRegExp = "(?<" + CATCH_GROUP_TEXT + ">[\\w\n:' ]+)*(?<" + CATCH_GROUP_BOX + ">\\{[ \\w:'\n\\}\\{]*})*";
    private static Pattern devideByBlocksPattern = Pattern.compile(devideByBlocksRegExp);
    private static String regExpInternal = "(?<" + CATCH_GROUP_PLAIN_TEXT_PIECE + ">\\}[\\w:' ]+\\{)*";
    private static Pattern patternIntern = Pattern.compile(regExpInternal);
    private static String regExpInternalTextStart = "^(?<" + CATCH_GROUP_PLAIN_TEXT_FIRST_PIECE + ">[\\w][\\w :']+\\{)?";
    private static Pattern patternInternalTextStart = Pattern.compile(regExpInternalTextStart);

    /**
     * A brut force solution
     *      which makes the whole text parsing in spite of the level of data we wanna get
     *      To implement a quick exit the RuntimeException could be used, but it is also not a good practice
     *
     * We use open braces stack (instead of incrementing the counter) to keep a possibility of storing node objects
     * which holds a brace position for instance to implement a more complex logic
     */
    public static List<String> extractDataV3(Stream<String> input, int levelDesired) throws Exception {

        if (levelDesired < 1) throw new Exception("Given text is not valid");

        final Deque<String> openBracesStack = new LinkedList<>();
        final StringBuilder buffer = new StringBuilder(); // StringBuilder is quick but to provide thread safety use StringBuffer
        final Map<Integer, List<String>> result = new HashMap<>();

        Consumer<Integer> storeBuffer = (givenLevel) -> {
            List<String> lst = Optional.ofNullable(result.get(givenLevel)).orElse(new ArrayList<>());
            String currentBuffer = buffer.toString().trim();
            if (currentBuffer.length() > 0)
                lst.add(currentBuffer);
            result.put(givenLevel, lst);
            buffer.setLength(0);
        };

        try {
            input
                    .map(String::trim)
                    .map(String::toCharArray)
                    .flatMap(arr -> IntStream.range(0, arr.length).mapToObj(i -> arr[i]))
                    .forEach(character -> {
                        if (character.equals('{')) {
                            storeBuffer.accept(openBracesStack.size());
                            openBracesStack.push("{");
                        }
                        else if (character.equals('}')) {
                            if (openBracesStack.isEmpty()) { // validation
                                throw new RuntimeException(); // not a good practice but quick to implement
                            }
                            storeBuffer.accept(openBracesStack.size());
                            openBracesStack.poll();
                        } else {
                            buffer.append(character);
                        }
                    });
        }
        catch (Throwable e) {
            throw new Exception("Given text is not valid");
        }

        // Post-validation
        if (!openBracesStack.isEmpty()) {
            throw new Exception("Given text is not valid");
        }

        return Optional.ofNullable(result.get(levelDesired)).orElse(Collections.EMPTY_LIST);
    }

    public static List<String> extractDataV2(String hierarchicalData, int level) {

        // pre-processing
        String data = hierarchicalData.replaceFirst("\\s++$", "").trim();
        data = data.replaceAll("[\t]+", ""); //  remove TAB
        data = data.replaceAll("[\n]+", ""); //  remove New line
        data = data.replaceAll("[\s^\b]{2,100}", ""); //  remove extra spaces

        // validation
        if (data == null) return Collections.EMPTY_LIST;
        if (data.length() == 0) return Collections.EMPTY_LIST;
        if (level < 1) return Collections.EMPTY_LIST;

        return parse(data, level, 0, new HashMap<>());
    }

    /**
     * Solution based on regular expressions
     * Does not work properly
     */
    static List<String> parse(String block, int desiredLevel, int currentLevel, HashMap<Integer, List<String>> result) {
        // Step 1 - explode block by pattern (text)n + (box)m
        List<String> text = new ArrayList<>();
        List<String> blocks = new ArrayList<>();

        Matcher matcher = devideByBlocksPattern.matcher(block);
        while (matcher.find()) {
            /* solution 2 - use catch groups
            String textPiece = matcher.group(CATCH_GROUP_TEXT);
            String boxCought = matcher.group(CATCH_GROUP_BOX);

            if (textPiece != null) {
                text.add(textPiece);
            }

            if (boxCought != null) {
                boxCought = boxCought.subSequence(1, boxCought.length() - 1).toString();
                blocks.add(boxCought);
                fetchFirstTextPiece(boxCought, text);
                fetchOtherPlainTextPieces(boxCought, text);
            } */

            // solution 1 - does not use named groups
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String piece = matcher.group(i);
                if (piece != null) {
                    piece = piece.trim();
                    if (piece.startsWith("{") && piece.endsWith("}")) {
                        piece = piece.subSequence(1, piece.length() - 1).toString(); // remove braces: {}
                        blocks.add(piece);
                        // fetchFirstTextPiece(piece, text);
                        // fetchOtherPlainTextPieces(piece, text);
                    } else {
                        text.add(piece);
                    }
                }
            }
        }

        // Step 2 - store text
        List<String> list = Optional.ofNullable(result.get(currentLevel)).orElse(new ArrayList<>());
        if (!text.isEmpty()) {
            list.addAll(text);
            result.put(currentLevel, list);
        }

        // Step 3 - quick exit if possible
        if (currentLevel == desiredLevel) {
            return result.get(currentLevel);
        }

        // Step 4 - otherwise go deeper
        return blocks.stream()
                .map(stringBlock -> parse(stringBlock, desiredLevel, currentLevel + 1, result))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    static void fetchFirstTextPiece(String piece, List<String> pureText) {
        Matcher matcherExpInternalTextStart = patternInternalTextStart.matcher(piece);
        while (matcherExpInternalTextStart.find()) {
            String plainTextFirstPiece = matcherExpInternalTextStart.group(CATCH_GROUP_PLAIN_TEXT_FIRST_PIECE);
            if (plainTextFirstPiece != null) {
                pureText.add(plainTextFirstPiece.subSequence(0, plainTextFirstPiece.length() - 1).toString());
            }
        }
    }

    static void fetchOtherPlainTextPieces(String piece, List<String> pureText) {
        Matcher matcherIntern = patternIntern.matcher(piece);
        while (matcherIntern.find()) {
            String plaintext = matcherIntern.group(CATCH_GROUP_PLAIN_TEXT_PIECE);
            if (plaintext != null) {
                pureText.add(plaintext.subSequence(1, plaintext.length() - 1).toString());
            }
        }
    }
}
