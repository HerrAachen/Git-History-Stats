package aaa;

import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Commit {
    private String author;
    private OffsetDateTime dateTime;
    private int linesAdded = 0;
    private int linesRemoved = 0;
    private int totalLinesOfCode;
    private List<File> filesChanged = new LinkedList<>();

    public Commit(int totalLinesOfCode) {
        this.totalLinesOfCode = totalLinesOfCode;
    }

    public int getTotalLinesOfCode() {
        return totalLinesOfCode;
    }


    public void incrementLinesAdded() {
        linesAdded++;
        totalLinesOfCode++;
    }

    public void incrementLinesRemoved() {
        linesRemoved++;
        totalLinesOfCode--;
    }

    public int getLinesAdded() {
        return linesAdded;
    }

    public int getLinesRemoved() {
        return linesRemoved;
    }

    public OffsetDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(OffsetDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void addFile(File file) {
        filesChanged.add(file);
    }

    public String getChangedFiles() {
        return filesChanged.stream().map(file -> file.getName()).collect(Collectors.joining(";"));
    }
}
