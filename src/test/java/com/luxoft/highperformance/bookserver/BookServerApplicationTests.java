package com.luxoft.highperformance.bookserver;

import com.luxoft.highperformance.bookserver.model.Book;
import com.luxoft.highperformance.bookserver.repositories.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@SpringBootTest
@Commit
class BookServerApplicationTests {

    public final int BOOKS_AMOUNT = 100_000;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    BookService bookService;

    @Test
    void contextLoads() {
    }

    @Test
    public void removeAllBooks() {
        bookRepository.deleteAllInBatch();
    }

    @Test
    @Transactional
    public void addBooks() {
        Random random = new Random();
        for (int i=0; i<BOOKS_AMOUNT; i++) {
            String title = "Book"+random.nextInt(BOOKS_AMOUNT);
            String authorName = "AuthorName"+random.nextInt(BOOKS_AMOUNT)+" ";
            String authorSurname = "AuthorSurname"+random.nextInt(BOOKS_AMOUNT);
            Book book = new Book();
            book.setTitle(title+" by "+authorName+authorSurname);
            Book book1 = bookRepository.save(book);
            bookService.initKeywords(book1);
            bookService.initKeywords2(book1);
            bookService.initKeywords3(book1);
        }
    }

    @Test
    public void showRandomBooks() {
        Random random = new Random();
        List<Book> all = bookRepository.findAll();
        System.out.println("Found "+all.size()+" books");
//        for (int i=0;i<10;i++) {
//            int index = random.nextInt(BOOKS_AMOUNT);
//            System.out.println(all.get(index));
//        }
    }

}
