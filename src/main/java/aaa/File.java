package aaa;

import java.util.regex.Pattern;

public class File {

    private int linesAdded = 0;
    private int linesRemoved = 0;

    public String getSourceFileName() {
        return sourceFileName;
    }

    private String sourceFileName;
    private String targetFileName;

    public void incrementLinesAdded() {
        linesAdded++;
    }

    public void incrementLinesRemoved() {
        linesRemoved++;
    }

    public int getLinesAdded() {
        return linesAdded;
    }

    public int getLinesRemoved() {
        return linesRemoved;
    }

    public void setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
    }

    public File(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }
    public String getTargetFileName() {
        return targetFileName;
    }

    public boolean shouldBeExcluded(String exclusionPattern) {
        String target = targetFileName.trim();
        String source = sourceFileName.trim();
        return exclusionPattern != null &&
                (Pattern.matches(exclusionPattern, target) ||
                        Pattern.matches(exclusionPattern, target.toLowerCase()) ||
                        Pattern.matches(exclusionPattern, source) ||
                        Pattern.matches(exclusionPattern, source.toLowerCase()));
    }

    private boolean isEmpty(String fileName) {
        return "/dev/null".equals(fileName);
    }

    public String getType() {
        String fileName;
        if (!isEmpty(sourceFileName)) {
            fileName = sourceFileName;
        } else {
            fileName = targetFileName;
        }
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase().trim();
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append(!isEmpty(sourceFileName)?"[" + sourceFileName + "] ":"");
        string.append(!isEmpty(targetFileName) && !sourceFileName.equals(targetFileName)?"[" + targetFileName+ "]":"");
        return string.toString();
    }
}
