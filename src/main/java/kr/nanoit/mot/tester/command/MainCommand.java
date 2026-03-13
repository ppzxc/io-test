package kr.nanoit.mot.tester.command;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Component
@Command(
        name = "io-tester",
        subcommands = {DbTestCommand.class, MqTestCommand.class, FileTestCommand.class},
        mixinStandardHelpOptions = true,
        description = "MariaDB & RabbitMQ I/O performance test tool"
)
public class MainCommand implements Runnable {

    @Spec
    private CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }
}
