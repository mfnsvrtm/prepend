package com.github.mfnsvrtm;

import com.github.rvesse.airline.SingleCommand;
import com.github.rvesse.airline.help.Help;
import com.github.rvesse.airline.help.cli.CliCommandUsageGenerator;
import com.github.rvesse.airline.parser.errors.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Main {
    private static int processedCount = 0;

    public static void main(String[] args) {
        try {
            SingleCommand<PrependCommand> parser = SingleCommand.singleCommand(PrependCommand.class);
            PrependCommand cmd = parser.parse(args);

            if (!cmd.help.showHelpIfRequested()) {
                run(cmd);
                System.out.printf("Success. %d files processed.%n", processedCount);
            }
        } catch (PrependerException e) {
            System.out.printf("Error. %s%n", e.getMessage());
            System.out.printf("%d files processed.%n", processedCount);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void run(PrependCommand cmd) throws PrependerException {
        List<Path> targets;
        byte[] copyright;

        try {
            targets = Files.lines(cmd.fileListPath).filter(Predicate.not(String::isBlank)).map(Path::of)
                    .collect(Collectors.toList());
            copyright = Files.readAllBytes(cmd.copyrightNoticePath);
        } catch (IOException e) {
            throw new PrependerException("Couldn't locate target list or copyright notice. " +
                    "Make sure targets \"targets.txt\" and \"copyright.txt\" are present in the current directory " +
                    "or pass one (or both) of -t <target list path> and -c <copyright path> flags.");
        } catch (InvalidPathException e) {
            throw new PrependerException("Target file contains invalid paths.");
        }

        if (cmd.extraLineCount != null) {
            copyright = addEmptyLines(copyright, cmd.extraLineCount, cmd.lineEnding);
        }

        if (cmd.rootDirectoryPath != null) {
            setRoot(targets, cmd.rootDirectoryPath);
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

