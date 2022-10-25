package com.github.mfnsvrtm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class Prepender {
    private final PrependArgs args;
    private int processedTargetCount = 0;

    Prepender(PrependArgs args) {
        this.args = args;
    }

    public int getProcessedTargetCount() {
        return processedTargetCount;
    }

    void run() throws PrependException {
        List<Path> targets;
        byte[] copyright;

        try {
            targets = Files.lines(args.fileListPath).filter(Predicate.not(String::isBlank)).map(Path::of)
                    .collect(Collectors.toList());
            copyright = Files.readAllBytes(args.copyrightNoticePath);
        } catch (IOException e) {
            throw new PrependException("Couldn't locate target list or copyright notice. " +
                    "Make sure targets \"targets.txt\" and \"copyright.txt\" are present in the current directory " +
                    "or pass one (or both) of -t <target list path> and -c <copyright path> flags.");
        } catch (InvalidPathException e) {
            throw new PrependException("Target file contains invalid paths.");
        }

        copyright = appendEmptyLines(copyright, args.extraLineCount, args.lineEnding);

        if (args.rootDirectoryPath != null) {
            resolveAll(targets, args.rootDirectoryPath);
        }

        // I don't know if this is of any value, but I thought it would be a good idea to exit early
        // and not make any modifications if one of the target paths is invalid
        validatePaths(targets);

        var buffer = new ByteArrayOutputStream();
        for (Path target : targets) {
            buffer.writeBytes(copyright);
            try {
                Files.newInputStream(target).transferTo(buffer);
                Files.write(target, buffer.toByteArray());
            } catch (IOException e) {
                throw new PrependException(String.format("Encountered and IO exception while processing %s. " +
                        "Make sure no other process is using the file.", target));
            }
            processedTargetCount++;
            buffer.reset();
        }
    }

    private static void resolveAll(List<Path> paths, Path root) {
        var iterator = paths.listIterator();
        while (iterator.hasNext()) {
            var target = iterator.next();
            iterator.set(root.toAbsolutePath().resolve(target));
        }
    }

    private static byte[] appendEmptyLines(byte[] buffer, int count, LineEnding lineEnding) throws PrependException {
        String ending = lineEnding.stringValue();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(buffer);
            out.write(ending.repeat(count).getBytes());
            return out.toByteArray();
        } catch (IOException e) {
            throw new PrependException("Runtime exception.");
        }
    }

    private static void validatePaths(List<Path> targets) throws PrependException {
        for (Path target : targets) {
            if (!Files.exists(target))
                throw new PrependException(String.format("%s path is invalid. File does not exist.", target));
        }
    }
}
