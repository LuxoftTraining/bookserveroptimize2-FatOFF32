package com.luxoft.highperformance.bookserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luxoft.highperformance.bookserver.model.Book;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BookService {

    @Autowired
    ObjectMapper mapper;

    public final int KEYWORDS_AMOUNT = 3;
    public Map<String, Set<Book>> keywordMap = new ConcurrentHashMap<>();
    public Int2ObjectOpenHashMap<IntOpenHashSet> keywordMap2 =
            new Int2ObjectOpenHashMap<>();
    public Int2ObjectOpenHashMap<Book> bookId2Book =
            new Int2ObjectOpenHashMap<>();
    public Int2ObjectOpenHashMap<String> bookId2JSON =
            new Int2ObjectOpenHashMap<>();

    public Map<String, List<String>> keywords2JSON = new ConcurrentHashMap<>();

    public void initKeywords(Book book) {
        String[] keywords = book.getTitle().split(" ");
        if (keywords.length > 0) book.setKeyword1(keywords[0]);
        if (keywords.length > 1) book.setKeyword2(keywords[2]);
        if (keywords.length > 2) book.setKeyword3(keywords[3]);
        addToHashMaps(book, List.of(keywords[0],keywords[2],keywords[3]));
    }

    public void initKeywords2(Book book)  {
        bookId2Book.put(book.getId().intValue(), book);
        try {
            bookId2JSON.put(book.getId().intValue(),
                    mapper.writeValueAsString(book));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        String[] keywords = book.getTitle().split(" ");
        if (keywords.length > 0) book.setKeyword1(keywords[0]);
        if (keywords.length > 1) book.setKeyword2(keywords[2]);
        if (keywords.length > 2) book.setKeyword3(keywords[3]);
        addToHashMaps2(book, List.of(keywords[0],keywords[2],keywords[3]));
    }

    public void initKeywords3(Book book)  {
        String json = "";
        try {
            json = mapper.writeValueAsString(book);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        String[] keywords = book.getTitle().split(" ");
        String keyword1 = "";
        String keyword2 = "";
        String keyword3 = "";
        if (keywords.length > 0) keyword1 = keywords[0];
        if (keywords.length > 1) keyword2 = keywords[2];
        if (keywords.length > 2) keyword3 = keywords[3];

        String[] variations = {
                keyword1, keyword2, keyword3,
                keyword1+" "+keyword2,
                keyword2+" "+keyword1,
                keyword2+" "+keyword3,
                keyword3+" "+keyword2,
                keyword1+" "+keyword3,
                keyword3+" "+keyword1,
                keyword1+" "+keyword2+" "+keyword3,
                keyword1+" "+keyword3+" "+keyword2,
                keyword2+" "+keyword1+" "+keyword3,
                keyword2+" "+keyword3+" "+keyword1,
                keyword3+" "+keyword1+" "+keyword2,
                keyword3+" "+keyword2+" "+keyword1
        };
        for (String variant: variations) {
            addToKeywordsIndex(variant, json);
        }
    }

    private void addToKeywordsIndex(String keywords, String bookJSON) {
        if (keywords2JSON.containsKey(keywords)) {
            keywords2JSON.get(keywords).add(bookJSON);
        } else {
            List<String> list = new ArrayList<>();
            list.add(bookJSON);
            keywords2JSON.put(keywords, list);
        }
    }

    private void addToHashMaps(Book book, List<String> keywords) {
        for (int i=0; i< KEYWORDS_AMOUNT; i++) {
            String keyword = keywords.get(i);
            if (keywordMap.containsKey(keyword)) {
                keywordMap.get(keyword).add(book);
            } else {
                HashSet<Book> set = new HashSet<>();
                set.add(book);
                keywordMap.put(keyword, set);
            }
        }
    }

    private void addToHashMaps2(Book book, List<String> keywords) {
        for (int i=0; i< KEYWORDS_AMOUNT; i++) {
            String keyword = keywords.get(i);
            if (keywordMap2.containsKey(keyword.hashCode())) {
                keywordMap2.get(keyword.hashCode()).add(book.getId().intValue());
            } else {
                IntOpenHashSet set = new IntOpenHashSet();
                set.add(book.getId().intValue());
                keywordMap2.put(keyword.hashCode(), set);
            }
        }
    }

}
