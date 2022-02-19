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
 Benchmark                                       Mode  Cnt  Score   Error  Units
 BookControllerBenchmarks.FastUtil               avgt    3  2.445 ± 0.542  us/op
 BookControllerBenchmarks.PreIndexed             avgt    3  2.213 ± 2.066  us/op
 BookControllerBenchmarks.String2StringMapCache  avgt    3  1.728 ± 1.549  us/op
 BookControllerBenchmarks.baseline               avgt    3  0.180 ± 0.118  us/op
 BookControllerBenchmarks.hashMapIndex           avgt    3  2.498 ± 2.808  us/op
 BookControllerBenchmarks.longHashCaching        avgt    3  1.786 ± 1.333  us/op
 */
@SpringBootTest
@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@RunWith(SpringRunner.class)
public class BookControllerBenchmarks extends AbstractBenchmark {

    String keywords1 = "Book83125";
    String keywords2 = "AuthorName76100 AuthorSurname88148";
    String keywords3 = "AuthorSurname88148 AuthorName76100 Book83125";

    public static BookController bookController;

    @Setup(Level.Trial)
    public void setupBenchmark() {
        bookController.readAll();
    }
    @Autowired
    public void setBookController(BookController bookController) {
        BookControllerBenchmarks.bookController = bookController;
    }

    @Benchmark
    @OperationsPerInvocation(3)
    public void baseline(Blackhole bh) {
        bh.consume(bookController.baseline(keywords1));
        bh.consume(bookController.baseline(keywords2));
        bh.consume(bookController.baseline(keywords3));
    }
/*
    @Benchmark
    @OperationsPerInvocation(3)
    public void hashMapIndex(Blackhole bh) {
        bh.consume(bookController.getBookByTitleHashMap(keywords1));
        bh.consume(bookController.getBookByTitleHashMap(keywords2));
        bh.consume(bookController.getBookByTitleHashMap(keywords3));
    }

    @Benchmark
    @OperationsPerInvocation(3)
    public String longHashCaching() {
        return bookController.getBookByTitleHashMapCached(keywords1)+
                bookController.getBookByTitleHashMapCached(keywords2)+
                bookController.getBookByTitleHashMapCached(keywords3);
    }

    @Benchmark
    @OperationsPerInvocation(3)
    public String String2StringMapCache() {
        return bookController.getBookByTitleHashMapCachedJSON(keywords1)+
                bookController.getBookByTitleHashMapCachedJSON(keywords2)+
                bookController.getBookByTitleHashMapCachedJSON(keywords3);
    }

    @Benchmark
    @OperationsPerInvocation(3)
    public String FastUtil() {
        return bookController.getBookByTitleFastUtil(keywords1)+
                bookController.getBookByTitleFastUtil(keywords2)+
                bookController.getBookByTitleFastUtil(keywords3);
    }

    @Benchmark
    @OperationsPerInvocation(3)
    public String PreIndexed() {
        return bookController.getBookByTitlePreparedIndex(keywords1)+
                bookController.getBookByTitlePreparedIndex(keywords2)+
                bookController.getBookByTitlePreparedIndex(keywords3);
    }*/
}
