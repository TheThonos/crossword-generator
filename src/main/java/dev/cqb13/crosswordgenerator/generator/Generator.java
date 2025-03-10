package dev.cqb13.crosswordgenerator.generator;

import dev.cqb13.crosswordgenerator.generator.setup.GridSetup;
import dev.cqb13.crosswordgenerator.generator.setup.Word;

import java.util.ArrayList;
import java.util.Comparator;

public class Generator {
    private final ArrayList<Word> wordList;
    private ArrayList<ArrayList<Character>> starterGrid;
    private final ArrayList<WordDetails> wordPlacements;
    private int lastDepth = -1;

    public Generator(ArrayList<Word> wordList, GridSetup gridSetup, ArrayList<ArrayList<Character>> starterGrid) {
        System.out.println("Across: " + gridSetup.wordPlacementsAcross().size());
        System.out.println("Down: " + gridSetup.wordPlacementsDown().size());
        gridSetup.wordPlacementsAcross().sort(new Comparator<WordDetails>() {
            @Override
            public int compare(WordDetails o1, WordDetails o2) {
                return Double.compare(Math.sqrt(o1.x() * o1.x() + o1.y() * o1.y()), Math.sqrt(o2.x() * o2.x() + o2.y() * o2.y()));
            }
        });
        gridSetup.wordPlacementsDown().sort(new Comparator<WordDetails>() {
            @Override
            public int compare(WordDetails o1, WordDetails o2) {
                return Double.compare(Math.sqrt(o1.x() * o1.x() + o1.y() * o1.y()), Math.sqrt(o2.x() * o2.x() + o2.y() * o2.y()));
            }
        });
        int wordsNum = gridSetup.wordPlacementsAcross().size() + gridSetup.wordPlacementsDown().size();
        wordPlacements = new ArrayList<>();

        System.out.println(gridSetup.wordPlacementsDown().size());
        System.out.println(gridSetup.wordPlacementsAcross().size());
        for (int i = 0; i < wordsNum; i++) {
            if ((i % 2 == 0 && !gridSetup.wordPlacementsAcross().isEmpty()) || gridSetup.wordPlacementsDown().isEmpty()) {
                wordPlacements.add(gridSetup.wordPlacementsAcross().getFirst());
                gridSetup.wordPlacementsAcross().removeFirst();
            } else {
                wordPlacements.add(gridSetup.wordPlacementsDown().getFirst());
                gridSetup.wordPlacementsDown().removeFirst();
            }
        }

        this.wordList = wordList;
        this.starterGrid = starterGrid;
    }

    public ArrayList<ArrayList<ArrayList<Character>>> generate(int gridsToGenerate) {
        ArrayList<ArrayList<ArrayList<Character>>> grids = new ArrayList<>();

        ArrayList<ArrayList<Character>> copy = deepClone(starterGrid);
        boolean success = tryWord(0, deepClone(copy));
        System.out.println("\n");

        for (ArrayList<Character> row : this.starterGrid) {
            for (Character box : row) {
                System.out.print(box + "  ");
            }
            System.out.println("|");
        }

        grids.add(starterGrid);
        lastDepth = -1;

        if (!success) {
            System.out.println("Failed to create a crossword for this grid");
            return grids;
        }

        for (int i = 0; i < gridsToGenerate - 1; i++) {
            success = tryWord(0, deepClone(copy));

            if (success) {
                System.out.println("\n");

                for (ArrayList<Character> row : this.starterGrid) {
                    for (Character box : row) {
                        System.out.print(box + "  ");
                    }
                    System.out.println("|");
                }

                grids.add(starterGrid);
                lastDepth = -1;
            }
        }

        return grids;
    }

    public static ArrayList<ArrayList<Character>> deepClone(ArrayList<ArrayList<Character>> grid) {
        ArrayList<ArrayList<Character>> newGrid = new ArrayList<>();
        for (ArrayList<Character> row : grid) {
            ArrayList<Character> newRow = new ArrayList<>(row);
            newGrid.add(newRow);
        }
        return newGrid;
    }

    private static void printGrid(ArrayList<ArrayList<Character>> grid) {
        for (ArrayList<Character> row: grid) {
            for (Character letter : row) {
                System.out.print(letter + "  ");
            }
            System.out.println("|");
        }
        System.out.println("\n");
    }

    private boolean tryWord(int wordIndex, ArrayList<ArrayList<Character>> oldGrid) {
        ArrayList<ArrayList<Character>> grid = deepClone(oldGrid);
        WordDetails currentWord = wordPlacements.get(wordIndex);
        if (lastDepth != wordIndex) {
            System.out.print("\r[" + "#".repeat(wordIndex + 1) + " ".repeat(wordPlacements.size() - wordIndex - 1) + "] " + (wordIndex + 1) + "/" + (wordPlacements.size()));
            lastDepth = wordIndex;
        }

        //TODO: in word limit finder, find the start of each word length and parse from there for the words
        for (Word word : wordList) {
            if (word.getWord().length() < currentWord.length()) break;
            if (word.getWord().length() != currentWord.length()) continue;

            // Make sure the word can be placed in the grid
            boolean wordWorks = true;
            char[] wordArray = word.getWord().toCharArray();

            int offsetX = 0;
            int offsetY = 0;
            for (Character character : wordArray) {
                Character charInBox = grid.get(currentWord.y() + offsetY).get(currentWord.x() + offsetX);
                if (charInBox != '-' && charInBox != character) {
                    wordWorks = false;
                    break;
                }

                if (currentWord.direction() == Direction.Down) {
                    offsetY++;
                } else {
                    offsetX++;
                }
            }

            if (!wordWorks || word.getUsed()) {
                continue;
            }

            word.toggleUsed();

            // Place the word in the grid
            if (currentWord.direction() == Direction.Across) {
                for (int x = currentWord.x(); x - currentWord.x() < currentWord.length(); x++) {
                    grid.get(currentWord.y()).set(x, wordArray[x - currentWord.x()]);
                }
            } else {
                for (int y = currentWord.y(); y - currentWord.y() < currentWord.length(); y++) {
                    grid.get(y).set(currentWord.x(), wordArray[y - currentWord.y()]);
                }
            }

            if (wordIndex + 1 >= wordPlacements.size()) {
                this.starterGrid = deepClone(grid);
                return true;
            }
            if (tryWord(wordIndex + 1, grid)) {
                return true;
            } else {
                grid = deepClone(oldGrid);
                word.toggleUsed();
                if (lastDepth != wordIndex) {
                    System.out.print("\r[" + "#".repeat(wordIndex + 1) + " ".repeat(wordPlacements.size() - wordIndex - 1) + "] " + (wordIndex + 1) + "/" + (wordPlacements.size()));
                    lastDepth = wordIndex;
                }
            }
        }

        return false;
    }
}
