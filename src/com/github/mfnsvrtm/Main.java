package com.github.mfnsvrtm;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Main {
    private static Path fileListPath;
    private static Path copyrightNoticePath;
    private static Path rootDirectoryPath;
    private static Integer extraLineCount;
    private static String lineEnding;

    private static int processedCount = 0;

    public static void main(String[] args) {
        try {
            run(args);
            System.out.printf("Success. %d files processed.%n", processedCount);
        } catch (PrependerException e) {
            System.out.printf("Error. %s%n", e.getMessage());
            System.out.printf("%d files processed.%n", processedCount);
        }
    }

    private static void run(String[] args) throws PrependerException {
        processArgs(args);

        List<Path> targets;
        byte[] copyright;

        try {
            targets = Files.lines(fileListPath).filter(Predicate.not(String::isBlank)).map(Path::of)
                    .collect(Collectors.toList());
            copyright = Files.readAllBytes(copyrightNoticePath);
        } catch (IOException e) {
            throw new PrependerException("Couldn't locate target list or copyright notice. " +
                    "Make sure targets \"targets.txt\" and \"copyright.txt\" are present in the current directory " +
                    "or pass one (or both) of -f <target list path> and -c <copyright path> flags.");
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
    }

    private static void processArgs(String[] args) throws PrependerException {
        List<String> argList = Arrays.asList(args);
        try {
            fileListPath = Path.of(getParameter("-t", argList).orElse("targets.txt"));
            copyrightNoticePath = Path.of(getParameter("-c", argList).orElse("copyright.txt"));
            rootDirectoryPath = getParameter("-r", argList).map(Path::of).orElse(null);
        } catch (InvalidPathException e) {
            throw new PrependerException("One of the -t -c -r flags has an invalid path.");
        }

        try {
            extraLineCount = getParameter("--add", argList).map(Integer::parseInt).orElse(null);
            lineEnding = getParameter("--ending", argList).orElse(null);
        } catch (NumberFormatException e) {
            throw new PrependerException("Given a non-integer value for the --add parameter.");
        }

        if (extraLineCount != null && extraLineCount <= 0) {
            throw new PrependerException("Given a zero or negative line count.");
        }
        if (lineEnding != null && !List.of("win", "nix", "max").contains(lineEnding)) {
            throw new PrependerException("Given an unknown line ending (only values \"win\"/\"nix\"/\"mac\" are supported).");
        }
    }

    private static Optional<String> getParameter(String name, List<String> argList) throws PrependerException {
        int flagIndex = argList.indexOf(name);
        if (flagIndex == -1) return Optional.empty();

        int valueIndex = flagIndex + 1;
        if (valueIndex >= argList.size())
            throw new PrependerException(String.format("%s flag provided with no value.", name));

        return Optional.of(argList.get(valueIndex));
    }

    private static void setRoot(List<Path> pathList, Path root) {
        var iterator = pathList.listIterator();
        while (iterator.hasNext()) {
            var target = iterator.next();
            iterator.set(root.toAbsolutePath().resolve(target));
        }
    }

    private static byte[] addEmptyLines(byte[] buffer, int count, String lineEnding) throws PrependerException {
        String ending;
        switch (lineEnding == null ? "nix" : lineEnding) {
            default: case "nix": ending = "\n"; break;
            case "win": ending = "\r\n"; break;
            case "mac": ending = "\r"; break;
        }

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(buffer);
            out.write(ending.repeat(count).getBytes());
            return out.toByteArray();
        } catch (IOException e) {
            throw new PrependerException("Runtime exception.");
        }
    }

    private static void checkPaths(List<Path> targets) throws PrependerException {
        for (Path target : targets) {
            if (!Files.exists(target))
                throw new PrependerException(String.format("%s path is invalid. File does not exist.", target));
        }
    }

    private static class PrependerException extends Exception {
        public PrependerException(String message) {
            super(message);
        }
    }
}
