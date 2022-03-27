package api;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import java.util.List;

public interface IApiClient<T> {

    void indexDocument(T document);

    void indexDocuments(List<T> documents);

    void deleteDocument(long id);

    void updateDocument(T document);

    T getDocument(long id);

    List<T> queryDocuments(Query query);
}
