package searchengine.dto.indexing;

import lombok.Data;

@Data
public class StopIndexingErrorResponse extends IndexResponse{
    private final String error = "Индексация не запущена";
}
