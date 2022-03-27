package usecases;

import data.DocumentSerialization;
import java.io.IOException;

public class CreateDocumentsFromPreprocessedJson {

    public static void main(String[] args) throws IOException {
        var dataDeserializer = new DocumentSerialization();
        var documents = dataDeserializer.deserializePreprocessedData("preprocessed.json");
        dataDeserializer.serializeDocuments(documents, "documents.json");
    }
}
