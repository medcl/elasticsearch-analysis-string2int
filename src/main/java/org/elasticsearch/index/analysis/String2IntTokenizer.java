package org.elasticsearch.index.analysis;
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

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

import java.io.IOException;
import java.io.Reader;

/**
 * Created by IntelliJ IDEA.
 * User: Medcl'
 * Date: 12-5-21
 * Time: 下午5:53
 */
public class String2IntTokenizer extends Tokenizer {

    private static final int DEFAULT_BUFFER_SIZE = 256;

    private boolean done = false;
    private int finalOffset;
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private String redis_server;
    private String redis_key;
    private int redis_port;
    private boolean local_mem_cache = true;
    private boolean use_lru_cache = true;
    private RedisHanlder handler;
    private static ESLogger logger = Loggers.getLogger("sting2int");

    public String2IntTokenizer(String redis_server, int redis_port, String redis_key, boolean local_mem_cache,boolean use_lru_cache) {
        this(DEFAULT_BUFFER_SIZE);
        this.redis_server = redis_server;
        this.redis_key = redis_key;
        this.redis_port = redis_port;
        this.local_mem_cache = local_mem_cache;
        this.use_lru_cache = use_lru_cache;
        handler = RedisHanlder.getInstance(redis_server, redis_port, local_mem_cache, use_lru_cache);
    }

    public String2IntTokenizer(int bufferSize) {
        super();
        termAtt.resizeBuffer(bufferSize);
    }


    @Override
    public final boolean incrementToken() throws IOException {
        if (!done) {
            clearAttributes();
            done = true;
            int upto = 0;
            char[] buffer = termAtt.buffer();
            while (true) {
                final int length = input.read(buffer, upto, buffer.length - upto);
                if (length == -1) break;
                upto += length;
                if (upto == buffer.length)
                    buffer = termAtt.resizeBuffer(1 + buffer.length);
            }
            termAtt.setLength(upto);
            String str = termAtt.toString();
            termAtt.setEmpty();
            long converted = handler.convert(redis_key, str);
            termAtt.append(String.valueOf(converted));

            if(logger.isDebugEnabled())
            {
                logger.debug(str + ">" + converted);
            }

            finalOffset = correctOffset(upto);
            offsetAtt.setOffset(correctOffset(0), finalOffset);
            return true;
        }
        return false;
    }

    @Override
    public final void end() throws IOException {
        super.end();
        // set final offset
        offsetAtt.setOffset(finalOffset, finalOffset);
    }


    @Override
    public void reset() throws IOException {
        super.reset();
        this.done = false;
    }

}
