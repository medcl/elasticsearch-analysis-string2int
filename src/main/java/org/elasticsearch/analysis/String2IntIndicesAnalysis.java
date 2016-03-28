package org.elasticsearch.analysis;


import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.analysis.*;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;

/**
 * Registers indices level analysis components so, if not explicitly configured,
 * will be shared among all indices.
 */
public class String2IntIndicesAnalysis extends AbstractComponent {

    @Inject
    public String2IntIndicesAnalysis(final Settings settings,
                                 IndicesAnalysisService indicesAnalysisService, Environment env) {
        super(settings);

        final String redis_server = settings.get("redis_server", "127.0.0.1");
        final Integer redis_port = Integer.valueOf(settings.get("redis_port", "6379"));
        final String redis_key = settings.get("redis_key", "default_key");
        String str = settings.get("local_mem_cache", "true");

        boolean local_mem_cache=true;
        boolean use_lru_cache=true;
        if (!str.equals("true")) {
            local_mem_cache = false;
        }

        str = settings.get("use_lru_cache", "true");
        if (!str.equals("true")) {
            use_lru_cache = false;
        }

        //analyzers
        indicesAnalysisService.analyzerProviderFactories().put("string2int",
                new PreBuiltAnalyzerProviderFactory("string2int", AnalyzerScope.GLOBAL,
                        new String2IntAnalyzer(settings)));


        //tokenizers
        final boolean finalLocalMemCache = local_mem_cache;
        final boolean finalUseLruCache = use_lru_cache;
        indicesAnalysisService.tokenizerFactories().put("string2int",
                new PreBuiltTokenizerFactoryFactory(new TokenizerFactory() {
                    @Override
                    public String name() {
                        return "string2int";
                    }

                    @Override
                    public Tokenizer create() {
                        return new String2IntTokenizer(redis_server, redis_port, redis_key, finalLocalMemCache, finalUseLruCache);
                    }
                }));


        //tokenFilters
        indicesAnalysisService.tokenFilterFactories().put("string2int",
                new PreBuiltTokenFilterFactoryFactory(new TokenFilterFactory() {
                    @Override
                    public String name() {
                        return "string2int";
                    }

                    @Override
                    public TokenStream create(TokenStream tokenStream) {
                        return new String2IntTokenFilter(tokenStream, redis_server, redis_port, redis_key, finalLocalMemCache,finalUseLruCache);
                    }
                }));

    }
}