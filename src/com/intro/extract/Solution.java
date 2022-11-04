package com.intro.extract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Extract data from given string
 * and arrange it by lavels
 */
public class Solution {

    // test data
    private static String testContent = """
                     {  
                       	  money Heist info  
                             {  
                      	    	the most important char: Professor
                             }
                             {  
                      		    another char is that of Berlin  
                                {  
                      			    Berlin is in charge of the money heist 
                                }
                      		    another char is that of Moscow 
                                { 
                      		    	Moscow is Danver's dad 
                                }
                      		    another char is that of Rio 
                                {  
                      			    Rio is a programmer  
                                     { 
                      				    Rio is also a decent hacker
                                      {  
                      				    	Rio is quite happy to be a part of the heist
                                         }
                                   }
                                }
                            }	 
                            {  
                      	 	 \s	Another character is that of Denver  
                            }
                            Jovanny geist info
                      }
            """;

    private static int testLevel = 1;

    public static void main(String[] args) throws Exception {
        // use a stream as input to avoid OutOfMemory error, e.g. when we process a huge file

        // Stream<String> input = Files.lines(Path.of("test.txt")); // from file

        Stream<String> input = Pattern.compile("\\R").splitAsStream(testContent); // from string

        List<String> result = Result.extractDataV3(input, testLevel);

        System.out.println(result);
    }

    public static void _main(String[] args) throws IOException {
        // use given string as input

        String content = testContent;
        int level = testLevel;

        // use console input
        // Pair pair = getConsoleContent();
        // level = pair.level;
        // content = pair.text;

        List<String> lines = Result.extractDataV2(content.replace("\\n", "\n"), level);
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
