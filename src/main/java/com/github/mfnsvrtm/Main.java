package com.github.mfnsvrtm;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Main {
    private static int processedCount = 0;

    public static void main(String[] args) {
        try {
            Args parsedArgs = new Args();
            JCommander jc = JCommander.newBuilder().programName("prepend").addObject(parsedArgs).build();
            jc.parse(args);

            if (parsedArgs.help) {
                jc.usage();
            } else {
                run(parsedArgs);
                System.out.printf("Success. %d files processed.%n", processedCount);
            }
        } catch (PrependerException e) {
            System.out.printf("Error. %s%n", e.getMessage());
            System.out.printf("%d files processed.%n", processedCount);
        } catch (ParameterException e) {
            // I'm unhappy with error messages that I get out of this. They work for the developer, but not the user.
            // For instance: "Was passed main parameter '<name>' but no main parameter was defined in your arg class"
            System.out.println(e.getMessage());
        }
    }

    private static void run(Args args) throws PrependerException {
        List<Path> targets;
        byte[] copyright;

        try {
            targets = Files.lines(args.fileListPath).filter(Predicate.not(String::isBlank)).map(Path::of)
                    .collect(Collectors.toList());
            copyright = Files.readAllBytes(args.copyrightNoticePath);
        } catch (IOException e) {
            throw new PrependerException("Couldn't locate target list or copyright notice. " +
                    "Make sure targets \"targets.txt\" and \"copyright.txt\" are present in the current directory " +
                    "or pass one (or both) of -t <target list path> and -c <copyright path> flags.");
        } catch (InvalidPathException e) {
            throw new PrependerException("Target file contains invalid paths.");
        }

        if (args.extraLineCount != null) {
            copyright = addEmptyLines(copyright, args.extraLineCount, args.lineEnding);
        }

        if (args.rootDirectoryPath != null) {
            setRoot(targets, args.rootDirectoryPath);
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

    private static void checkPaths(List<Path> targets) throws PrependerException {
        for (Path target : targets) {
            if (!Files.exists(target))
                throw new PrependerException(String.format("%s path is invalid. File does not exist.", target));
        }
    }
}

