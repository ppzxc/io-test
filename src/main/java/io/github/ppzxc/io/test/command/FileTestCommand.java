package io.github.ppzxc.io.test.command;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Component
@Command(
        name = "file",
        mixinStandardHelpOptions = true,
        description = "File I/O performance test"
)
public class FileTestCommand implements Runnable {

    @Option(names = {"-n", "--files"}, description = "Number of files (default: ${DEFAULT-VALUE})", defaultValue = "1000")
    private int files;

    @Option(names = {"-s", "--size"}, description = "File size in bytes (default: ${DEFAULT-VALUE})", defaultValue = "4096")
    private int size;

    @Option(names = {"-t", "--threads"}, description = "Concurrent threads (default: ${DEFAULT-VALUE})", defaultValue = "1")
    private int threads;

    @Option(names = {"-o", "--operation"}, description = "Operation: write, read, all (default: ${DEFAULT-VALUE})", defaultValue = "all")
    private String operation;

    @Option(names = {"-d", "--dir"}, description = "Test directory (default: ${DEFAULT-VALUE})", defaultValue = "/tmp/mot-file-test")
    private String dir;

    private Path baseDir;

    @Override
    public void run() {
        baseDir = Path.of(dir);
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            System.err.println("Failed to create test directory: " + e.getMessage());
            return;
        }

        byte[] payload = buildPayload(size);

        System.out.printf("========== File I/O Test (threads=%d, size=%dB) ==========%n", threads, size);
        System.out.printf("%-11s| %-7s | %-9s | %-7s | %s%n",
                "Operation", "Count", "Total(ms)", "Avg(ms)", "Throughput");
        System.out.println("------------------------------------------------------");

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            switch (operation.toLowerCase()) {
                case "write" -> {
                    WriteResult wr = execWrite(executor, payload);
                    wr.result().print();
                    cleanup(wr.paths());
                }
                case "read" -> {
                    List<Path> paths = setupWrite(payload);
                    execRead(executor, paths).print();
                    cleanup(paths);
                }
                case "all" -> runAll(executor, payload);
                default -> System.err.println("Unknown operation: " + operation);
            }
        }

        System.out.println("======================================================");
    }

    private void runAll(ExecutorService executor, byte[] payload) {
        CompletableFuture<WriteResult> wf = CompletableFuture.supplyAsync(() -> execWrite(executor, payload), executor);
        WriteResult wr = wf.join();
        wr.result().print();

        execRead(executor, wr.paths()).print();
        cleanup(wr.paths());
    }

    private WriteResult execWrite(ExecutorService executor, byte[] payload) {
        int perThread = Math.max(1, files / threads);
        java.util.concurrent.ConcurrentLinkedQueue<Path> written = new java.util.concurrent.ConcurrentLinkedQueue<>();

        long start = System.currentTimeMillis();
        IntStream.range(0, threads)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    for (int j = 0; j < perThread; j++) {
                        Path p = baseDir.resolve(UUID.randomUUID() + ".bin");
                        try {
                            Files.write(p, payload);
                            written.add(p);
                        } catch (IOException e) {
                            System.err.println("Write failed: " + e.getMessage());
                        }
                    }
                }, executor))
                .toList()
                .forEach(CompletableFuture::join);
        long elapsed = System.currentTimeMillis() - start;

        List<Path> paths = List.copyOf(written);
        return new WriteResult(new OpResult("WRITE", paths.size(), elapsed), paths);
    }

    private OpResult execRead(ExecutorService executor, List<Path> paths) {
        int partSize = Math.max(1, (paths.size() + threads - 1) / threads);
        long start = System.currentTimeMillis();

        List<List<Path>> parts = partition(paths, partSize);
        parts.stream()
                .map(part -> CompletableFuture.runAsync(() -> {
                    for (Path p : part) {
                        try {
                            Files.readAllBytes(p);
                        } catch (IOException e) {
                            System.err.println("Read failed: " + e.getMessage());
                        }
                    }
                }, executor))
                .toList()
                .forEach(CompletableFuture::join);

        long elapsed = System.currentTimeMillis() - start;
        return new OpResult("READ", paths.size(), elapsed);
    }

    private List<Path> setupWrite(byte[] payload) {
        java.util.concurrent.ConcurrentLinkedQueue<Path> written = new java.util.concurrent.ConcurrentLinkedQueue<>();
        for (int i = 0; i < files; i++) {
            Path p = baseDir.resolve(UUID.randomUUID() + ".bin");
            try {
                Files.write(p, payload);
                written.add(p);
            } catch (IOException e) {
                System.err.println("Setup write failed: " + e.getMessage());
            }
        }
        return List.copyOf(written);
    }

    private void cleanup(List<Path> paths) {
        for (Path p : paths) {
            try {
                Files.deleteIfExists(p);
            } catch (IOException ignored) {
            }
        }
    }

    private <T> List<List<T>> partition(List<T> list, int partSize) {
        java.util.List<java.util.List<T>> result = new java.util.ArrayList<>();
        for (int i = 0; i < list.size(); i += partSize) {
            result.add(list.subList(i, Math.min(i + partSize, list.size())));
        }
        return result;
    }

    private byte[] buildPayload(int bytes) {
        byte[] buf = new byte[bytes];
        Arrays.fill(buf, (byte) 'x');
        return buf;
    }

    private record OpResult(String op, int count, long totalMs) {
        void print() {
            double avg = (double) totalMs / Math.max(count, 1);
            double throughput = (count * 1000.0) / Math.max(totalMs, 1);
            System.out.printf("%-11s| %7d | %9d | %7.2f | %10.2f/s%n",
                    op, count, totalMs, avg, throughput);
        }
    }

    private record WriteResult(OpResult result, List<Path> paths) {}
}
