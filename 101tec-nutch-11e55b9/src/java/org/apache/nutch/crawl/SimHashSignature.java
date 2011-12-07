/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nutch.crawl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.Text;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.shingle.ShingleMatrixFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseUtil;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.util.NutchConfiguration;

/**
 * <p>
 * An implementation of a page signature. It calculates an simhash the text of a
 * page. In case there is no text, it calculates a hash using the
 * {@link MD5Signature}.
 * </p>
 * <p>
 * The algorithm to calculate a page "profile" takes the plain text version of a
 * page and performs the following steps:
 * <ul>
 * <li>create tokens equal or shorter than MIN_TOKEN_LEN (default 2 characters),
 * </li>
 * <li>remove all characters except letters (or digits), and bring all
 * characters to lower case,</li>
 * <li>split the text into shingles (3 words per default),</li>
 * <li>hash the shingles using MurmurHash64 for better performance and
 * distribution,</li>
 * <li>use the MD5 hash of the resulting simhash as fingerprint.</li>
 * </ul>
 * <p>
 * The behavior can be configured as follows:
 * </p>
 * <ul>
 * <li>ingrid.signature.simhash.min_token_len (default:2) - minimum token
 * length, shorter tokens are ignored. the token size is computed AFTER
 * non-letters have been removed</li>
 * <li>ingrid.signature.simhash.shingle_size (default:3) - number of shingles,
 * number of words</li>
 * <li>ingrid.signature.simhash.min_shingle_frequency (default:1) - ignore
 * shingles with lower frequency</li>
 * <li>ingrid.signature.simhash.ignore_digits (default:true) - ignore digits,
 * use only letters</li>
 * <li>ingrid.signature.simhash.max_number_of_hashes (default:100) - number of
 * hash values to use for the simhash. if this number is exceeded keep the
 * number of lowest hash values.</li>
 * </ul>
 * 
 * @author joachim@wemove.com;
 */
public class SimHashSignature extends Signature {

    Signature fallback = new MD5Signature();

    public static final Log LOG = LogFactory.getLog(SimHashSignature.class.getName());

    private int MIN_TOKEN_LEN;

    private int SHINGLE_SIZE;

    private int MIN_SHINGLE_FREQUENCY;

    private boolean IGNORE_DIGITS;

    private int MAX_NUMBER_OF_HASHES;

    public byte[] calculate(Content content, Parse parse) {
        MIN_TOKEN_LEN = getConf().getInt("ingrid.signature.simhash.min_token_len", 2);
        SHINGLE_SIZE = getConf().getInt("ingrid.signature.simhash.shingle_size", 3);
        MIN_SHINGLE_FREQUENCY = getConf().getInt("ingrid.signature.simhash.min_shingle_frequency", 1);
        IGNORE_DIGITS = getConf().getBoolean("ingrid.signature.simhash.ignore_digits", true);
        MAX_NUMBER_OF_HASHES = getConf().getInt("ingrid.signature.simhash.max_number_of_hashes", 100);

        String text = null;
        if (parse != null)
            text = parse.getText();
        if (text == null || text.length() == 0) {
            return fallback.calculate(content, parse);
        }

        Analyzer analyzer = new NGramAnalyzer();

        TokenStream stream = analyzer.tokenStream("content", new StringReader(text));
        Token token = new Token();
        HashMap<String, Integer> lTokens = new HashMap<String, Integer>();
        try {
            while ((token = stream.next(token)) != null) {
                int counter = 0;
                if (lTokens.containsKey(token.term())) {
                    counter = lTokens.get(token.term()).intValue() + 1;
                } else {
                    counter = 1;
                }
                lTokens.put(token.term(), new Integer(counter));
            }
        } catch (IOException e) {
            LOG.error("Error shingling text.", e);
        } catch (NullPointerException e) {
            LOG.error("Error shingling text. This seems to be a problem with the org.apache.lucene.analysis.shingle.ShingleMatrixFilter.", e);
        }

        SimHasher2 simHasher = new SimHasher2(MAX_NUMBER_OF_HASHES);
        //MurmurHash64 hasher = new MurmurHash64();
        MD5Hash hasher = new MD5Hash();

        for (String t : lTokens.keySet()) {
            if (lTokens.get(t).intValue() >= MIN_SHINGLE_FREQUENCY) {
                simHasher.addFeature(MD5Hash.digest(t).hashCode(), lTokens.get(t).intValue());
            }
        }

        long fingerprint = simHasher.summarize();
        int shingleFrequency = MIN_SHINGLE_FREQUENCY;
        while (fingerprint == 0) {
            if (shingleFrequency == 1) {
                LOG.warn("No shingles found with frequency " + shingleFrequency + " for url '" + content.getUrl()
                        + "', using fallback signature method.");
                return fallback.calculate(content, parse);
            }
            LOG.warn("No shingles found with frequency " + shingleFrequency + " for url '" + content.getUrl()
                    + "', lowering frequency by one.");
            shingleFrequency--;
            for (String t : lTokens.keySet()) {
                if (lTokens.get(t).intValue() >= shingleFrequency) {
                    simHasher.addFeature(MD5Hash.digest(t).hashCode(), lTokens.get(t).intValue());
                }
            }
            fingerprint = simHasher.summarize();

        }
        return MD5Hash.digest(Long.toString(fingerprint).getBytes()).getDigest();

    }

