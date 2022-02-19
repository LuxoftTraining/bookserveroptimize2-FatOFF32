package com.luxoft.highperformance.bookserver;

import com.luxoft.highperformance.bookserver.measure.Measure;
import com.luxoft.highperformance.bookserver.model.Book;
import com.luxoft.highperformance.bookserver.repositories.BookRepository;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("books")
public class BookController {

    public final int BOOKS_AMOUNT=10_000;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    BookService bookService;

    @GetMapping("baseline/{s}")
    public String baseline(@PathVariable String s) {
        return "baseline "+s;
    }

    //@Measure(value = "baseline", warmup = 50)
    @GetMapping("keywords0/{keywordsString}")
    public List<Book> getBookByTitle(@PathVariable String keywordsString) {
        String[] keywords = keywordsString.split(" ");
        if (keywords.length == 1) {
            return bookRepository.findAllByTitleContaining(keywords[0]);
        } else if (keywords.length == 2) {
            return bookRepository.findAllByTitleContainingAndTitleContaining(
                keywords[0], keywords[1]);
        } else if (keywords.length == 3) {
            return bookRepository.findAllByTitleContainingAndTitleContainingAndTitleContaining(
                keywords[0], keywords[1], keywords[2]);
        }
        return null;
    }

    //@Measure(value = "3 keywords indexed in DB", warmup = 50)
    @GetMapping("keywords1/{keywordsString}")
    @Transactional(readOnly = true)
    public List<Book> getBookByTitleDB(@PathVariable String keywordsString) {
        String[] keywords = keywordsString.split(" ");
        if (keywords.length == 1) {
            List<Book> list1 = bookRepository.findByKeyword1(keywords[0]);
            List<Book> list2 = bookRepository.findByKeyword2(keywords[0]);
            List<Book> list3 = bookRepository.findByKeyword3(keywords[0]);
            list1.addAll(list2);
            list1.addAll(list3);
            return list1;
        } else if (keywords.length == 2) {
            List<Book> list1 = bookRepository.findByKeyword1AndKeyword2(
                    keywords[0], keywords[1]);
            List<Book> list2 = bookRepository.findByKeyword1AndKeyword2(
                    keywords[1], keywords[0]);
            List<Book> list3 = bookRepository.findByKeyword2AndKeyword3(
                    keywords[0], keywords[1]);
            List<Book> list4 = bookRepository.findByKeyword2AndKeyword3(
                    keywords[1], keywords[0]);
            List<Book> list5 = bookRepository.findByKeyword1AndKeyword3(
                    keywords[0], keywords[1]);
            List<Book> list6 = bookRepository.findByKeyword1AndKeyword3(
                    keywords[1], keywords[0]);
            list1.addAll(list2);
            list1.addAll(list3);
            list1.addAll(list4);
            list1.addAll(list5);
            list1.addAll(list6);
            return list1;
        } else if (keywords.length == 3) {
            List<Book> list1 = bookRepository.findByKeyword1AndKeyword2AndKeyword3(
                    keywords[0], keywords[1], keywords[2]);
            List<Book> list2 = bookRepository.findByKeyword1AndKeyword2AndKeyword3(
                    keywords[0], keywords[2], keywords[1]);
            List<Book> list3 = bookRepository.findByKeyword1AndKeyword2AndKeyword3(
                    keywords[1], keywords[0], keywords[2]);
            List<Book> list4 = bookRepository.findByKeyword1AndKeyword2AndKeyword3(
                    keywords[1], keywords[2], keywords[0]);
            List<Book> list5 = bookRepository.findByKeyword1AndKeyword2AndKeyword3(
                    keywords[2], keywords[1], keywords[0]);
            List<Book> list6 = bookRepository.findByKeyword1AndKeyword2AndKeyword3(
                    keywords[2], keywords[0], keywords[1]);
            list1.addAll(list2);
            list1.addAll(list3);
            list1.addAll(list4);
            list1.addAll(list5);
            list1.addAll(list6);
            return list1;
        }
        return null;
    }

    @Autowired
    DataSource dataSource;

