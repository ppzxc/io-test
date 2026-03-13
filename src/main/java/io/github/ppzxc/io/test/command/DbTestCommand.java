package io.github.ppzxc.io.test.command;

import io.github.ppzxc.io.test.entity.CdrStatus;
import io.github.ppzxc.io.test.entity.Mo;
import io.github.ppzxc.io.test.entity.MoType;
import io.github.ppzxc.io.test.repository.MoRepository;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

@Component
@Command(
        name = "db",
        mixinStandardHelpOptions = true,
        description = "MariaDB I/O performance test"
)
public class DbTestCommand implements Runnable {

    private final MoRepository moRepository;

    public DbTestCommand(MoRepository moRepository) {
        this.moRepository = moRepository;
    }

    @Option(names = {"-c", "--count"}, description = "Number of records (default: ${DEFAULT-VALUE})", defaultValue = "1000")
    private int count;

    @Option(names = {"-t", "--threads"}, description = "Concurrent threads per operation (default: ${DEFAULT-VALUE})", defaultValue = "1")
    private int threads;

    @Option(names = {"-o", "--operation"}, description = "Operation: write, read, delete, all (default: ${DEFAULT-VALUE})", defaultValue = "all")
    private String operation;

    @Option(names = {"--subject-size"}, description = "Subject field size in bytes (default: ${DEFAULT-VALUE})", defaultValue = "46")
    private int subjectSize;

    @Option(names = {"--body-size"}, description = "Body field size in bytes (default: ${DEFAULT-VALUE})", defaultValue = "2000")
    private int bodySize;

    @Override
    public void run() {
        System.out.printf("========== MariaDB I/O Test (threads=%d) ===========%n", threads);
        System.out.printf("%-11s| %-7s | %-9s | %-7s | %-7s | %-7s | %s%n",
                "Operation", "Count", "Total(ms)", "Avg(ms)", "Min(ms)", "Max(ms)", "Throughput");
        System.out.println("---------------------------------------------------------------");

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            switch (operation.toLowerCase()) {
                case "write" -> {
                    OpResult r = execWrite(executor, count);
                    r.print();
                    cleanup(r.ids);
                }
                case "read" -> {
                    List<Long> ids = setupData(count);
                    execRead(executor, ids).print();
                    cleanup(ids);
                }
                case "delete" -> {
                    List<Long> ids = setupData(count);
                    execDelete(executor, ids).print();
                }
                case "all" -> runAll(executor);
                default -> System.err.println("Unknown operation: " + operation);
            }
        }

