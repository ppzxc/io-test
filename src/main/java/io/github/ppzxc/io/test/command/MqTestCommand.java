package io.github.ppzxc.io.test.command;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Component
@Command(
        name = "mq",
        mixinStandardHelpOptions = true,
        description = "RabbitMQ I/O performance test"
)
public class MqTestCommand implements Runnable {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitAdmin rabbitAdmin;

    public MqTestCommand(RabbitTemplate rabbitTemplate, RabbitAdmin rabbitAdmin) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitAdmin = rabbitAdmin;
    }

    @Option(names = {"-c", "--count"}, description = "Number of messages (default: ${DEFAULT-VALUE})", defaultValue = "1000")
    private int count;

    @Option(names = {"-s", "--size"}, description = "Message size in bytes (default: ${DEFAULT-VALUE})", defaultValue = "1024")
    private int size;

    @Option(names = {"-q", "--queue"}, description = "Queue name (default: ${DEFAULT-VALUE})", defaultValue = "mot-test-queue")
    private String queue;

    @Option(names = {"-t", "--threads"}, description = "Concurrent threads (default: ${DEFAULT-VALUE})", defaultValue = "1")
    private int threads;

    @Option(names = {"-o", "--operation"}, description = "Operation: publish, consume, all (default: ${DEFAULT-VALUE})", defaultValue = "all")
    private String operation;

    @Override
    public void run() {
        rabbitAdmin.declareQueue(new Queue(queue, false, false, false));

        byte[] payload = buildPayload(size);

        System.out.printf("========== RabbitMQ I/O Test (threads=%d) ==========%n", threads);
        System.out.printf("%-11s| %-7s | %-9s | %-7s | %s%n",
                "Operation", "Count", "Total(ms)", "Avg(ms)", "Throughput");
        System.out.println("----------------------------------------------------");

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            switch (operation.toLowerCase()) {
                case "publish" -> {
                    OpResult r = execPublish(executor, payload, count);
                    r.print();
                    purgeQueue();
                }
                case "consume" -> {
                    setupPublish(payload, count);
                    execConsume(executor, count).print();
                }
                case "all" -> runAll(executor, payload);
                default -> System.err.println("Unknown operation: " + operation);
            }
        }

        System.out.println("====================================================");
    }

    private void runAll(ExecutorService executor, byte[] payload) {
        // setup: pre-publish count messages for consume side
        setupPublish(payload, count);

        // simultaneously: publish count messages + consume pre-published count messages
        CompletableFuture<OpResult> pf = CompletableFuture.supplyAsync(() -> execPublish(executor, payload, count), executor);
        CompletableFuture<OpResult> cf = CompletableFuture.supplyAsync(() -> execConsume(executor, count), executor);

        CompletableFuture.allOf(pf, cf).join();

        pf.join().print();
        cf.join().print();

        // cleanup: purge remaining publish messages
        purgeQueue();
    }

    private OpResult execPublish(ExecutorService executor, byte[] payload, int total) {
        int perThread = Math.max(1, total / threads);
        long start = System.currentTimeMillis();
        IntStream.range(0, threads)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    for (int j = 0; j < perThread; j++) {
                        rabbitTemplate.convertAndSend(queue, payload);
                    }
                }, executor))
                .toList()
                .forEach(CompletableFuture::join);
        long elapsed = System.currentTimeMillis() - start;
        return new OpResult("PUBLISH", perThread * threads, elapsed);
    }

    private OpResult execConsume(ExecutorService executor, int total) {
        AtomicInteger received = new AtomicInteger(0);
        long start = System.currentTimeMillis();
        IntStream.range(0, threads)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    while (received.get() < total) {
                        Message msg = rabbitTemplate.receive(queue, 3000);
                        if (msg == null) break;
                        received.incrementAndGet();
                    }
                }, executor))
                .toList()
                .forEach(CompletableFuture::join);
        long elapsed = System.currentTimeMillis() - start;
        int got = received.get();
        if (got < total) {
            System.err.println("Timeout waiting for messages. Received: " + got + "/" + total);
        }
        return new OpResult("CONSUME", got, elapsed);
    }

    private void setupPublish(byte[] payload, int n) {
        for (int i = 0; i < n; i++) {
            rabbitTemplate.convertAndSend(queue, payload);
        }
    }

    private void purgeQueue() {
        rabbitAdmin.purgeQueue(queue, false);
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
}
