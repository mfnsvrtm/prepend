package com.github.mfnsvrtm;

import picocli.CommandLine;

import java.nio.file.Files;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "prepend", description = "Prepend copyright notice to a list of files", sortOptions = false)
public class CliApp implements Callable<Integer> {
    @CommandLine.Mixin
    private CliArgs args;

    private int processedTargetCount = 0;

    public static void main(String[] args) {
        var app = new CliApp();

        CommandLine cl = new CommandLine(app)
                .setExecutionExceptionHandler(new PrependExceptionHandler())
                .setCaseInsensitiveEnumValuesAllowed(true);

        int exitCode = cl.execute(args);
        if (exitCode == CommandLine.ExitCode.SOFTWARE) {
            System.out.printf("%d files processed.%n", app.processedTargetCount);
        } else if (exitCode == CommandLine.ExitCode.OK && !cl.isUsageHelpRequested()) {
            System.out.printf("Success. %d files processed.%n", app.processedTargetCount);
        }

        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        validate();

        var prepender = new Prepender(args.makePojo());
        prepender.run();
        processedTargetCount = prepender.getProcessedTargetCount();

        return CommandLine.ExitCode.OK;
    }

    private void validate() {
        if (Files.notExists(args.targetListPath)) {
            throw new CommandLine.ParameterException(args.spec.commandLine(),
                    "Invalid <targets-path>. File doesn't exist. Make sure 'targets.txt' " +
                            "is present in CWD or provide a valid path with '-t' option.");
        }
        if (Files.notExists(args.copyrightNoticePath)) {
            throw new CommandLine.ParameterException(args.spec.commandLine(),
                    "Invalid <copyright-path>. File doesn't exist. Make sure 'copyright.txt' " +
                            "is present in CWD or provide a valid path with '-c' option.");
        }
        if (args.rootDirectoryPath != null && !Files.isDirectory(args.rootDirectoryPath)) {
            throw new CommandLine.ParameterException(args.spec.commandLine(),
                    "Invalid <root/base-dir>. Directory doesn't exist.");
        }
    }
}

