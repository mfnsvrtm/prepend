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

    void run() {
        List<Path> targetList;
        byte[] copyrightNotice;

        try {
            targetList = Files.lines(args.targetListPath).filter(Predicate.not(String::isBlank)).map(Path::of)
                    .collect(Collectors.toList());
            copyrightNotice = Files.readAllBytes(args.copyrightNoticePath);
        } catch (IOException e) {
            throw new PrependException("Couldn't read the target list file.");
        } catch (InvalidPathException e) {
            throw new PrependException("Target list file contains invalid paths.");
        }

        copyrightNotice = appendEmptyLines(copyrightNotice, args.extraLineCount, args.lineEnding);

        if (args.rootDirectoryPath != null) {
            resolveAll(targetList, args.rootDirectoryPath);
        }

        // I don't know if this is of any value, but I thought it would be a good idea to exit early
        // and not make any modifications if one of the target paths is invalid
        assertFilesExist(targetList);

        var buffer = new ByteArrayOutputStream();
        for (Path target : targetList) {
            buffer.writeBytes(copyrightNotice);
            try {
                Files.newInputStream(target).transferTo(buffer);
                Files.write(target, buffer.toByteArray());
            } catch (IOException e) {
                throw new PrependException(String.format("Couldn't overwrite target `%s`. " +
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

    private static byte[] appendEmptyLines(byte[] buffer, int count, LineEnding lineEnding) {
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

    private static void assertFilesExist(List<Path> targets) {
        for (Path target : targets) {
            if (!Files.exists(target))
                throw new PrependException(String.format("'%s' target path is invalid. File does not exist.", target));
        }
    }
}
