package dev.cqb13.crosswordgenerator.generator;

import dev.cqb13.crosswordgenerator.generator.setup.Word;

import java.util.ArrayList;

public record WordPlacement(int x, int y, int length, Direction direction, ArrayList<Word> words, ArrayList<Character> wordCharacter, boolean hasInput) {
}
