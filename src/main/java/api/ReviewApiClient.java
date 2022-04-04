package api;


import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.mapping.*;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.PutMappingRequest;
import data.ReviewDocument;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import lombok.Getter;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

// Wraps crud functionality around metacritic index data
public class ReviewApiClient implements IApiClient<ReviewDocument> {

    @Getter
    private final ElasticsearchClient client;

    private static final Logger LOGGER = Logger.getLogger(ReviewApiClient.class.getName());

    public static final String INDEX = "metacritic";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ReviewApiClient() {
        this.client = new ElasticsearchClient(
                new RestClientTransport(
                        RestClient.builder(
                                new HttpHost("localhost", 9200)).build(), new JacksonJsonpMapper()
                )
        );

        LOGGER.info("Elasticsearch client created");
    }

    public void createMetacriticIndex() throws ElasticsearchException, IOException, URISyntaxException {

        var mapping = new HashMap<String, Property>();

        mapping.put("criticReview", new Property.Builder().boolean_(new BooleanProperty.Builder().build()).build());
        mapping.put("dateReviewed", new Property.Builder().date(new DateProperty.Builder().build()).build());
        mapping.put("gameName", new Property.Builder().text(new TextProperty.Builder().build()).build());
        mapping.put("reviewerName", new Property.Builder().text(new TextProperty.Builder().build()).build());
        mapping.put("id", new Property.Builder().long_(new LongNumberProperty.Builder().build()).build());
        mapping.put("type", new Property.Builder().text(new TextProperty.Builder().build()).build());
        mapping.put("score", new Property.Builder().float_(new FloatNumberProperty.Builder().build()).build());
        mapping.put("text", new Property.Builder().text(new TextProperty.Builder().build()).build());

        var req = new CreateIndexRequest.Builder()
                .index("metacritic")
                .mappings(
                        new TypeMapping.Builder()
                                .properties(mapping)
                                .build())
                .build();

        client.indices().create(req);
    }

    /**
     * Loads data from resources folder - file path must be relative to the root of src/main/resources
     */
    public void indexData(String filePath) throws IOException, URISyntaxException {
        var fileUri = getClass().getClassLoader().getResource(filePath);
        if (fileUri == null) {
            LOGGER.severe("File not found: " + filePath + "\n Skipping ...");
            return;
        }

        var jsonData = Files.readString(Path.of(fileUri.toURI()));
        var req = IndexRequest.of(b -> b
                .index("metacritic")
                .document(jsonData)
        );

        client.index(req);

        LOGGER.info("Indexed data from file: " + filePath);
    }


    private String serializeObject(ReviewDocument document) throws JsonProcessingException {
        return objectMapper.writeValueAsString(document);
    }

    private String serializeBatch(List<ReviewDocument> documents) throws JsonProcessingException {
        return objectMapper.writeValueAsString(documents);
    }

    @Override
    public void indexDocuments(List<ReviewDocument> documents) {
        try {
            var bulkOperations = new ArrayList<BulkOperation>(documents.size());
            documents.forEach(document -> {
                var indexOperation = new IndexOperation.Builder<ReviewDocument>()
                        .index(INDEX)
                        .id(String.valueOf(document.getId()))
                        .document(document)
                        .build();
                var bulkOperation = new BulkOperation.Builder()
                        .index(indexOperation);
                bulkOperations.add(bulkOperation.build());
            });

            var bulkRequest = new BulkRequest.Builder()
                    .operations(bulkOperations)
                    .build();

            var res = client.bulk(bulkRequest);
            if (res.errors()) {
                LOGGER.severe("Failed to index documents: " + res.items());
            } else {
                LOGGER.info("Indexed " + documents.size() + " documents");
            }
        } catch (IOException ex) {
            LOGGER.severe("Failed to index documents: " + ex.getMessage());
        }
    }

    @Override
    public void indexDocument(ReviewDocument document) {
        try {
            var indexRequest = new IndexRequest.Builder<ReviewDocument>()
                    .index(INDEX)
                    .id(String.valueOf(document.getId()))
                    .document(document)
                    .build();

            client.index(indexRequest);
        } catch (IOException ex) {
            LOGGER.severe("Failed to index document: " + ex.getMessage());
        }
    }

    @Override
    public void deleteDocument(long id) {
        try {
            var deleteRequest = new DeleteRequest.Builder()
                    .index(INDEX)
                    .id(String.valueOf(id))
                    .build();
            client.delete(deleteRequest);
        } catch (IOException ex) {
            LOGGER.severe("Failed to delete document: " + ex.getMessage());
        }
    }

    @Override
    public void updateDocument(ReviewDocument document) {
        try {
            var updateRequest = new UpdateRequest.Builder<ReviewDocument, ReviewDocument>()
                    .index(INDEX)
                    .id(String.valueOf(document.getId()))
                    .doc(document)
                    .build();
            client.update(updateRequest, ReviewDocument.class);
        } catch (IOException ex) {
            LOGGER.severe("Failed to update document: " + ex.getMessage());
        }
    }

    @Override
    public ReviewDocument getDocument(long id) {
        try {
            var getRequest = new GetRequest.Builder()
                    .index(INDEX)
                    .id(String.valueOf(id))
                    .build();
            var res = client.get(getRequest, ReviewDocument.class);
            return res.source();
        } catch (IOException ex) {
            LOGGER.severe("Failed to get document: " + ex.getMessage());
            return null;
        }
    }

    @Override
    public List<ReviewDocument> queryDocuments(Query query) {
        try {
            var searchRequest = new SearchRequest.Builder()
                    .query(query)
                    .build();

            var res = client.search(searchRequest, ReviewDocument.class);
            return res.hits()
                    .hits()
                    .stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            LOGGER.severe("Failed to find documents: " + ex.getMessage());
            return null;
        }
    }
}
