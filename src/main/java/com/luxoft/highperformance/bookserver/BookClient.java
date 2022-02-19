package com.luxoft.highperformance.bookserver;

import com.luxoft.highperformance.bookserver.model.Book;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Set;

@FeignClient(name = "books", url = "http://localhost:8080/books")
public interface BookClient {

    @RequestMapping("baseline/{s}")
    String baseline(@PathVariable String s);

    @RequestMapping("keywords3/{keywordsString}")
    Set<Book> getBookByTitleHashMap(@PathVariable String keywordsString);

    @RequestMapping(value = "keywords4/{keywordsString}")
    String getBookByTitleFastUtil(@PathVariable String keywordsString);

    @RequestMapping("keywords5/{keywordsString}")
    String getBookByTitleHashMapCached(@PathVariable String keywordsString);

    @RequestMapping("keywords6/{keywordsString}")
    String getBookByTitleHashMapCachedJSON(@PathVariable String keywordsString);

    @RequestMapping("keywords7/{keywordsString}")
    String getBookByTitlePreparedIndex(@PathVariable String keywordsString);

}