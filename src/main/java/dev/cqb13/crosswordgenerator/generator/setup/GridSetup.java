package dev.cqb13.crosswordgenerator.generator.setup;

import dev.cqb13.crosswordgenerator.generator.WordPlacement;

import java.util.ArrayList;

public record GridSetup(int shortestWord, int longestWord, ArrayList<WordPlacement> wordPlacementsAcross, ArrayList<WordPlacement> wordPlacementsDown) { }
