GRAPH_STORE_PARAM = "-Dcloudext.graphstore.drivers=com.antgroup.openspg.cloudext.impl.graphstore.tugraph" \
                    ".TuGraphStoreClientDriver"

SEARCH_CLIENT_PARAM = "-Dcloudext.searchengine.drivers=com.antgroup.openspg.cloudext.impl.searchengine.elasticsearch" \
                      ".ElasticSearchEngineClientDriver"

LOCAL_BUILDER_JAR = "builder-runner-local-0.0.1-SNAPSHOT-jar-with-dependencies.jar"

LOCAL_REASONER_JAR = ""

LOCAL_SCHEMA_URL = "http://localhost:8887"

LOCAL_GRAPH_STORE_URL = "tugraph://127.0.0.1:9090?graphName=default&timeout=50000&accessId=admin&accessKey=73@TuGraph"

LOCAL_SEARCH_ENGINE_URL = "elasticsearch://127.0.0.1:9200?scheme=http"