    //@Measure(value = "JDBC", warmup = 50)
    @GetMapping("keywords2/{keywordsString}")
    @Transactional
    public List<Book> getBookByTitleJDBC(@PathVariable String keywordsString) throws SQLException {
        StringBuilder builder = new StringBuilder();
        String[] keywords = keywordsString.split(" ");
        builder.append("'").append(keywords[0]).append("'");
        if (keywords.length>1) {
            builder.append(",'").append(keywords[1]).append("'");
        }
        if (keywords.length>2) {
            builder.append(",'").append(keywords[2]).append("'");
        }
        String keywordsIn = builder.toString();

        List<Book> books = new ArrayList<>();
        String SQL = "SELECT * FROM BOOK ";
        if (keywords.length == 1) {
            SQL += "WHERE KEYWORD1='"+keywords[0]+"' OR " +
                    "KEYWORD2='"+keywords[0]+"' OR " +
                    "KEYWORD3='"+keywords[0]+"'";
        } else if (keywords.length == 2) {
            SQL += "WHERE "+
                    "(KEYWORD1='"+keywords[0]+"' AND KEYWORD2='"+keywords[1]+"') OR " +
                    "(KEYWORD2='"+keywords[0]+"' AND KEYWORD1='"+keywords[1]+"') OR "+
                    "(KEYWORD1='"+keywords[0]+"' AND KEYWORD3='"+keywords[1]+"') OR " +
                    "(KEYWORD3='"+keywords[0]+"' AND KEYWORD1='"+keywords[1]+"') OR "+
                    "(KEYWORD2='"+keywords[0]+"' AND KEYWORD3='"+keywords[1]+"') OR " +
                    "(KEYWORD3='"+keywords[0]+"' AND KEYWORD2='"+keywords[1]+"')";
        } else if (keywords.length == 3) {
            SQL += "WHERE (KEYWORD1 IN (" + keywordsIn + ")) ";
            SQL += "AND (KEYWORD2 IN (" + keywordsIn + ")) ";
            SQL += "AND (KEYWORD3 IN (" + keywordsIn + ")) ";
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQL)) {
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) return books; // nothing found
            Book book = new Book();
            book.setId(resultSet.getInt("ID"));
            book.setTitle(resultSet.getString("TITLE"));
            book.setKeyword1(resultSet.getString("KEYWORD1"));
            book.setKeyword2(resultSet.getString("KEYWORD2"));
            book.setKeyword3(resultSet.getString("KEYWORD3"));
            books.add(book);
        }

        return books;
    }

    //@Measure("test")
    @GetMapping("test")
    public String getTest() {
        return "just a test!";
    }

    @GetMapping("/random")
    public Book getBookRandom() {
        Random random = new Random();
        int index = random.nextInt(BOOKS_AMOUNT);
        List<Book> all = bookRepository.findAll();
        return all.get(index);
    }

    @Measure(value = "3. HashMap", warmup = 500, baseline = true)
    @GetMapping("keywords3/{keywordsString}")
    public Set<Book> getBookByTitleHashMap(@PathVariable String keywordsString) {
        String[] keywords = keywordsString.split(" ");

        Set<Book> bookSet = null;
        for (String keyword : keywords) {
            Set<Book> booksWithKeywordSet = new HashSet<>();
            Map<String, Set<Book>> map = bookService.keywordMap;
            if (map.containsKey(keyword)) {
                booksWithKeywordSet.addAll(map.get(keyword));
            }
            if (bookSet == null) {
                bookSet = booksWithKeywordSet;
            } else {
                bookSet.retainAll(booksWithKeywordSet);
            }
        }

        return bookSet;
    }

    public static long getLongHashCode(String string) {
        long hash = 0;
        int len = string.length();

        for (int i = 0; i < len; i++) {
            hash = 31 * hash + string.charAt(i);
        }

        return hash;
    }

    Map<String, String> cacheKeywords = new ConcurrentHashMap<>();
    Long2ObjectOpenHashMap<String> cacheKeywords2 = new Long2ObjectOpenHashMap<>();
