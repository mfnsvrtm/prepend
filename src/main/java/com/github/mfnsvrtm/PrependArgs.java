package com.github.mfnsvrtm;

import java.nio.file.Path;

class PrependArgs {
    private final Path targetListPath;
    private final Path copyrightNoticePath;
    private final Path rootDirectoryPath;
    private final int extraLineCount;
    private final LineEnding lineEnding;

    public PrependArgs(Path targetListPath, Path copyrightNoticePath, Path rootDirectoryPath, int extraLineCount,
                       LineEnding lineEnding) {
        this.targetListPath = targetListPath;
        this.copyrightNoticePath = copyrightNoticePath;
        this.rootDirectoryPath = rootDirectoryPath;
        this.extraLineCount = extraLineCount;
        this.lineEnding = lineEnding;
    }

    public Path getTargetListPath() {
        return targetListPath;
    }

    public Path getCopyrightNoticePath() {
        return copyrightNoticePath;
    }

    public Path getRootDirectoryPath() {
        return rootDirectoryPath;
    }

    public int getExtraLineCount() {
        return extraLineCount;
    }

    public LineEnding getLineEnding() {
        return lineEnding;
    }
}
