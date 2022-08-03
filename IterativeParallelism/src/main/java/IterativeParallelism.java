import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class IterativeParallelism {
    public <T> T minimum(int threads, List<T> list, Comparator<? super T> comparator) {
        return completeJob(threads, list, comparator, e -> e.min(comparator).orElse(null));
    }
    public <T> T maximum(int threads, List<T> list, Comparator<? super T> comparator) {
        return completeJob(threads, list, comparator, e -> e.max(comparator).orElse(null));
    }
    private  <T> T completeJob(int threads, List<T> list, Comparator<? super T> comparator,
                         Function<Stream<T>, T> operation) {
        List<Stream<T>> parts = separate(threads, list);
        return operation.apply(concurrentJob(parts, comparator, operation).stream());
    }


    private <T> List<T> concurrentJob(List<Stream<T>> parts, Comparator<? super T> comparator,
                                      Function<Stream<T>, T> operation) {
        List<T> results = new ArrayList<>(Collections.nCopies(parts.size(), null));
        List<Thread> partResults = IntStream.
                range(0, parts.size()).
                mapToObj(i -> {
                    Thread thread = new Thread(() -> {
                        results.set(i, operation.apply(parts.get(i)));
                    });
                    thread.start();
                    return thread;
                }).collect(Collectors.toList());
        for(Thread thread: partResults) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return results;
    }

    private <T> List<Stream<T>> separate(int threads, List<T> list) {
        List<Stream<T>> streams = new ArrayList<>();
        int partSize = list.size() / threads;
        int last = 0;
        for(int i = 0;i < threads;i++) {
            streams.add(list.subList(last, Math.min(last + partSize, list.size())).stream());
            last += partSize;
        }
        return streams;
    }
}