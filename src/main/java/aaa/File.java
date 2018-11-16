package aaa;

import java.util.regex.Pattern;

public class File {

    public String getSourceFileName() {
        return sourceFileName;
    }

    private String sourceFileName;
    private String targetFileName;

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
        return exclusionPattern != null &&
                (Pattern.matches(exclusionPattern, targetFileName) || Pattern.matches(exclusionPattern, sourceFileName));
    }
}