    private class NGramAnalyzer extends Analyzer {
        public TokenStream tokenStream(String fieldName, Reader reader) {
            return new ShingleMatrixFilter(new MyTokenFilter(new StandardTokenizer(reader)), SHINGLE_SIZE,
                    SHINGLE_SIZE, ' ');
        }

        public class MyTokenFilter extends Tokenizer {

            Tokenizer t = null;

            public MyTokenFilter(Tokenizer tokenizer) {
                this.t = tokenizer;
            }

            @Override
            public Token next(Token reusableToken) throws IOException {
                reusableToken = t.next(reusableToken);
                while (reusableToken != null) {
                    while (reusableToken.term().length() <= MIN_TOKEN_LEN) {
                        reusableToken = t.next(reusableToken);
                    }
                    String term = reusableToken.term();
                    StringBuffer filteredTerm = new StringBuffer();
                    for (int i = 0; i < term.length(); i++) {
                        char c = term.charAt(i);
                        if (Character.isLetter(c) || (!IGNORE_DIGITS && Character.isDigit(c))) {
                            filteredTerm.append(Character.toLowerCase(c));
                        }
                    }
                    if (filteredTerm.length() > MIN_TOKEN_LEN) {
                        reusableToken.setTermBuffer(filteredTerm.toString());
                        return reusableToken;
                    } else {
                        reusableToken = t.next(reusableToken);
                    }
                }
                return null;
            }
        }
    }

    final class SimHasher2 {

        int dim = 10;

        TreeMap<Integer, Integer> hashes;

        public SimHasher2(int dim) {
            this.dim = dim;
            hashes = new TreeMap<Integer, Integer>();
        }

        public void addFeature(final int fhash, final int weight) {
            if (hashes.size() < dim) {
                hashes.put(fhash, weight);
            } else {
                Map.Entry<Integer, Integer> maxHash = hashes.lastEntry();
                if (maxHash.getKey() > fhash) {
                    hashes.remove(maxHash.getKey());
                    hashes.put(fhash, weight);
                }
            }
        }

        public long summarize() {
            for (Integer h : hashes.keySet()) {
                int weight = hashes.get(h);
                int bit = 1;
                for (int i = 0; i < 32; ++i) {
                    if ((h & bit) != 0) {
                        _fvect[i] += weight;
                    } else {
                        _fvect[i] -= weight;
                    }
                    bit <<= 1;
                }
            }

            int simhash = 0;
            for (int i = 0; i < 32; ++i) {
                if (_fvect[i] > 0)
                    simhash |= 1L;
                simhash <<= 1;
            }
            return simhash;
        }

        private final int[] _fvect = new int[32];

    }

    final class SimHasher {
        public void addFeature(final long fhash, final int weight) {
            long bit = 1L;
            for (int i = 0; i < 64; ++i) {
                if ((fhash & bit) != 0L) {
                    _fvect[i] += weight;
                } else {
                    _fvect[i] -= weight;
                }
                bit <<= 1;
            }
        }

        public long summarize() {
            long simhash = 0;
            for (int i = 0; i < 64; ++i) {
                if (_fvect[i] > 0)
                    simhash |= 1L;
                simhash <<= 1;
            }
            return simhash;
        }

        private final int[] _fvect = new int[64];
    }

    public static long hammingDistance(int hash1, int hash2) {
        int x = (hash1 ^ hash2);
        int dist = 0;
        while (x != 0) {
            dist += 1;
            x &= x - 1;
        }
        return dist;
    }

    public static double similarity(int hash1, int hash2) {
        double a = hash1 * 1.0;
        double b = hash2 * 1.0;
        if (a > b)
            return b / a;
        return a / b;
    }

    private final class MurmurHash64 {
        /**
         * Generate a 64 bit hash from CharSequence (as UCS-2, 16-bit
         * characters).
         */
        public long hash(final CharSequence text) {
            return hash(text, 0, text.length());
        }

        /**
         * Generate a 64 bit hash from bytes with default seed value.
         */
        public long hash(ByteBuffer b) {
            // FIXME: Support native buffers?
            return hash(b.array(), b.arrayOffset() + b.position(), b.remaining());
        }

        /**
         * Generate a 64 bit hash from byte array with default seed value.
         */
        public long hash(final byte[] data, final int offset, final int length) {
            return hash(data, offset, length, 0xe17a1465);
        }

