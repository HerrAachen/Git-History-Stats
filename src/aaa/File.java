package aaa;

import java.util.regex.Pattern;

public class File {
    private String name;
    private boolean excludeFromStatistics;

    public File(String name, String exclusionPattern) {
        this.name = name;
        excludeFromStatistics = shouldBeExcluded(exclusionPattern);
    }

    public boolean shouldBeExcludedFromStatistics() {
        return excludeFromStatistics;
    }

    private boolean shouldBeExcluded(String exclusionPattern) {
        boolean shouldBeExcluded = exclusionPattern != null && Pattern.matches(exclusionPattern, name);
        if (shouldBeExcluded) {
            System.out.println("Excluding " + name);
        }
        return shouldBeExcluded;
    }
}
