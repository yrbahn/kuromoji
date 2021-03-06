/**
 * Copyright 2010-2013 Atilika Inc. and contributors (see CONTRIBUTORS.md)
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.  A copy of the
 * License is distributed with this work in the LICENSE.md file.  You may
 * also obtain a copy of the License from
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atilika.kuromoji.util;

import com.atilika.kuromoji.dict.ConnectionCosts;
import com.atilika.kuromoji.dict.TokenInfoDictionary;
import com.atilika.kuromoji.dict.UnknownDictionary;
import com.atilika.kuromoji.trie.DoubleArrayTrie;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public abstract class AbstractDictionaryBuilder {

    public void build(String inputDirname, String outputDirname, String encoding, boolean compactTries) throws IOException {
        File outputDir = new File(outputDirname);
        outputDir.mkdirs();
        buildTokenInfoDictionary(inputDirname, outputDirname, encoding, compactTries);
        buildUnknownWordDictionary(inputDirname, outputDirname, encoding);
        buildConnectionCosts(inputDirname, outputDirname);
    }

    private void buildTokenInfoDictionary(String inputDirname, String outputDirname, String encoding, boolean compactTrie) throws IOException {
        System.out.println("building tokeninfo dict...");
        AbstractTokenInfoDictionaryBuilder tokenInfoBuilder = getTokenInfoDictionaryBuilder(encoding);
        TokenInfoDictionary tokenInfoDictionary = tokenInfoBuilder.build(inputDirname);

        List<String> surfaces = tokenInfoDictionary.getSurfaces();

        System.out.print("  building double array trie...");
        DoubleArrayTrie trie = DoubleArrayTrieBuilder.build(surfaces, compactTrie);
        trie.write(outputDirname);
        System.out.println("  done");

        System.out.print("  processing target map...");

        for (int i = 0; i < surfaces.size(); i++) {
            int doubleArrayId = trie.lookup(surfaces.get(i));
            assert doubleArrayId > 0;
            tokenInfoDictionary.addMapping(doubleArrayId, i);
        }

        tokenInfoDictionary.write(outputDirname);

        System.out.println("done");
        System.out.println("done");
    }

    abstract protected AbstractTokenInfoDictionaryBuilder getTokenInfoDictionaryBuilder(String encoding);

    private void buildUnknownWordDictionary(String inputDirname, String outputDirname, String encoding) throws IOException {
        System.out.print("building unknown word dict...");
        UnknownDictionaryBuilder unkBuilder = new UnknownDictionaryBuilder(encoding);
        UnknownDictionary unkDictionary = unkBuilder.build(inputDirname);
        unkDictionary.write(outputDirname);
        System.out.println("done");
    }

    private void buildConnectionCosts(String inputDirname, String outputDirname) throws IOException {
        System.out.print("building connection costs...");
        ConnectionCosts connectionCosts = ConnectionCostsBuilder.build(inputDirname + File.separator + "matrix.def");
        OutputStream os = new FileOutputStream(outputDirname + File.separator + ConnectionCosts.FILENAME);
        connectionCosts.write(os);
        os.close();
        System.out.println("done");
    }

    protected void build(String[] args) throws IOException {
        String inputDirname = args[0];
        String outputDirname = args[1];
        String inputEncoding = args[2];
        boolean compactTries = Boolean.parseBoolean(args[3]);

        System.out.println("dictionary builder");
        System.out.println("");
        System.out.println("input directory: " + inputDirname);
        System.out.println("output directory: " + outputDirname);
        System.out.println("input encoding: " + inputEncoding);
        System.out.println("compact tries: " + compactTries);
        System.out.println("");

        build(inputDirname, outputDirname, inputEncoding, compactTries);
    }
}
