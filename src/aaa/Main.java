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
        String outputFolder = args[1];
        Process process = Runtime.getRuntime().exec("git --git-dir " + gitFolder + "\\.git log -p --reverse");
        BufferedReader gitOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));

        convertToCsv(gitOutput, outputFolder);
    }

    private static void convertToCsv(BufferedReader gitOutput, String targetFileFolder) throws IOException {
        String s;
        Commit commit = null;
        BufferedWriter commitsWriter = new BufferedWriter(new FileWriter(targetFileFolder + "\\Commits.csv"));
        BufferedWriter weekWriter = new BufferedWriter(new FileWriter(targetFileFolder + "\\Weeks.csv"));
        BufferedWriter daysWriter = new BufferedWriter(new FileWriter(targetFileFolder + "\\Days.csv"));
        String week = null;
        String day = null;
        while ((s = gitOutput.readLine()) != null) {
            if (s.startsWith("commit ")) {

                writeCommitLine(commitsWriter, commit);
                commit = new Commit(commit != null ? commit.getTotalLinesOfCode() : 0);
            }
            if (s.startsWith(AUTHOR_PREFIX)) {
                commit.setAuthor(getAuthor(s));
            }
            if (s.startsWith(DATE_PREFIX)) {
                commit.setDateTime(getDateTime(s));
                if (isDifferentWeek(week, commit)) {
                    writeDateAndTotalLoc(weekWriter, commit);
                    week = getWeekString(commit);
                }
                if (isDifferentDay(day, commit)) {
                    writeDateAndTotalLoc(daysWriter, commit);
                    day = getDateString(commit);
                }
            }
            if (isAddedLineOfCode(s)) {
                commit.incrementLinesAdded();
            }
            if (isRemovedLineOfCode(s)) {
                commit.incrementLinesRemoved();
            }
        }
        commitsWriter.close();
        weekWriter.close();
        daysWriter.close();
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

    private static void writeDateAndTotalLoc(BufferedWriter writer, Commit commit) throws IOException {
        if (commit != null) {
            writer.write(
                    commit.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ","
                    + commit.getTotalLinesOfCode() + ","
                    + "\r\n");
        }
    }
}
