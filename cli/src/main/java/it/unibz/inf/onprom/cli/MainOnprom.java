package it.unibz.inf.onprom.cli;
import picocli.CommandLine;

import static picocli.CommandLine.*;

@Command(
        name = "onprom-cli",
        mixinStandardHelpOptions = true,
        subcommands = {
                AnnotationCommand.class,
                GenerateCommand.class,
                MergeCommand.class,
                UseCommand.class
        }
)
public class MainOnprom implements Runnable{
    @Override
    public void run() {
        System.out.println("Use a subcommand: generate | annotate | merge");
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new MainOnprom()).execute(args);
        System.exit(exitCode);
    }
}