//    Long2ObjectOpenHashMap<String> cacheKeywords2 = new Long2ObjectOpenHashMap<>(1000);

    @Measure(value = "5. Cached with long hash", warmup = 500)
    @GetMapping("keywords5/{keywordsString}")
    public String getBookByTitleHashMapCached(@PathVariable String keywordsString) {
        long hash = getLongHashCode(keywordsString);
//        String res = cacheKeywords2.get(hash);
//        if (res != null) return res;
        if (cacheKeywords2.containsKey(hash))
            return cacheKeywords2.get(hash);

        String[] keywords = keywordsString.split(" ");

        Set<Book> bookSet = null;
        for (String keyword : keywords) {
            Set<Book> booksWithKeywordSet = new HashSet<>();
            Map<String, Set<Book>> map = bookService.keywordMap;
            if (map.containsKey(keyword)) {
                booksWithKeywordSet.addAll(map.get(keyword));
            }
            if (bookSet == null) {
                bookSet = booksWithKeywordSet;
            } else {
                bookSet.retainAll(booksWithKeywordSet);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (Book book: bookSet) {
            if (sb.length()>0) sb.append(", ");
            sb.append(bookService.bookId2JSON.get(book.getId().intValue()));
        }

        String jsonBooks = "["+sb.toString()+"]";

        cacheKeywords2.put(hash, jsonBooks);

        return jsonBooks;
    }

    @Measure(value = "6. Cached with String->String and JSON", warmup = 500)
    @GetMapping("keywords6/{keywordsString}")
    public String getBookByTitleHashMapCachedJSON(@PathVariable String keywordsString) {
        if (cacheKeywords.containsKey(keywordsString))
            return cacheKeywords.get(keywordsString);

        String[] keywords = keywordsString.split(" ");

        Set<Book> bookSet = null;
        for (String keyword : keywords) {
            Set<Book> booksWithKeywordSet = new HashSet<>();
            Map<String, Set<Book>> map = bookService.keywordMap;
            if (map.containsKey(keyword)) {
                booksWithKeywordSet.addAll(map.get(keyword));
            }
            if (bookSet == null) {
                bookSet = booksWithKeywordSet;
            } else {
                bookSet.retainAll(booksWithKeywordSet);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (Book book: bookSet) {
            if (sb.length()>0) sb.append(", ");
            sb.append(bookService.bookId2JSON.get(book.getId().intValue()));
        }

        String jsonBooks = "["+sb.toString()+"]";

        cacheKeywords.put(keywordsString, jsonBooks);

        return jsonBooks;
    }

    @Measure(value = "4. FastUtil", warmup = 500)
    @GetMapping(value = "keywords4/{keywordsString}",
            produces = APPLICATION_JSON_VALUE)
    public String getBookByTitleFastUtil(@PathVariable String keywordsString) {
        String[] keywords = keywordsString.split(" ");

        IntOpenHashSet bookIdsSet = null;
        IntOpenHashSet booksWithKeywordSet = new IntOpenHashSet();
        for (String keyword : keywords) {
            if (booksWithKeywordSet.size()>0) {
                booksWithKeywordSet.clear();
            }
            Int2ObjectOpenHashMap<IntOpenHashSet> map = bookService.keywordMap2;
            if (map.containsKey(keyword.hashCode())) {
                booksWithKeywordSet.addAll(map.get(keyword.hashCode()));
            }
            if (bookIdsSet == null) {
                bookIdsSet = booksWithKeywordSet;
            } else {
                bookIdsSet.retainAll(booksWithKeywordSet);
            }
        }

        if (bookIdsSet == null) return "[]";

        StringBuilder sb = new StringBuilder();
        for (int id: bookIdsSet) {
            if (sb.length()>0) sb.append(", ");
            sb.append(bookService.bookId2JSON.get(id));
        }

        return "["+sb.toString()+"]";
    }

    @Measure(value = "7. Fully pre-indexed", warmup = 500)
    @GetMapping(value = "keywords7/{keywordsString}",
            produces = APPLICATION_JSON_VALUE)
    public String getBookByTitlePreparedIndex(@PathVariable String keywordsString) {
        List<String> booksJSON = bookService.keywords2JSON.get(keywordsString);
        if (booksJSON == null) {
            System.out.println("REQUEST ["+keywordsString+"] is not indexed!");
            return null;
        } else if (booksJSON.size() == 1) {
            return "["+booksJSON.get(0)+"]";
        } else {
            StringBuilder sb = new StringBuilder();
            for (String s : booksJSON) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(s);
            }
            return "[" + sb.toString() + "]";
        }
    }

    @GetMapping
    public List<Book> getBooks() {
        return bookRepository.findAll();
    }

    @GetMapping("read-all")
    public void readAll() {
        List<Book> all = bookRepository.findAll();
        for (Book book: all) {
            bookService.initKeywords(book);
            bookService.initKeywords2(book);
            bookService.initKeywords3(book);
        }
    }

    @PostMapping
    public Book addBook(@RequestBody Book book) {
        Book book1 = bookRepository.save(book);
        bookService.initKeywords(book);
        bookService.initKeywords2(book);
        return book1;
    }

}
