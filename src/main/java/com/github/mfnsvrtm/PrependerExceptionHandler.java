package com.github.mfnsvrtm;

import picocli.CommandLine;

class PrependerExceptionHandler implements CommandLine.IExecutionExceptionHandler {

    @Override
    public int handleExecutionException(Exception ex, CommandLine cmd, CommandLine.ParseResult parseResult) {
        cmd.getErr().println(cmd.getColorScheme().errorText(ex.getMessage()));
        if (!(ex instanceof PrependerException)) {
            ex.printStackTrace();
        }

        return cmd.getExitCodeExceptionMapper() != null
                ? cmd.getExitCodeExceptionMapper().getExitCode(ex)
                : cmd.getCommandSpec().exitCodeOnExecutionException();
    }
}
