package com.dpsk.dpsk_quiz_sys_java.controller;



import com.dpsk.dpsk_quiz_sys_java.pojo.dto.ChartResponse;
import com.dpsk.dpsk_quiz_sys_java.service.ChartService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chart")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5175", "http://localhost:3000"}, allowCredentials = "true", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class ChartController {
    private final ChartService chartService;

    public ChartController(ChartService chartService) {
        this.chartService = chartService;
    }

    @GetMapping
    public ChartResponse getChartData() {
        return chartService.calculateChartData();
    }
}