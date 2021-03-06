/**
 * Copyright 2010-2013 Atilika Inc. and contributors (see CONTRIBUTORS.md)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.  A copy of the
 * License is distributed with this work in the LICENSE.md file.  You may
 * also obtain a copy of the License from
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atilika.kuromoji.ipadic;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TokenizerTest {

    private static Tokenizer tokenizer;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        tokenizer = new Tokenizer.Builder().build();
    }

    @Test
    public void testSimpleSegmentation() {
        String input = "スペースステーションに行きます。うたがわしい。";
        String[] surfaceForms = {"スペース", "ステーション", "に", "行き", "ます", "。", "うたがわしい", "。"};
        List<Token> tokens = tokenizer.tokenize(input);
        assertTrue(tokens.size() == surfaceForms.length);
        for (int i = 0; i < tokens.size(); i++) {
            assertEquals(surfaceForms[i], tokens.get(i).getSurfaceForm());
        }
    }

    @Test
    public void testSimpleReadings() {
        List<Token> tokens = tokenizer.tokenize("寿司が食べたいです。");
        assertTrue(tokens.size() == 6);
        assertEquals(tokens.get(0).getReading(), "スシ");
        assertEquals(tokens.get(1).getReading(), "ガ");
        assertEquals(tokens.get(2).getReading(), "タベ");
        assertEquals(tokens.get(3).getReading(), "タイ");
        assertEquals(tokens.get(4).getReading(), "デス");
        assertEquals(tokens.get(5).getReading(), "。");
    }

    @Test
    public void testSimpleReading() {
        List<Token> tokens = tokenizer.tokenize("郵税");
        assertEquals(tokens.get(0).getReading(), "ユウゼイ");
    }

    @Test
    public void testSimpleBaseFormKnownWord() {
        List<Token> tokens = tokenizer.tokenize("お寿司が食べたい。");
        assertTrue(tokens.size() == 6);
        assertEquals("食べ", tokens.get(3).getSurfaceForm());
        assertEquals("食べる", tokens.get(3).getBaseForm());

    }

    @Test
    public void testSimpleBaseFormUnknownWord() {
        List<Token> tokens = tokenizer.tokenize("アティリカ株式会社");
        assertTrue(tokens.size() == 2);
        assertTrue(tokens.get(0).isUnknown());
        assertNull(tokens.get(0).getBaseForm());
        assertTrue(tokens.get(1).isKnown());
        assertEquals("株式会社", tokens.get(1).getBaseForm());
    }

    @Test
    public void testYabottaiCornerCase() {
        List<Token> tokens = tokenizer.tokenize("やぼったい");
        assertEquals(1, tokens.size());
        assertEquals("やぼったい", tokens.get(0).getSurfaceForm());
    }

    @Test
    public void testTsukitoshaCornerCase() {
        List<Token> tokens = tokenizer.tokenize("突き通しゃ");
        assertEquals(1, tokens.size());
        assertEquals("突き通しゃ", tokens.get(0).getSurfaceForm());
    }

    @Test
    public void testIpadicTokenAPIs() throws Exception {
        List<Token> tokens = tokenizer.tokenize("お寿司が食べたい！");
        String[] pronunciations = {"オ", "スシ", "ガ", "タベ", "タイ", "！"};

        assertEquals(pronunciations.length, tokens.size());

        for (int i = 0; i < tokens.size(); i++) {
            assertEquals(pronunciations[i], tokens.get(i).getPronunciation());
        }

        String[] conjugationForms = {"*", "*", "*", "連用形", "基本形", "*"};

        for (int i = 0; i < tokens.size(); i++) {
            assertEquals(conjugationForms[i], tokens.get(i).getConjugationForm());
        }

        String[] conjugationTypes = {"*", "*", "*", "一段", "特殊・タイ", "*"};

        for (int i = 0; i < tokens.size(); i++) {
            assertEquals(conjugationTypes[i], tokens.get(i).getConjugationType());
        }

        String[] posLevel1 = {"接頭詞", "名詞", "助詞", "動詞", "助動詞", "記号"};

        for (int i = 0; i < tokens.size(); i++) {
            assertEquals(posLevel1[i], tokens.get(i).getPosLevel1());
        }

        String[] posLevel2 = {"名詞接続", "一般", "格助詞", "自立", "*", "一般"};

        for (int i = 0; i < tokens.size(); i++) {
            assertEquals(posLevel2[i], tokens.get(i).getPosLevel2());
        }

        String[] posLevel3 = {"*", "*", "一般", "*", "*", "*"};

        for (int i = 0; i < tokens.size(); i++) {
            assertEquals(posLevel3[i], tokens.get(i).getPosLevel3());
        }

        String[] posLevel4 = {"*", "*", "*", "*", "*", "*"};

        for (int i = 0; i < tokens.size(); i++) {
            assertEquals(posLevel4[i], tokens.get(i).getPosLevel4());
        }
    }

    @Test
    public void testCustomPenalties() {
        Tokenizer customTokenizer = new Tokenizer.Builder().mode(Tokenizer.Mode.SEARCH).penalties(3, 10000, Integer.MAX_VALUE, 0).build();
        String input = "シニアソフトウェアエンジニアを探しています";
        String[] expected1 = {"シニアソフトウェアエンジニア", "を", "探し", "て", "い", "ます"};
        List<Token> tokens = customTokenizer.tokenize(input);
        assertTrue(tokens.size() == expected1.length);
        for (int i = 0; i < tokens.size(); i++) {
            assertEquals(expected1[i], tokens.get(i).getSurfaceForm());
        }

        Tokenizer searchTokenizer = new Tokenizer.Builder().mode(Tokenizer.Mode.SEARCH).build();
        String[] expected2 = {"シニア", "ソフトウェア", "エンジニア", "を", "探し", "て", "い", "ます"};
        tokens = searchTokenizer.tokenize(input);
        assertTrue(tokens.size() == expected2.length);
        for (int i = 0; i < tokens.size(); i++) {
            assertEquals(expected2[i], tokens.get(i).getSurfaceForm());
        }
    }

    @Test
    public void testAllFeatures() {
        Tokenizer tokenizer = new Tokenizer.Builder().build();
        String input = "寿司が食べたいです。";

        List<Token> tokens = tokenizer.tokenize(input);
        assertEquals("寿司\t名詞,一般,*,*,*,*,寿司,スシ,スシ", toString(tokens.get(0)));
        assertEquals("が\t助詞,格助詞,一般,*,*,*,が,ガ,ガ", toString(tokens.get(1)));
        assertEquals("食べ\t動詞,自立,*,*,一段,連用形,食べる,タベ,タベ", toString(tokens.get(2)));
        assertEquals("たい\t助動詞,*,*,*,特殊・タイ,基本形,たい,タイ,タイ", toString(tokens.get(3)));
        assertEquals("です\t助動詞,*,*,*,特殊・デス,基本形,です,デス,デス", toString(tokens.get(4)));
    }

    private String toString(Token token) {
        return token.getSurfaceForm() + "\t" +  token.getAllFeatures();
    }

    @Test
    public void testBocchan() throws IOException, InterruptedException {
        int runs = 3;
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(
            this.getClass().getClassLoader().getResourceAsStream("bocchan.utf-8.txt")));

        String text = reader.readLine();
        reader.close();

        System.out.println("Test for Bocchan without pre-splitting sentences");

        long totalStart = System.currentTimeMillis();
        for (int i = 0; i < runs; i++) {
            List<Token> tokens = tokenizer.tokenize(text);
            for (Token token : tokens) {
                token.getAllFeatures();
            }
        }

        reportStatistics(totalStart, runs);

        System.out.println("Test for Bocchan with pre-splitting sentences");

        String[] sentences = text.split("、|。");

        totalStart = System.currentTimeMillis();
        for (int i = 0; i < runs; i++) {
            for (String sentence : sentences) {
                List<Token> tokens = tokenizer.tokenize(sentence);
                for (Token token : tokens) {
                    token.getAllFeatures();
                }
            }
        }

        reportStatistics(totalStart, runs);
    }

    private void reportStatistics(long totalStart, int runs) {
        long time = System.currentTimeMillis() - totalStart;

        System.out.println("Total time : " + time);
        System.out.println("Average time per run : " + (time / runs));
    }

}
