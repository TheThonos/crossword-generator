package dev.cqb13.crosswordgenerator.generator.setup;

public class Word {
    private final String word;
    private final int frequency;
    private boolean used;

    public Word(String word, int frequency, boolean used) {
        this.word = word;
        this.frequency = frequency;
        this.used = used;
    }

    public String getWord() {
        return this.word;
    }

    public int getFrequency() {
        return this.frequency;
    }

    public boolean getUsed() {
        return this.used;
    }

    public void toggleUsed() {
        this.used = !this.used;
    }
}
