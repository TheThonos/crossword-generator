package dev.cqb13.crosswordgenerator.generator.setup;

import dev.cqb13.crosswordgenerator.generator.WordDetails;

import java.util.ArrayList;

public record GridSetup(int shortestWord, int longestWord, ArrayList<WordDetails> wordPlacementsAcross, ArrayList<WordDetails> wordPlacementsDown) { }
