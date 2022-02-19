package com.luxoft.highperformance.bookserver;

import org.junit.runner.RunWith;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

/**
 *
 4 THREADS:
 Benchmark                                   Mode  Cnt    Score      Error  Units
 BookClientBenchmarks.baseline               avgt    3  450.291 ±  157.356  us/op
 BookClientBenchmarks.PreIndexed             avgt    3  466.955 ±  230.209  us/op
 BookClientBenchmarks.longHashCaching        avgt    3  475.820 ±   77.496  us/op
 BookClientBenchmarks.String2StringMapCache  avgt    3  480.568 ±  286.444  us/op
 BookClientBenchmarks.hashMapIndex           avgt    3  549.348 ± 1264.782  us/op
 BookClientBenchmarks.FastUtil               avgt    3  609.072 ± 3549.859  us/op

 100 THREADS:
 Benchmark                                   Mode  Cnt      Score      Error  Units
 BookClientBenchmarks.baseline               avgt    3   9545.781 ± 6305.537  us/op
 BookClientBenchmarks.PreIndexed             avgt    3   9241.909 ± 5614.781  us/op
 BookClientBenchmarks.String2StringMapCache  avgt    3  10105.118 ± 4313.847  us/op
 BookClientBenchmarks.longHashCaching        avgt    3  10517.882 ± 6444.846  us/op
 BookClientBenchmarks.FastUtil               avgt    3  10799.307 ± 5951.566  us/op
 BookClientBenchmarks.hashMapIndex           avgt    3  10897.052 ± 3552.746  us/op

 */
@SpringBootTest
@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@RunWith(SpringRunner.class)
public class BookClientBenchmarks extends AbstractBenchmark {

    String keywords1 = "Book83125";
    String keywords2 = "AuthorName76100 AuthorSurname88148";
    String keywords3 = "AuthorSurname88148 AuthorName76100 Book83125";

    public static BookController bookController;
    public static BookClient bookClient;

    @Autowired
    public void setBookController(BookController bookController) {
        BookClientBenchmarks.bookController = bookController;
        System.out.println("SET bookController="+bookController);
    }

    @Autowired
    public void setBookClient(BookClient bookClient) {
        BookClientBenchmarks.bookClient = bookClient;
    }

    @Setup(Level.Trial)
    public void setupBenchmark() {
        BookClientBenchmarks.bookController.readAll();
    }

    @Benchmark
    @OperationsPerInvocation(3)
    public void baseline(Blackhole bh) {
        bh.consume(bookClient.baseline("hello"));
        bh.consume(bookClient.baseline("hello"));
        bh.consume(bookClient.baseline("hello"));
    }

    @Benchmark
    @OperationsPerInvocation(3)
    public void hashMapIndex(Blackhole bh) {
        bh.consume(bookClient.getBookByTitleHashMap(keywords1));
        bh.consume(bookClient.getBookByTitleHashMap(keywords2));
        bh.consume(bookClient.getBookByTitleHashMap(keywords3));
    }

    @Benchmark
    @OperationsPerInvocation(3)
    public String FastUtil() {
        return bookClient.getBookByTitleFastUtil(keywords1)+
                bookClient.getBookByTitleFastUtil(keywords2)+
                bookClient.getBookByTitleFastUtil(keywords3);
    }

    @Benchmark
    @OperationsPerInvocation(3)
    public String longHashCaching() {
        return bookClient.getBookByTitleHashMapCached(keywords1)+
                bookClient.getBookByTitleHashMapCached(keywords2)+
                bookClient.getBookByTitleHashMapCached(keywords3);
    }

    @Benchmark
    @OperationsPerInvocation(3)
    public String String2StringMapCache() {
        return bookClient.getBookByTitleHashMapCachedJSON(keywords1)+
                bookClient.getBookByTitleHashMapCachedJSON(keywords2)+
                bookClient.getBookByTitleHashMapCachedJSON(keywords3);
    }

    @Benchmark
    @OperationsPerInvocation(3)
    public String PreIndexed() {
        return bookClient.getBookByTitlePreparedIndex(keywords1)+
                bookClient.getBookByTitlePreparedIndex(keywords2)+
                bookClient.getBookByTitlePreparedIndex(keywords3);
    }
}

