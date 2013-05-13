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

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.settings.IndexSettings;


public class String2IntTokenFilterFactory extends AbstractTokenFilterFactory {
    private String redis_server;
    private int redis_port;
    private String redis_key;
    private boolean local_mem_cache;
    private boolean use_lru_cache;

    @Inject
    public String2IntTokenFilterFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        redis_server = settings.get("redis_server", "127.0.0.1");
        redis_port = Integer.valueOf(settings.get("redis_port", "6379"));
        redis_key = settings.get("redis_key", "default_key");
        String str = settings.get("local_mem_cache", "true");
        if (!str.equals("true")) {
            local_mem_cache = false;
        }
        str = settings.get("use_lru_cache", "true");
        if (!str.equals("true")) {
            use_lru_cache = false;
        }
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new String2IntTokenFilter(tokenStream, redis_server, redis_port, redis_key, local_mem_cache,use_lru_cache);
    }
}