        System.out.println("===============================================================");
    }

    // write / read / delete 를 동시에 실행
    private void runAll(ExecutorService executor) {
        List<Long> readPool = setupData(count);
        List<Long> deletePool = setupData(count);

        // 세 operation을 동시에 submit
        CompletableFuture<OpResult> wf = CompletableFuture.supplyAsync(() -> execWrite(executor, count), executor);
        CompletableFuture<OpResult> rf = CompletableFuture.supplyAsync(() -> execRead(executor, readPool), executor);
        CompletableFuture<OpResult> df = CompletableFuture.supplyAsync(() -> execDelete(executor, deletePool), executor);

        CompletableFuture.allOf(wf, rf, df).join();

        OpResult write = wf.join();
        write.print();
        rf.join().print();
        df.join().print();

        cleanup(write.ids);
    }

    private OpResult execWrite(ExecutorService executor, int total) {
        int perThread = total / threads;
        int remainder = total % threads;
        ConcurrentLinkedQueue<Long> ids = new ConcurrentLinkedQueue<>();
        AtomicInteger written = new AtomicInteger(0);
        AtomicLong minMs = new AtomicLong(Long.MAX_VALUE);
        AtomicLong maxMs = new AtomicLong(0);

        long start = System.currentTimeMillis();
        IntStream.range(0, threads)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    int cnt = perThread + (i < remainder ? 1 : 0);
                    for (int j = 0; j < cnt; j++) {
                        long t0 = System.nanoTime();
                        ids.add(moRepository.saveAndFlush(buildTestMo()).getId());
                        long latMs = (System.nanoTime() - t0) / 1_000_000;
                        written.incrementAndGet();
                        minMs.updateAndGet(v -> Math.min(v, latMs));
                        maxMs.updateAndGet(v -> Math.max(v, latMs));
                    }
                }, executor))
                .toList()
                .forEach(CompletableFuture::join);
        long elapsed = System.currentTimeMillis() - start;

        return new OpResult("WRITE", written.get(), elapsed, minMs.get(), maxMs.get(), new ArrayList<>(ids));
    }

    private OpResult execRead(ExecutorService executor, List<Long> ids) {
        AtomicLong minMs = new AtomicLong(Long.MAX_VALUE);
        AtomicLong maxMs = new AtomicLong(0);
        long start = System.currentTimeMillis();
        partition(ids, threads).stream()
                .map(part -> CompletableFuture.runAsync(() -> {
                    for (Long id : part) {
                        long t0 = System.nanoTime();
                        moRepository.findById(id);
                        long latMs = (System.nanoTime() - t0) / 1_000_000;
                        minMs.updateAndGet(v -> Math.min(v, latMs));
                        maxMs.updateAndGet(v -> Math.max(v, latMs));
                    }
                }, executor))
                .toList()
                .forEach(CompletableFuture::join);
        long elapsed = System.currentTimeMillis() - start;
        return new OpResult("READ", ids.size(), elapsed, minMs.get(), maxMs.get(), List.of());
    }

    private OpResult execDelete(ExecutorService executor, List<Long> ids) {
        AtomicLong minMs = new AtomicLong(Long.MAX_VALUE);
        AtomicLong maxMs = new AtomicLong(0);
        long start = System.currentTimeMillis();
        partition(ids, threads).stream()
                .map(part -> CompletableFuture.runAsync(() -> {
                    for (Long id : part) {
                        long t0 = System.nanoTime();
                        moRepository.deleteById(id);
                        long latMs = (System.nanoTime() - t0) / 1_000_000;
                        minMs.updateAndGet(v -> Math.min(v, latMs));
                        maxMs.updateAndGet(v -> Math.max(v, latMs));
                    }
                }, executor))
                .toList()
                .forEach(CompletableFuture::join);
        long elapsed = System.currentTimeMillis() - start;
        return new OpResult("DELETE", ids.size(), elapsed, minMs.get(), maxMs.get(), List.of());
    }

    private List<Long> setupData(int n) {
        List<Long> ids = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            ids.add(moRepository.saveAndFlush(buildTestMo()).getId());
        }
        return ids;
    }

    private void cleanup(List<Long> ids) {
        ids.forEach(moRepository::deleteById);
    }

    private <T> List<List<T>> partition(List<T> list, int n) {
        int size = list.size();
        int partSize = Math.max(1, (size + n - 1) / n);
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < size; i += partSize) {
            result.add(list.subList(i, Math.min(i + partSize, size)));
        }
        return result;
    }

    private Mo buildTestMo() {
        return Mo.builder()
                .msgId("test-" + UUID.randomUUID())
                .type(MoType.SMS)
                .autoreply(false)
                .from("01012345678")
                .fromTelco("SKT")
                .to("01087654321")
                .toTelco("KT")
                .subject(buildString(subjectSize))
                .body(buildString(bodySize))
                .attachCount(0)
                .forwardCount(0)
                .cdr(CdrStatus.READY)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private String buildString(int bytes) {
        return "x".repeat(bytes);
    }

    private record OpResult(String op, int count, long totalMs, long minMs, long maxMs, List<Long> ids) {
        void print() {
            double avg = (double) totalMs / Math.max(count, 1);
            double throughput = (count * 1000.0) / Math.max(totalMs, 1);
            System.out.printf("%-11s| %7d | %9d | %7.2f | %7d | %7d | %10.2f/s%n",
                    op, count, totalMs, avg, minMs, maxMs, throughput);
        }
    }
}
