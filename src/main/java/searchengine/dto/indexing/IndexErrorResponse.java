package searchengine.dto.indexing;

import lombok.Data;

@Data
public class IndexErrorResponse extends IndexResponse {
    private final String error = "Индексация уже запущена";


}
