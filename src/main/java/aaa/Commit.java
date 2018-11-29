package aaa;

import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Commit {
    private String author;
    private OffsetDateTime dateTime;
    private String commitMessage = "";
    private List<File> filesChanged = new LinkedList<>();

    public int getLinesAdded() {
        return filesChanged.stream().mapToInt(file -> file.getLinesAdded()).sum();
    }

    public int getLinesRemoved() {
        return filesChanged.stream().mapToInt(file -> file.getLinesRemoved()).sum();
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public void appendToMessage(String messageLine) {
        commitMessage += messageLine.trim() + " ";
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
        return filesChanged.stream().map(file -> {
            if (!file.getTargetFileName().contains("dev/null")) {
                return file.getTargetFileName();
            } else {
                return file.getSourceFileName();
            }
        }).collect(Collectors.joining(";"));
    }
}
