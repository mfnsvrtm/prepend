package com.github.mfnsvrtm;

import picocli.CommandLine;

class PrependExceptionHandler implements CommandLine.IExecutionExceptionHandler {
    @Override
    public int handleExecutionException(Exception ex, CommandLine cmd, CommandLine.ParseResult parseResult) {
        cmd.getErr().println(cmd.getColorScheme().errorText(ex.getMessage()));
        if (!(ex instanceof PrependException)) {
            ex.printStackTrace();
        }

        return cmd.getExitCodeExceptionMapper() != null
                ? cmd.getExitCodeExceptionMapper().getExitCode(ex)
                : cmd.getCommandSpec().exitCodeOnExecutionException();
    }
}
