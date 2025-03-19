package dev.cqb13.crosswordgenerator.generator;

import dev.cqb13.crosswordgenerator.generator.setup.GridSetup;
import dev.cqb13.crosswordgenerator.generator.setup.Word;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.FutureTask;
import java.util.function.Function;

public class Generator {
//    private final ArrayList<Word> wordList;
    private ArrayList<ArrayList<Character>> starterGrid;
    private final ArrayList<WordPlacement> wordPlacements;
    private int lastDepth = -1;
    private final ProgressBar progressBar;

    public Generator(ArrayList<Word> wordList, GridSetup gridSetup, ArrayList<ArrayList<Character>> starterGrid, ProgressBar progressBar) {
        this.progressBar = progressBar;
        System.out.println("Across: " + gridSetup.wordPlacementsAcross().size());
        System.out.println("Down: " + gridSetup.wordPlacementsDown().size());
        gridSetup.wordPlacementsAcross().sort(new Comparator<WordPlacement>() {
            @Override
            public int compare(WordPlacement o1, WordPlacement o2) {
                return Double.compare(Math.sqrt(o1.x() * o1.x() + o1.y() * o1.y()), Math.sqrt(o2.x() * o2.x() + o2.y() * o2.y()));
            }
        });
        gridSetup.wordPlacementsDown().sort(new Comparator<WordPlacement>() {
            @Override
            public int compare(WordPlacement o1, WordPlacement o2) {
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

        for (WordPlacement wordPlacement : wordPlacements) {
            for (Word word : wordList) {
                if (wordFitsCharacterRequirements(word.getWord(), wordPlacement.wordCharacter())) {
                    wordPlacement.words().add(word);
                }
            }

            if (wordPlacement.words().isEmpty()) {
                System.out.format("No words can fit in the word at x: %s y:%s", wordPlacement.x(), wordPlacement.y());
                System.exit(1);
            }
        }

//        this.wordList = wordList;
        this.starterGrid = starterGrid;
    }

    private static boolean wordFitsCharacterRequirements(String word, ArrayList<Character> characterRequirements) {
        char[] splitWord = word.toCharArray();

        if (splitWord.length != characterRequirements.size()) {
            return false;
        }

        for (int i = 0; i < splitWord.length; i++) {
            if (characterRequirements.get(i) == '-') continue;
            if(!characterRequirements.get(i).toString().equals(characterRequirements.get(i).toString().toLowerCase())) throw new RuntimeException("its uppercase");

            if (characterRequirements.get(i) != splitWord[i]) {
                return false;
            }
        }


        return true;
    }

    public void generate(int gridsToGenerate, Function<ArrayList<ArrayList<ArrayList<Character>>>, Void> callback) {
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
            callback.apply(new ArrayList<>());
            return;
        }

        updateProgressBar(0, grids, gridsToGenerate, copy, callback);
    }

    private void generateGrid(int i, ArrayList<ArrayList<ArrayList<Character>>> grids, int gridsToGenerate, ArrayList<ArrayList<Character>> copy, Function<ArrayList<ArrayList<ArrayList<Character>>>, Void> callback) {
        boolean success = tryWord(0, deepClone(copy));

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
            updateProgressBar(i, grids, gridsToGenerate, copy, callback);
        }
    }

    private FutureTask<Void> updateProgressBar(int i, ArrayList<ArrayList<ArrayList<Character>>> grids, int total, ArrayList<ArrayList<Character>> copy, Function<ArrayList<ArrayList<ArrayList<Character>>>, Void> callback) {
        FutureTask<Void> updateProgressBarTask = new FutureTask<Void>(() -> {
            progressBar.setProgress((i + 1.0) / total);

            if(i + 1 == total) {
                callback.apply(grids);
            } else {
                generateGrid(i + 1, grids, total, copy, callback);
            }
        }, null);

        Platform.runLater(updateProgressBarTask);


        return updateProgressBarTask;
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
        WordPlacement currentWord = wordPlacements.get(wordIndex);

        for (Word word : wordPlacements.get(wordIndex).words()) {
            if (word.getWord().length() < currentWord.length()) break;
            if (word.getWord().length() != currentWord.length()) continue;
            if (word.getUsed()) continue;

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

            if (!wordWorks) {
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
