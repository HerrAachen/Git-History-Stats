package aaa;

import java.io.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static final String AUTHOR_PREFIX = "Author: ";
    public static final String DATE_PREFIX = "Date:   ";
    public static final String SOURCE_FILE_PREFIX = "--- ";
    public static final String TARGET_FILE_PREFIX = "+++ ";
    private static String outputFolder;

    public static void main(String[] args) throws IOException {
        if (args == null || args.length < 2) {
            System.out.println("Usage java aaa.Main <path-to-git-repo> <path-to-csv-output-file> <exclusionPattern>");
        }
        String gitFolder = args[0];
        outputFolder = args[1];
        String exclusionPattern = args.length >=2 ? args[2] : null;
        Process process = Runtime.getRuntime().exec("git --git-dir " + gitFolder + "\\.git log -p --reverse");
        BufferedReader gitOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        convertToCsv(gitOutput, exclusionPattern);
    }

    private static void convertToCsv(BufferedReader gitOutput, String exclusionPattern) throws IOException {
        String currentLine;
        Commit commit = null;
        BufferedWriter commitsWriter = new BufferedWriter(new FileWriter(outputFolder + "\\Commits.csv"));
        commitsWriter.write("User,Date,Total LOC,LOC Added,LOC Removed,Commit Message,Files\r\n");
        BufferedWriter weekWriter = new BufferedWriter(new FileWriter(outputFolder + "\\Weeks.csv"));
        weekWriter.write("Date,Total LOC\r\n");
        BufferedWriter daysWriter = new BufferedWriter(new FileWriter(outputFolder + "\\Days.csv"));
        daysWriter.write("Date,Total LOC\r\n");
        String week = null;
        String day = null;
        File file = null;
        String previousLine = null;
        int totalLoc = 0;
        boolean isCommitMessageRow = false;
        Map<String, Integer> type2Loc = new HashMap<>();
        while ((currentLine = gitOutput.readLine()) != null) {
            if (currentLine.startsWith("commit ")) {
                if (commit != null) {
                    totalLoc += commit.getLinesAdded() - commit.getLinesRemoved();
                }
                writeCommitLine(commitsWriter, commit, totalLoc);
                commit = new Commit();
            }
            if (isCommitMessageRow) {
                if (currentLine.isEmpty() || currentLine.startsWith("   ")) {
                    commit.appendToMessage(currentLine.replace(',', ' '));
                } else {
                    isCommitMessageRow = false;
                }
            }
            if (currentLine.startsWith(AUTHOR_PREFIX)) {
                commit.setAuthor(getAuthor(currentLine));
            }
            if (currentLine.startsWith(DATE_PREFIX)) {
                commit.setDateTime(getDateTime(currentLine));
                if (isDifferentWeek(week, commit)) {
                    writeDateAndTotalLoc(weekWriter, commit, totalLoc);
                    week = getWeekString(commit);
                }
                if (isDifferentDay(day, commit)) {
                    writeDateAndTotalLoc(daysWriter, commit, totalLoc);
                    day = getDateString(commit);
                }
                isCommitMessageRow = true;
            }
            if (isAddedLineOfCode(currentLine) && !file.shouldBeExcluded(exclusionPattern)) {
                file.incrementLinesAdded();
            }
            if (isRemovedLineOfCode(currentLine) && !file.shouldBeExcluded(exclusionPattern)) {
                file.incrementLinesRemoved();
            }
            if (isSourceFileLine(currentLine, previousLine)) {
                addLinesPerType(type2Loc, file);
                file = getFile(currentLine);
            }
            if (isTargetFileLine(currentLine, previousLine)) {
                file.setTargetFileName(getFileString(currentLine));
                if (!file.shouldBeExcluded(exclusionPattern)) {
                    commit.addFile(file);
                } else {
                    System.out.println("Excluding " + file);
                }
            }
            previousLine = currentLine;
        }
        commitsWriter.close();
        weekWriter.close();
        daysWriter.close();
        writeLocPerType(type2Loc);
    }

    private static void writeLocPerType(Map<String, Integer> type2Loc) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFolder + "\\FileTypes.csv"));
        writer.write("File Type,Lines of Code\r\n");
        for(Map.Entry<String, Integer> typeAndLoc: type2Loc.entrySet()) {
            Integer loc = typeAndLoc.getValue();
            if (loc != 0) {
                writer.write(typeAndLoc.getKey() + "," + loc + "\r\n");
            }
        }
        writer.close();
    }

    private static void addLinesPerType(Map<String, Integer> type2Loc, File file) {
        if (file != null) {
            Integer locForType = type2Loc.get(file.getType());
            if (locForType == null) {
                locForType = 0;
            }
            locForType += file.getLinesAdded();
            locForType -= file.getLinesRemoved();
            type2Loc.put(file.getType(), locForType);
        }
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

    private static void writeCommitLine(BufferedWriter fileWriter, Commit commit, int totalLoc) throws IOException {
        if (commit != null) {
            fileWriter.write(commit.getAuthor() + ","
                    + commit.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + ","
                    + totalLoc + ","
                    + commit.getLinesAdded() + ","
                    + commit.getLinesRemoved() + ","
                    + commit.getCommitMessage() + ","
                    + commit.getChangedFiles()
                    + "\r\n");
        }
    }

    private static void writeDateAndTotalLoc(BufferedWriter writer, Commit commit, int totalLoc) throws IOException {
        if (commit != null) {
            writer.write(
                    commit.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ","
                    + totalLoc + ","
                    + "\r\n");
        }
    }
}
