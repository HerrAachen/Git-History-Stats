package aaa;

import java.io.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class Main {

    public static final String AUTHOR_PREFIX = "Author: ";
    public static final String DATE_PREFIX = "Date:   ";

    public static void main(String[] args) throws IOException {
        if (args == null || args.length != 2) {
            System.out.println("Usage java aaa.Main <path-to-git-repo> <path-to-csv-output-file>");
        }
        String gitFolder = args[0];
        String outputFile = args[1];
        Process process = Runtime.getRuntime().exec("git --git-dir " + gitFolder + "\\.git log -p --reverse");
        BufferedReader gitOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));

        convertToCsv(gitOutput, outputFile);
    }

    private static void convertToCsv(BufferedReader gitOutput, String targetFile) throws IOException {
        String s;
        Commit commit = null;
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(targetFile));
        while ((s = gitOutput.readLine()) != null) {
            if (s.startsWith("commit ")) {

                writeCommitLine(fileWriter, commit);
                commit = new Commit(commit !=null?commit.getTotalLinesOfCode():0);
            }
            if (s.startsWith(AUTHOR_PREFIX)) {
                commit.setAuthor(getAuthor(s));
            }
            if (s.startsWith(DATE_PREFIX)) {
                commit.setDateTime(getDateTime(s));
            }
            if (isAddedLineOfCode(s)) {
                commit.incrementLinesAdded();
            }
            if (isRemovedLineOfCode(s)) {
                commit.incrementLinesRemoved();
            }
        }
        fileWriter.close();
    }

    private static boolean isRemovedLineOfCode(String gitLine) {
        return gitLine.startsWith("-") && !gitLine.startsWith("---");
    }

    private static boolean isAddedLineOfCode(String gitLine) {
        return gitLine.startsWith("+") && !gitLine.startsWith("+++");
    }

    private static OffsetDateTime getDateTime(String dateTimeLine) {
        String dateTimeString = dateTimeLine.substring(DATE_PREFIX.length());
        return OffsetDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy Z"));
    }

    private static String getAuthor(String authorLine) {
        return authorLine.substring(AUTHOR_PREFIX.length(), authorLine.indexOf(" <"));
    }

    private static void writeCommitLine(BufferedWriter fileWriter, Commit commit) throws IOException {
        if (commit != null) {
            fileWriter.write(commit.getAuthor() + ","
                    + commit.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + ","
                    + commit.getTotalLinesOfCode() + ","
                    + "\r\n");
        }
    }
}
