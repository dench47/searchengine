package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.indexing.IndexErrorResponse;
import searchengine.dto.indexing.IndexResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.WebSiteRepository;
import searchengine.services.IndexService;
import searchengine.services.StatisticsService;

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

        return ResponseEntity.ok(indexService.getIndex());
    }


}
