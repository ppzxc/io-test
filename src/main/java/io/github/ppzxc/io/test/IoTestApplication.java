package io.github.ppzxc.io.test;

import io.github.ppzxc.io.test.command.MainCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@SpringBootApplication
public class IoTestApplication implements CommandLineRunner, ExitCodeGenerator {

    @Autowired
    private MainCommand mainCommand;

    @Autowired
    private ApplicationContext applicationContext;

    private int exitCode;

    @Override
    public void run(String... args) {
        IFactory factory = new IFactory() {
            @Override
            public <K> K create(Class<K> cls) throws Exception {
                try {
                    return applicationContext.getBean(cls);
                } catch (Exception e) {
                    return CommandLine.defaultFactory().create(cls);
                }
            }
        };
        exitCode = new CommandLine(mainCommand, factory).execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(IoTestApplication.class, args)));
    }
}
