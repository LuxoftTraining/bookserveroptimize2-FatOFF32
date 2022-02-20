package com.luxoft.highperformance.bookserver;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

class MyTests {

    public int curIndex = 0;
    public int step = 10;
    byte[] myHeap = new byte[10_000];

    @Test
    void myTest1(){
        String[] arrayStr = new String[4];
        arrayStr[0] = "KeyWord1";
        arrayStr[1] = "KeyWord3";
        arrayStr[2] = "Word4";
        arrayStr[3] = "Word5";

        for (String str : arrayStr) {
            addWordInMyHeap(str);
        }

        for (int i = 0; i < arrayStr.length; i++){
            assert getWordByIndex(i).trim().equals(arrayStr[i]);
        }

    }

    @Test
    void increment() {
        AtomicInteger integer = new AtomicInteger(-1);

        assert integer.incrementAndGet() == 0;
        assert integer.incrementAndGet() == 1;
    }

    private void addWordInMyHeap(String word) {
        byte[] curWord = word.getBytes(StandardCharsets.UTF_8);
        if (curWord.length > step) {
            throw new IllegalStateException("Word should be less than 10");
        }
        System.arraycopy(curWord, 0, myHeap, curIndex, curIndex + curWord.length - curIndex);

        curIndex += step;
    }

    private String getWordByIndex(int idx) {
        return new String(Arrays.copyOfRange(myHeap, idx*step, idx*step + step));
    }
}
