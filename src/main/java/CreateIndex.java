import api.ReviewApiClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import java.io.IOException;
import java.util.logging.Logger;

public class CreateIndex {

    public static final Logger LOGGER = Logger.getLogger(CreateIndex.class.getName());

    public static void main(String[] args) throws IOException {
        var client = new ReviewApiClient();
        try {
            client.createMetacriticIndex();
        }
        catch (ElasticsearchException ex) {
            LOGGER.info("Index already exists");
        }
    }
}
