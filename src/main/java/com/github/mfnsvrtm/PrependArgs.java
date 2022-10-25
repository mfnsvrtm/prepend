package com.github.mfnsvrtm;

import picocli.CommandLine;

import java.nio.file.Path;

class PrependArgs {
    int extraLineCount = 0;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.Option(names = {"-t", "--targets"}, paramLabel = "<targets-path>",
            description = "Path to target list")
    Path targetListPath = Path.of("targets.txt");

    @CommandLine.Option(names = {"-c", "--copyright"}, paramLabel = "<copyright-path>",
            description = "Path to copyright notice")
    Path copyrightNoticePath = Path.of("copyright.txt");

    @CommandLine.Option(names = {"-b", "-r", "--root-dir", "--base-dir"}, paramLabel = "<root-path>",
            description = "Root directory for relative targets")
    Path rootDirectoryPath = null;

    @CommandLine.Option(names = {"-a", "--add-lines"}, paramLabel = "<line-count>",
            description = "Number of empty lines to be appended to the notice")
    void setExtraLineCount(int value) {
        if (value < 0) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                    String.format("Invalid value '%s' for option '--add-lines': " +
                            "only non-negative integers are allowed", value));
        }
        extraLineCount = value;
    }

    @CommandLine.Option(names = {"-e", "--line-ending"}, paramLabel = "<line-ending>",
            description = "Line ending of the empty lines created by --add-lines. One of ${COMPLETION-CANDIDATES}")
    LineEnding lineEnding = LineEnding.NIX;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true,
            description = "Display this help message")
    boolean usageHelpRequested;
}
