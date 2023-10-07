package searchengine.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.indexing.IndexErrorResponse;
import searchengine.dto.indexing.IndexResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.index.IndexService;
import searchengine.services.index.IndexServiceImpl;
import searchengine.services.statistics.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final StatisticsService statisticsService;
    private final IndexService indexService;


    public ApiController(StatisticsService statisticsService, IndexService indexService) {
        this.statisticsService = statisticsService;
        this.indexService = indexService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexResponse> startIndexing() {
        if (!IndexServiceImpl.isIndexing) {
            IndexResponse indexResponse = indexService.getIndex();
            return ResponseEntity.ok(indexResponse);
        }
        IndexResponse errorResponse = indexService.getIndex();
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_ACCEPTABLE);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexResponse> stopIndexing() {
        if (IndexServiceImpl.isIndexing) {
            return ResponseEntity.ok(indexService.stopIndex());
        }
        IndexResponse errorResponse = indexService.stopIndex();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


}
