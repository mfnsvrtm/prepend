package com.github.mfnsvrtm;

import com.github.rvesse.airline.HelpOption;
import com.github.rvesse.airline.annotations.AirlineModule;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.Parser;
import com.github.rvesse.airline.annotations.restrictions.ranges.IntegerRange;

import java.nio.file.Path;

@Command(name = "prepend", description = "Prepend copyright notice to a list of files")
@Parser(typeConverter = ExtendedTypeConverter.class)
public class PrependCommand {
    @AirlineModule
    HelpOption<PrependCommand> help;

    @Option(name = {"-t", "--targets"}, title = "targets-path", description = "Path to target list")
    Path fileListPath = Path.of("targets.txt");

    @Option(name = {"-c", "--copyright"}, title = "copyright-path", description = "Path to copyright notice")
    Path copyrightNoticePath = Path.of("copyright.txt");

    @Option(name = {"-b", "-r", "--root-dir", "--base-dir"}, title = "root-path", description = "Root directory for relative targets")
    Path rootDirectoryPath = null;

    @Option(name = {"-a", "--add-lines"}, title = "line-count", description = "Number of empty lines to be appended to the notice")
    @IntegerRange(min = 0)
    Integer extraLineCount = 0;

    @Option(name = {"-e", "--line-ending"}, title = "line-ending", description = "Line ending of the empty lines created by --add-lines")
    LineEnding lineEnding = LineEnding.NIX;
}
