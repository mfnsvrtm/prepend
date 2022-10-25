package com.github.mfnsvrtm;

import picocli.CommandLine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@CommandLine.Command(name = "prepend", description = "Prepend copyright notice to a list of files", sortOptions = false)
public class Main implements Callable<Integer> {
    private Integer extraLineCount = 0;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @CommandLine.Option(names = {"-t", "--targets"}, paramLabel = "<targets-path>",
            description = "Path to target list")
    private Path fileListPath = Path.of("targets.txt");

    @CommandLine.Option(names = {"-c", "--copyright"}, paramLabel = "<copyright-path>",
            description = "Path to copyright notice")
    private Path copyrightNoticePath = Path.of("copyright.txt");

    @CommandLine.Option(names = {"-b", "-r", "--root-dir", "--base-dir"}, paramLabel = "<root-path>",
            description = "Root directory for relative targets")
    private Path rootDirectoryPath = null;

    @CommandLine.Option(names = {"-a", "--add-lines"}, paramLabel = "<line-count>",
            description = "Number of empty lines to be appended to the notice")
    private void setExtraLineCount(Integer value) {
        if (value < 0) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                    String.format("Invalid value '%s' for option '--add-lines': " +
                            "only non-negative integers are allowed", value));
        }
        extraLineCount = value;
    }

    @CommandLine.Option(names = {"-e", "--line-ending"}, paramLabel = "<line-ending>",
            description = "Line ending of the empty lines created by --add-lines. One of ${COMPLETION-CANDIDATES}")
    private LineEnding lineEnding = LineEnding.NIX;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true,
            description = "Display this help message")
    private boolean usageHelpRequested;

    private static int processedCount = 0;

    public static void main(String[] args) {
        CommandLine cl = new CommandLine(new Main()).setExecutionExceptionHandler(new PrependerExceptionHandler())
                .setCaseInsensitiveEnumValuesAllowed(true);
        int exitCode = cl.execute(args);
        if (exitCode == CommandLine.ExitCode.SOFTWARE) {
            System.out.printf("%d files processed.%n", processedCount);
        } else if (exitCode == CommandLine.ExitCode.OK && !cl.isUsageHelpRequested()) {
            System.out.printf("Success. %d files processed.%n", processedCount);
        }
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws PrependerException {
        List<Path> targets;
        byte[] copyright;

        try {
            targets = Files.lines(fileListPath).filter(Predicate.not(String::isBlank)).map(Path::of)
                    .collect(Collectors.toList());
            copyright = Files.readAllBytes(copyrightNoticePath);
        } catch (IOException e) {
            throw new PrependerException("Couldn't locate target list or copyright notice. " +
                    "Make sure targets \"targets.txt\" and \"copyright.txt\" are present in the current directory " +
                    "or pass one (or both) of -t <target list path> and -c <copyright path> flags.");
        } catch (InvalidPathException e) {
            throw new PrependerException("Target file contains invalid paths.");
        }

        if (extraLineCount != null) {
            copyright = addEmptyLines(copyright, extraLineCount, lineEnding);
        }

        if (rootDirectoryPath != null) {
            setRoot(targets, rootDirectoryPath);
        }

        // I don't know if this is of any value, but I thought it would be a good idea to exit early
        // and not make any modifications if one of the target paths is invalid
        checkPaths(targets);

        var buffer = new ByteArrayOutputStream();
        for (Path target : targets) {
            buffer.writeBytes(copyright);
            try {
                Files.newInputStream(target).transferTo(buffer);
                Files.write(target, buffer.toByteArray());
            } catch (IOException e) {
                throw new PrependerException(String.format("Encountered and IO exception while processing %s. " +
                        "Make sure no other process is using the file.", target));
            }
            processedCount++;
            buffer.reset();
        }

        return CommandLine.ExitCode.OK;
    }

    private static void setRoot(List<Path> pathList, Path root) {
        var iterator = pathList.listIterator();
        while (iterator.hasNext()) {
            var target = iterator.next();
            iterator.set(root.toAbsolutePath().resolve(target));
        }
    }

    private static byte[] addEmptyLines(byte[] buffer, int count, LineEnding lineEnding) throws PrependerException {
        String ending = lineEnding.getString();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(buffer);
            out.write(ending.repeat(count).getBytes());
            return out.toByteArray();
        } catch (IOException e) {
            throw new PrependerException("Runtime exception.");
        }
    }

    private static void checkPaths(List<Path> targets) throws PrependerException  {
        for (Path target : targets) {
            if (!Files.exists(target))
                throw new PrependerException(String.format("%s path is invalid. File does not exist.", target));
        }
    }
}

