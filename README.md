String2Integer Analysis for ElasticSearch
==================================



you can download this plugin from RTF project(https://github.com/medcl/elasticsearch-rtf)

    --------------------------------------------------
    | String2Integer Analysis Plugin| ElasticSearch  |
    --------------------------------------------------
    | 1.3.0                        | 1.0.0           |
    --------------------------------------------------
    | 1.2.0                        | 0.90.x          |
    --------------------------------------------------
    | 1.0.0                        | 0.19.x          |
    --------------------------------------------------

The plugin includes a `string2int` analyzer , a `string2int` tokenizer  and a `string2int` token-filter.

this plugin is used for saving your memory and reduce the size of your index.
sometimes there are some entities in our index,for example,people's name,the title of you position,and so on,
generally we set these fields to `not_analyzed` and hope to use them together,
and they often slightly change ,but if you wanna do faceting over these fields,
you should be very carefully,because the memory usage is a headache,especially it contains a lot of of terms,
but if you can convert these long string entities into numbers,
the memory usage will be a little smaller,and make the impossible thing  to be possible.

the plugin use redis to store the mapping of your entity and the number.
the number is assigned by auto_increment style,and in order to speedup the indexing,there is a local cache in memory.


how to use this plugin?

1.step one,add a custom analysis type in the elasticsearch.yml

<pre>
index:
  analysis:
    analyzer:
      string2int:
          type: org.elasticsearch.index.analysis.String2IntAnalyzerProvider
          redis_server: "127.0.0.1"
          redis_port: 6379
          redis_key: "index1_type1_name1"
</pre>

the `redis_key` is like a catalog of your entities

2. step two,create a index,and create a type ,and make sure the field's index_analyzer is `string2int`,the analyzer we just defined..

<pre>
curl -XPOST http://localhost:9200/index/string2int/_mapping -d'
{
    "string2int": {
        "_meta": {
            "author": "medcl"
        },
        "_all": {
            "analyzer": "ik"
        },
        "_source": {
            "enabled": false
        },
        "properties": {
            "author": {
                "type": "string",
                "index_analyzer": "string2int",
                "search_analyzer": "keyword",
                "include_in_all": false,
                "store":true
            }
        }
    }
}'

</pre>

3.step 3,as the filed is named "author",so let's index some people

<pre>
 curl -XPOST http://localhost:9200/index/string2int/1 -d'
 {"author":"medcl"}'

 curl -XPOST http://localhost:9200/index/string2int/2 -d'
 {"author":"michael jackson"}'

 ...

</pre>

4.do faceting now

<pre>
curl -XPOST http://localhost:9200/index/string2int/_search -d'
{
    "query": {
        "query_string": {
            "query": "*"
        }
    },
    "facets": {
        "author": {
            "terms": {
                "field": "author"
            }
        }
    }
}'
</pre>
 response:
<pre>
{"took":9,"timed_out":false,"_shards":{"total":5,"successful":5,"failed":0},"hits":{"total":3,"max_score":1.0,"hits":[{"_index":"index","_type":"string2int","_id":"1","_score":1.0},{"_index":"index","_type":"string2int","_id":"2","_score":1.0},{"_index":"index","_type":"string2int","_id":"3","_score":1.0}]},"facets":{"author":{"_type":"terms","missing":0,"total":3,"other":0,"terms":[{"term":"6","count":2},{"term":"7","count":1}]}}}
</pre>

the next step is to replace the everything back.
and you can also change the field's mapping,by set `store` to `true`,to get them back directly.


the default value is below,you should change them first:
<pre>
redis
    redis_server :"127.0.0.1"
    redis_port:6379
    redis_key:"default_key"
    local_mem_cache: "true"
    use_lru_cache: "true"
</pre>

another token-filter example:
<pre>
index:
  analysis:
    tokenizer:
      my_string2int:
          type: "string2int"
          redis_server: "127.0.0.1"
          redis_port: 6379
          redis_key: "index1_type2_name2"

    analyzer:
      custom_string2int:
          type: custom
          tokenizer: whitespace
          filter: [my_string2int,lowercase]
</pre>