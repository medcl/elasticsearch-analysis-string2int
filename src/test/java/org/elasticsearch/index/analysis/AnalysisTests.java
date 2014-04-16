///*
//* Licensed to ElasticSearch and Shay Banon under one
//* or more contributor license agreements.  See the NOTICE file
//* distributed with this work for additional information
//* regarding copyright ownership. ElasticSearch licenses this
//* file to you under the Apache License, Version 2.0 (the
//* "License"); you may not use this file except in compliance
//* with the License.  You may obtain a copy of the License at
//*
//*    http://www.apache.org/licenses/LICENSE-2.0
//*
//* Unless required by applicable law or agreed to in writing,
//* software distributed under the License is distributed on an
//* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
//* KIND, either express or implied.  See the License for the
//* specific language governing permissions and limitations
//* under the License.
//*/
//
//package org.elasticsearch.index.analysis;
//
//import junit.framework.Assert;
//import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.TokenFilter;
//import org.apache.lucene.analysis.core.KeywordAnalyzer;
//import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
//import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
//import org.apache.lucene.util.Version;
//import org.elasticsearch.common.inject.Injector;
//import org.elasticsearch.common.inject.ModulesBuilder;
//import org.elasticsearch.common.joda.time.DateTime;
//import org.elasticsearch.common.settings.SettingsModule;
//import org.elasticsearch.env.Environment;
//import org.elasticsearch.env.EnvironmentModule;
//import org.elasticsearch.index.Index;
//import org.elasticsearch.index.IndexNameModule;
//import org.elasticsearch.index.settings.IndexSettingsModule;
//import org.elasticsearch.indices.analysis.IndicesAnalysisService;
//import org.hamcrest.MatcherAssert;
//import org.testng.annotations.Test;
//
//import java.io.IOException;
//import java.io.StringReader;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.elasticsearch.common.settings.ImmutableSettings.Builder.EMPTY_SETTINGS;
//import static org.hamcrest.Matchers.instanceOf;
//
///**
//*/
//public class AnalysisTests {
//
//    @Test
//    public void testPinyinAnalysis() {
//        Index index = new Index("test");
//
//        Injector parentInjector = new ModulesBuilder().add(new SettingsModule(EMPTY_SETTINGS), new EnvironmentModule(new Environment(EMPTY_SETTINGS))).createInjector();
//        Injector injector = new ModulesBuilder().add(
//                new IndexSettingsModule(index, EMPTY_SETTINGS),
//                new IndexNameModule(index),
//                new AnalysisModule(EMPTY_SETTINGS, parentInjector.getInstance(IndicesAnalysisService.class)).addProcessor(new String2IntAnalysisBinderProcessor()))
//                .createChildInjector(parentInjector);
//
//        AnalysisService analysisService = injector.getInstance(AnalysisService.class);
//
//        TokenizerFactory tokenizerFactory = analysisService.tokenizer("string2int");
//        MatcherAssert.assertThat(tokenizerFactory, instanceOf(String2IntTokenizerFactory.class));
//
//        TokenFilterFactory tokenFilterFactory = analysisService.tokenFilter("string2int");
//        MatcherAssert.assertThat(tokenFilterFactory,instanceOf(String2IntTokenFilterFactory.class));
//
//    }
//
//    @Test
//    public void testTokenFilter() throws IOException{
//        StringReader sr = new StringReader("刘德华 张学友");
//        Analyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_42);
//        String2IntTokenFilter filter = new String2IntTokenFilter(analyzer.tokenStream("f",sr),"localhost",6379,"key123",true,false);
//        List<String>  list= new ArrayList<String>();
//        filter.reset();
//        while (filter.incrementToken())
//        {
//            CharTermAttribute ta = filter.getAttribute(CharTermAttribute.class);
//            list.add(ta.toString());
//            System.out.println(ta.toString());
//        }
//        Assert.assertEquals(2, list.size());
//        Assert.assertEquals("1",list.get(0));
//        Assert.assertEquals("2", list.get(1));
//    }
//
//    @Test
//    public void TestTokenFilter1() throws IOException {
//        StringReader sr1 = new StringReader("刘德华");
//        Analyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_46);
//
//       TokenFilter filter = new String2IntTokenFilter(analyzer.tokenStream("f",sr1),"localhost",6379,"key123",true,false);
//        List<String>  list= new ArrayList<String>();
//        try {
//            filter.reset();
//            while (filter.incrementToken())
//            {
//                CharTermAttribute ta = filter.getAttribute(CharTermAttribute.class);
//                list.add(ta.toString());
//                System.out.println("value:" + ta.toString());
//
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Assert.assertEquals(1,list.size());
//        Assert.assertEquals("1", list.get(0));
//    }
//
//    @Test
//    public void TestTokenizer() throws IOException {
//        String[] s = {"斯巴达", "300"};
//        for (String value : s) {
//            System.out.println(value);
//            StringReader sr = new StringReader(value);
//
//            String2IntTokenizer tokenizer = new String2IntTokenizer(sr,"localhost",6379,"key11",true,false);
//
//            boolean hasnext = tokenizer.incrementToken();
//
//            while (hasnext) {
//
//                CharTermAttribute ta = tokenizer.getAttribute(CharTermAttribute.class);
//
//                System.out.println(ta.toString());
//
//                hasnext = tokenizer.incrementToken();
//
//            }
//        }
//
//    }
//
//
//
//    public void TestLRUTokenizer() throws IOException {
//
//        while (true){
//            String value= DateTime.now().toLocalDateTime().toString();
////            System.out.println(value);
//            StringReader sr = new StringReader(value);
//
//            String2IntTokenizer tokenizer = new String2IntTokenizer(sr,"localhost",6379,"key11",true,false);
////            String2IntTokenizer tokenizer = new String2IntTokenizer(sr,"localhost",6379,"key11",true,true);
//
//            tokenizer.reset();
//            boolean hasnext = tokenizer.incrementToken();
//
//            while (hasnext) {
//
//                CharTermAttribute ta = tokenizer.getAttribute(CharTermAttribute.class);
//
////                System.out.println(ta.toString());
//
//                hasnext = tokenizer.incrementToken();
//
//            }
//        }
//
//    }
//
//}
