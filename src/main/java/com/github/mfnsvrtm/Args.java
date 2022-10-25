package com.github.mfnsvrtm;

import com.beust.jcommander.Parameter;

import java.nio.file.Path;

class Args {
    @Parameter(names = {"-t", "--targets"}, description = "path to target list", order = 0)
    Path fileListPath = Path.of("targets.txt");

    @Parameter(names = {"-c", "--copyright"}, description = "path to copyright notice", order = 1)
    Path copyrightNoticePath = Path.of("copyright.txt");

    @Parameter(names = {"-b", "-r", "--root-dir", "--base-dir"}, description = "root directory for relative targets", order = 2)
    Path rootDirectoryPath = null;

    @Parameter(names = {"-a", "--add-lines"}, description = "number of empty lines to be appended to the notice",
            validateValueWith = NonNegativeIntegerValidator.class, order = 3)
    Integer extraLineCount = 0;

    @Parameter(names = {"-e", "--line-ending"}, description = "line ending of the empty lines created by --add-lines", order = 4)
    LineEnding lineEnding = LineEnding.NIX;

    @Parameter(names = {"-h", "--help"}, help = true)
    boolean help;
}
