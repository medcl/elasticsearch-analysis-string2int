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

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

import java.io.IOException;

/**
 */
public class String2IntTokenFilter extends TokenFilter {

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private String redis_server;
    private String redis_key;
    private int redis_port;
    private boolean local_mem_cache = true;
    private RedisHanlder handler;
    private static ESLogger logger = Loggers.getLogger("sting2int");
    private boolean use_lsu_cache;

    @Override
    public final boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {
            return false;
        }

        StringBuilder stringBuilder = new StringBuilder();

        String str = termAtt.toString();
        termAtt.setEmpty();
        long converted = handler.convert(redis_key, str);
        stringBuilder.append(String.valueOf(converted));
        if(logger.isDebugEnabled())
        {
            logger.debug(str + ">" + converted);
        }


        termAtt.resizeBuffer(stringBuilder.length());
        termAtt.append(stringBuilder.toString());
        termAtt.setLength(stringBuilder.length());

        return true;
    }

    public String2IntTokenFilter(TokenStream in, String redis_server, int redis_port, String redis_key, boolean local_mem_cache,boolean use_lru_cache) {
        super(in);
        this.redis_server = redis_server;
        this.redis_key = redis_key;
        this.redis_port = redis_port;
        this.local_mem_cache = local_mem_cache;
        this.use_lsu_cache = use_lru_cache;
        handler = RedisHanlder.getInstance(redis_server, redis_port, local_mem_cache,use_lru_cache);
    }

    @Override
    public final void end() throws IOException {
        // set final offset
        super.end();
    }

    @Override
    public void reset() throws IOException {
        super.reset();
    }
}
