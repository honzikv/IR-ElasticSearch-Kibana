import api.ReviewApiClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.json.JsonData;
import data.DocumentSerialization;
import data.ReviewDocument;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import static java.lang.System.exit;

public class App {

    public static final Logger LOGGER = Logger.getLogger(App.class.getName());

    private static void testApi(long documentId, ReviewApiClient apiClient, List<ReviewDocument> documents) {
        // Simple use case to verify that api calls are working as intended
        var document = documents.get((int) documentId);
        var documentFromES = apiClient.getDocument(document.getId());
        assert document.equals(documentFromES);

        document.setGameName("New Game Name");
        apiClient.updateDocument(document);
        var updatedDocumentFromES = apiClient.getDocument(document.getId());
        assert document.equals(updatedDocumentFromES);

        apiClient.deleteDocument(document.getId());
        assert null == apiClient.getDocument(document.getId());

    }

    //    {
//        "query": {
//        "bool": {
//            "must_not": [
//            {
//                "match": {
//                "text": {
//                    "query": "best game",
//                            "operator": "and"
//                }
//            }
//            }
//      ],
//            "must": [
//            {
//                "range": {
//                "score": {
//                    "gte": 70,
//                            "lte": 80
//                }
//            }
//            }
//      ]
//        }
//    }
//    }
    private static void queryExample1(ReviewApiClient apiClient) {
        var matchBestGameQuery = new Query.Builder()
                .match(
                        new MatchQuery.Builder()
                                .field("text")
                                .operator(Operator.And)
                                .query("best game")
                                .build())
                .build();

        var rangeBetween70and80Query = new Query.Builder()
                .range(
                        new RangeQuery.Builder()
                                .field("score")
                                .gte(JsonData.of(70))
                                .lte(JsonData.of(80))
                                .build())
                .build();

        var query = new Query.Builder()
                .bool(new BoolQuery.Builder()
                        .mustNot(matchBestGameQuery)
                        .must(rangeBetween70and80Query)
                        .build())
                .build();

        var documents = apiClient.queryDocuments(query);
        LOGGER.info("Found " + documents.size() + " documents");
    }

    //    GET metacritic/_search
//    {
//        "query": {
//        "bool": {
//            "must": [
//            {
//                "match": {
//                "text": {
//                    "query": "best game",
//                            "operator": "and"
//                }
//            }
//            }
//      ],
//            "filter": [
//            {
//                "range": {
//                "score": {
//                    "gte": 90,
//                            "lte": 100
//                }
//            }
//            }
//      ]
//        }
//    }
//    }
    private static void queryExample2(ReviewApiClient apiClient) {
        // Example with filter
        var matchBestGameQuery = new Query.Builder()
                .match(
                        new MatchQuery.Builder()
                                .field("text")
                                .operator(Operator.And)
                                .query("best game")
                                .build())
                .build();

        var rangeBetween90to100 = new Query.Builder()
                .range(
                        new RangeQuery.Builder()
                                .field("score")
                                .gte(JsonData.of(90))
                                .lte(JsonData.of(100))
                                .build())
                .build();

        var query = new Query.Builder()
                .bool(new BoolQuery.Builder()
                        .must(matchBestGameQuery)
                        .filter(rangeBetween90to100)
                        .build())
                .build();

        var documents = apiClient.queryDocuments(query);
        LOGGER.info("Found " + documents.size() + " documents");
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        var documentSerializer = new DocumentSerialization();
        var documents = documentSerializer.deserializeDocuments("documents.json");
        var apiClient = new ReviewApiClient();

        apiClient.indexDocuments(documents.subList(0, 100)); // this will do nothing since the documents already exist

        // Simple CRUD operations test
        for (var i = 0L; i < 10L; i += 1) {
            testApi(i, apiClient, documents);
        }

        // Search test
        queryExample1(apiClient);
        queryExample2(apiClient);

        LOGGER.info("All tests passed");
        exit(0);
    }


}
