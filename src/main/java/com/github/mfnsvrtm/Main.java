package com.github.mfnsvrtm;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "prepend", description = "Prepend copyright notice to a list of files", sortOptions = false)
public class Main implements Callable<Integer> {
    private static Prepender prepender;

    @CommandLine.Mixin
    private PrependArgs args;

    public static void main(String[] args) {
        CommandLine cl = new CommandLine(new Main())
                .setExecutionExceptionHandler(new PrependExceptionHandler())
                .setCaseInsensitiveEnumValuesAllowed(true);

        int exitCode = cl.execute(args);
        if (exitCode == CommandLine.ExitCode.SOFTWARE) {
            System.out.printf("%d files processed.%n", prepender.getProcessedTargetCount());
        } else if (exitCode == CommandLine.ExitCode.OK && !cl.isUsageHelpRequested()) {
            System.out.printf("Success. %d files processed.%n", prepender.getProcessedTargetCount());
        }

        System.exit(exitCode);
    }

    @Override
    public Integer call() throws PrependException {
        prepender = new Prepender(args);
        prepender.run();

        return CommandLine.ExitCode.OK;
    }
}

