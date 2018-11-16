package aaa;

import java.io.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class Main {

    public static final String AUTHOR_PREFIX = "Author: ";
    public static final String DATE_PREFIX = "Date:   ";
    public static final String SOURCE_FILE_PREFIX = "--- ";
    public static final String TARGET_FILE_PREFIX = "+++ ";

    public static void main(String[] args) throws IOException {
        if (args == null || args.length < 2) {
            System.out.println("Usage java aaa.Main <path-to-git-repo> <path-to-csv-output-file> <exclusionPattern>");
        }
        String gitFolder = args[0];
        String outputFolder = args[1];
        String exclusionPattern = args.length >=2 ? args[2] : null;
        Process process = Runtime.getRuntime().exec("git --git-dir " + gitFolder + "\\.git log -p --reverse");
        BufferedReader gitOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));

        convertToCsv(gitOutput, outputFolder, exclusionPattern);
    }

    private static void convertToCsv(BufferedReader gitOutput, String targetFileFolder, String exclusionPattern) throws IOException {
        String currentLine;
        Commit commit = null;
        BufferedWriter commitsWriter = new BufferedWriter(new FileWriter(targetFileFolder + "\\Commits.csv"));
        commitsWriter.write("User,Date,Total LOC,LOC Added,LOC Removed,Files\r\n");
        BufferedWriter weekWriter = new BufferedWriter(new FileWriter(targetFileFolder + "\\Weeks.csv"));
        weekWriter.write("Date,Total LOC\r\n");
        BufferedWriter daysWriter = new BufferedWriter(new FileWriter(targetFileFolder + "\\Days.csv"));
        daysWriter.write("Date,Total LOC\r\n");
        String week = null;
        String day = null;
        File file = null;
        String previousLine = null;
        while ((currentLine = gitOutput.readLine()) != null) {
            if (currentLine.startsWith("commit ")) {

                writeCommitLine(commitsWriter, commit);
                commit = new Commit(commit != null ? commit.getTotalLinesOfCode() : 0);
            }
            if (currentLine.startsWith(AUTHOR_PREFIX)) {
                commit.setAuthor(getAuthor(currentLine));
            }
            if (currentLine.startsWith(DATE_PREFIX)) {
                commit.setDateTime(getDateTime(currentLine));
                if (isDifferentWeek(week, commit)) {
                    writeDateAndTotalLoc(weekWriter, commit);
                    week = getWeekString(commit);
                }
                if (isDifferentDay(day, commit)) {
                    writeDateAndTotalLoc(daysWriter, commit);
                    day = getDateString(commit);
                }
            }
            if (isAddedLineOfCode(currentLine) && !file.shouldBeExcluded(exclusionPattern)) {
                commit.incrementLinesAdded();
            }
            if (isRemovedLineOfCode(currentLine) && !file.shouldBeExcluded(exclusionPattern)) {
                commit.incrementLinesRemoved();
            }
            if (isSourceFileLine(currentLine, previousLine)) {
                file = getFile(currentLine);
            }
            if (isTargetFileLine(currentLine, previousLine)) {
                file.setTargetFileName(getFileString(currentLine));
                if (!file.shouldBeExcluded(exclusionPattern)) {
                    commit.addFile(file);
                } else {
                    System.out.println("Excluding [" + file.getSourceFileName()+ "] [" + file.getTargetFileName()+ "]");
                }
            }
            previousLine = currentLine;
        }
        commitsWriter.close();
        weekWriter.close();
        daysWriter.close();
    }

    private static boolean isTargetFileLine(String currentLine, String previousLine) {
        return currentLine.startsWith(TARGET_FILE_PREFIX)  && previousLine.startsWith(SOURCE_FILE_PREFIX);
    }

    private static boolean isSourceFileLine(String currentLine, String previousLine) {
        return currentLine.startsWith(SOURCE_FILE_PREFIX) && previousLine.startsWith("index ");
    }

    private static String getFileString(String s) {
        return s.substring(s.indexOf("/"));
    }

    private static File getFile(String fileLine) {
        try {
            return new File(getFileString(fileLine));
        } catch (Exception e) {
            System.out.println("Could not extract file name from " + fileLine);
            throw e;
        }
    }

    private static String getDateString(Commit commit) {
        return commit.getDateTime().format(DateTimeFormatter.ofPattern("yyyy MM dd"));
    }

    private static boolean isDifferentWeek(String week, Commit commit) {
        return week == null || !week.equals(getWeekString(commit));
    }

    private static boolean isDifferentDay(String day, Commit commit) {
        return day == null || !day.equals(getDateString(commit));
    }

    private static String getWeekString(Commit commit) {
        return commit.getDateTime().format(DateTimeFormatter.ofPattern("yyyy w"));
    }

    private static boolean isRemovedLineOfCode(String gitLine) {
        return gitLine.startsWith("-") && !gitLine.startsWith("---");
    }

    private static boolean isAddedLineOfCode(String gitLine) {
        return gitLine.startsWith("+") && !gitLine.startsWith(TARGET_FILE_PREFIX);
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
                    + commit.getLinesAdded() + ","
                    + commit.getLinesRemoved() + ","
                    + commit.getChangedFiles()
                    + "\r\n");
        }
    }

    private static void writeDateAndTotalLoc(BufferedWriter writer, Commit commit) throws IOException {
        if (commit != null) {
            writer.write(
                    commit.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ","
                    + commit.getTotalLinesOfCode() + ","
                    + "\r\n");
        }
    }
}
