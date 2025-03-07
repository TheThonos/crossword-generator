package dev.cqb13.crosswordgenerator.generator.setup;

import dev.cqb13.crosswordgenerator.generator.Direction;
import dev.cqb13.crosswordgenerator.generator.WordDetails;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class WordLoader {
    public static GridSetup findLimits(ArrayList<ArrayList<Character>> crosswordGrid) {
        int crosswordWidth = crosswordGrid.getFirst().size();
        int crosswordHeight = crosswordGrid.size();

        int shortestWord = Math.max(crosswordWidth, crosswordHeight);
        int longestWord = 0;
        ArrayList<WordDetails> wordPlacementsAcross = new ArrayList<>();
        ArrayList<WordDetails> wordPlacementsDown = new ArrayList<>();


        for (int y = 0; y < crosswordGrid.size(); y++) {
            for (int x = 0; x < crosswordGrid.getFirst().size(); x++) {
                if(crosswordGrid.get(y).get(x) == '#') continue;
                // if we are on the edge of the board or there is a square to the left, starting a word, do a scan
                if (x == 0 || (crosswordGrid.get(y).get(x - 1) == '#' && (x != crosswordGrid.getFirst().size() - 1 && crosswordGrid.get(y).get(x + 1) != '#'))) {
                    int wordLength = 1;
                    // scan left till edge of board or #
                    for (int offset = 1; x + offset < crosswordGrid.getFirst().size() && crosswordGrid.get(y).get(x + offset) != '#'; offset++) {
                        wordLength++;
                    }

                    if (wordLength < shortestWord) {
                        shortestWord = wordLength;
                    }

                    wordPlacementsAcross.add(new WordDetails(x, y, wordLength, Direction.Across));
                }

                // if we are on the edge of the board or there is a square above starting a word, do a scan
                if (y == 0 || (crosswordGrid.get(y - 1).get(x) == '#' && (y != crosswordGrid.size() - 1 && crosswordGrid.get(y + 1).get(x) != '#'))) {
                    int wordLength = 1;
                    // scan down till edge of board or #
                    for (int offset = 1; y + offset < crosswordGrid.getFirst().size() && crosswordGrid.get(y + offset).get(x) != '#'; offset++) {
                        wordLength++;
                    }

                    if (wordLength > longestWord) {
                        longestWord = wordLength;
                    }

                    wordPlacementsDown.add(new WordDetails(x, y, wordLength, Direction.Down));
                }
            }
        }

        return new GridSetup(shortestWord, longestWord, wordPlacementsAcross, wordPlacementsDown);
    }

    public static ArrayList<Word> loadWordList(GridSetup gridSetup, int frequency) throws IOException {
        ArrayList<Word> wordList = new ArrayList<>();

        ArrayList<Integer> validWordLengths = new ArrayList<>();


        ArrayList<WordDetails> bigList = (ArrayList<WordDetails>) gridSetup.wordPlacementsAcross().clone();
        bigList.addAll(gridSetup.wordPlacementsDown());
        bigList.forEach(wordDetails -> {
            if (!validWordLengths.contains(wordDetails.length())) {
                validWordLengths.add(wordDetails.length());
            }
        });
        Scanner scanner = new Scanner(new FileReader("src/main/resources/dev/cqb13/crosswordgenerator/words.txt"));

        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Word word = processLine(line, validWordLengths, frequency);

            if (word == null) {
                continue;
            }

            wordList.add(word);
        }

        return wordList;
    }

    private static Word processLine(String line, ArrayList<Integer> validWordLengths, int minFrequency) {
        String[] split = line.split(",");
        assert split.length == 3;

        String word = split[0];
        if (!validWordLengths.contains(Integer.parseInt(split[1]))) {
            return null;
        }

        if (
                word.endsWith("org") ||
                word.endsWith("xyz") ||
                word.contains("ww") ||
                word.contains("xx") ||
                word.contains("yy") ||
                word.contains("vv") || 
                word.endsWith("com")
        ) return null;

        int wordFrequency = Integer.parseInt(split[2]);
        if(wordFrequency < minFrequency) return null;

        return new Word(word, minFrequency, false);
    }
}
