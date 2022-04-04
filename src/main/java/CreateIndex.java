import api.ReviewApiClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import static java.lang.System.exit;

public class CreateIndex {

    public static final Logger LOGGER = Logger.getLogger(CreateIndex.class.getName());

    public static void main(String[] args) throws IOException {
        var client = new ReviewApiClient();
        try {
            client.createMetacriticIndex();
            exit(0);
        }
        catch (ElasticsearchException|URISyntaxException ex) {
            LOGGER.info("Index already exists");
        }
    }
}
