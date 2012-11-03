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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;

import java.io.IOException;
import java.io.Reader;


public final class String2IntAnalyzer extends Analyzer {


    private String redis_server;
    private String redis_key;
    private int redis_port;
    private boolean local_mem_cache=true;
    private RedisHanlder handler;

    public String2IntAnalyzer(Settings settings) {
        redis_server = settings.get("redis_server", "127.0.0.1");
        redis_port = Integer.valueOf(settings.get("redis_port", "6379"));
        redis_key = settings.get("redis_key", "default_key");
        String str=settings.get("local_mem_cache", "true");
        if(!str.equals("true")){local_mem_cache =false;}
        handler= RedisHanlder.getInstance(redis_server,redis_port,local_mem_cache);
    }

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        return new String2IntTokenizer(reader, redis_server,redis_port,redis_key,local_mem_cache);
    }

    @Override
    public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {

        //得到上一次使用的TokenStream，如果没有则生成新的，并且用setPreviousTokenStream放入成员变量，使得下一个可用。
        Tokenizer tokenizer = (Tokenizer) getPreviousTokenStream();

        if (tokenizer == null) {
            tokenizer = new String2IntTokenizer(reader, redis_server,redis_port,redis_key,local_mem_cache);
            setPreviousTokenStream(tokenizer);
        } else {
            //如果上一次生成过TokenStream，则reset初始化。
            tokenizer.reset(reader);
        }
        return tokenizer;
    }
}