        /**
         * Generate a 64 bit hash from data of the given length, with seed.
         */
        public long hash(final byte[] data, int offset, final int length, final int seed) {
            // 'm' and 'r' are mixing constants generated offline.
            // They're not really 'magic', they just happen to work well.
            final long m = 0xc6a4a7935bd1e995L;
            final int r = 47;

            long h = (seed & 0xffffffffL) ^ (length * m);

            final int octets = length / 8;
            for (int i = 0; i < octets; ++i) {

                long k = (((data[offset++] & 0xffL)) | ((data[offset++] & 0xffL) << 8)
                        | ((data[offset++] & 0xffL) << 16) | ((data[offset++] & 0xffL) << 24)
                        | ((data[offset++] & 0xffL) << 32) | ((data[offset++] & 0xffL) << 40)
                        | ((data[offset++] & 0xffL) << 48) | ((data[offset++] & 0xffL) << 56));

                k *= m;
                k ^= k >>> r;
                k *= m;

                h ^= k;
                h *= m;
            }

            switch (length % 8) {
            case 7:
                h ^= (data[offset + 6] & 0xffL) << 48;
            case 6:
                h ^= (data[offset + 5] & 0xffL) << 40;
            case 5:
                h ^= (data[offset + 4] & 0xffL) << 32;
            case 4:
                h ^= (data[offset + 3] & 0xffL) << 24;
            case 3:
                h ^= (data[offset + 2] & 0xffL) << 16;
            case 2:
                h ^= (data[offset + 1] & 0xffL) << 8;
            case 1:
                h ^= (data[offset] & 0xffL);
                h *= m;
            }

            h ^= h >>> r;
            h *= m;
            h ^= h >>> r;

            return h;
        }

        /**
         * Generate a 64 bit hash from the specified CharSequence(as UCS-2) ,
         * with default seed value.
         */
        public long hash(final CharSequence cs, final int offset, final int length) {
            return hash(cs, offset, length, 0xe17a1465);
        }

        /**
         * Generate a 64 bit hash from CharSequence (as UCS-2), with seed.
         */
        public long hash(final CharSequence cs, int offset, final int length, final int seed) {
            // 'm' and 'r' are mixing constants generated offline.
            // They're not really 'magic', they just happen to work well.
            final long m = 0xc6a4a7935bd1e995L;
            final int r = 47;

            long h = (seed & 0xffffffffL) ^ (length * m);

            final int quartets = length / 4;
            for (int i = 0; i < quartets; ++i) {

                long k = (((cs.charAt(offset++) & 0xffffL)) | ((cs.charAt(offset++) & 0xffffL) << 16)
                        | ((cs.charAt(offset++) & 0xffffL) << 32) | ((cs.charAt(offset++) & 0xffffL) << 48));

                k *= m;
                k ^= k >>> r;
                k *= m;

                h ^= k;
                h *= m;
            }

            switch (length % 4) {
            case 3:
                h ^= (cs.charAt(offset + 2) & 0xffffL) << 32;
            case 2:
                h ^= (cs.charAt(offset + 1) & 0xffffL) << 16;
            case 1:
                h ^= (cs.charAt(offset) & 0xffffL);
                h *= m;
            }

            h ^= h >>> r;
            h *= m;
            h ^= h >>> r;

            return h;
        }

    }

    public static void main(String[] args) throws Exception {
        SimHashSignature sig = new SimHashSignature();
        Configuration config = NutchConfiguration.create();
        sig.setConf(NutchConfiguration.create());
        LinkedHashMap<String, byte[]> res = new LinkedHashMap<String, byte[]>();
        for (int i = 0; i < args.length; i++) {
            URL url = new URL(args[i]);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer text = new StringBuffer();
            String line = null;
            while ((line = br.readLine()) != null) {
                if (text.length() > 0)
                    text.append("\n");
                text.append(line);
            }
            br.close();
            Content c = new Content(args[i], args[i], text.toString().getBytes(), "application/xhtml+xml",
                    new Metadata(), config);
            ParseUtil pu = new ParseUtil(config);
            for (Entry<Text, Parse> entry : pu.parse(c)) {
                Text key = entry.getKey();
                Parse parse = entry.getValue();

                byte[] signature = sig.calculate(null, parse);
                res.put(args[i], signature);
            }
        }
        Iterator<String> it = res.keySet().iterator();
        int lastSigInt = 0;
        while (it.hasNext()) {
            String name = it.next();
            byte[] signature = res.get(name);
            MD5Hash hash = new MD5Hash(signature);
            int sigInt = hash.hashCode();
            System.out.println(name + "\t" + sigInt);
            if (lastSigInt != 0) {
                System.out.println("hammingDistance to last: \t" + sig.hammingDistance(sigInt, lastSigInt));
            }
            if (lastSigInt != 0) {
                System.out.println("similarity to last: \t" + sig.similarity(sigInt, lastSigInt));
            }
            lastSigInt = sigInt;
        }
    }
}
