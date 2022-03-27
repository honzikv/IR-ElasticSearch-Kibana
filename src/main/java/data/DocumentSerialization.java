package data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DocumentSerialization {

    private static long idCounter = 0;

    private static ReviewDocument mapDocument(MetacriticReview review, String metacriticGameName, boolean criticReview) {
        return new ReviewDocument(idCounter++, metacriticGameName, review.getReviewerName(),
                review.getDateReviewed(), review.getScore(), review.getText(), criticReview);
    }

    /**
     * Method to transform preprocessed json to indexable documents
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public List<ReviewDocument> deserializePreprocessedData(String filePath) throws IOException {
        idCounter = 0; // fuj
        var objectMapper = new JsonMapper();
        var fileUri = getClass().getClassLoader().getResource(filePath);
        if (fileUri == null) {
            throw new IllegalArgumentException("File not found");
        }

        var gamesWithReviews = objectMapper.readValue(fileUri, new TypeReference<List<MetacriticGame>>() { });

        return gamesWithReviews.stream().map(metacriticGame -> {
            var documents = new ArrayList<ReviewDocument>();
            metacriticGame.getCriticReviews()
                    .forEach(review -> documents.add(mapDocument(review, metacriticGame.getName(), true)));
            metacriticGame.getUserReviews()
                    .forEach(review -> documents.add(mapDocument(review, metacriticGame.getName(), false)));

            return documents;
        }).flatMap(List::stream).collect(Collectors.toList());
    }

    public void serializeDocuments(List<ReviewDocument> documents, String filePath) throws IOException {
        new JsonMapper().writeValue(new File(filePath), documents);
    }

    public List<ReviewDocument> deserializeDocuments(String filePath) throws IOException {
        return new JsonMapper().readValue(getClass().getClassLoader().getResource(filePath), new TypeReference<>() { });
    }
}
