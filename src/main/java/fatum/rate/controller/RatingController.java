package fatum.rate.controller;

import fatum.rate.dto.request.RatingRequest;
import fatum.rate.dto.response.RatingResponse;
import fatum.rate.model.constant.RatingTarget;
import fatum.rate.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rating")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    public ResponseEntity<RatingResponse> rate(@Valid @RequestBody RatingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingService.register(request));
    }

    @GetMapping("/{id}")
    public RatingResponse getById(@PathVariable String id) {
        return ratingService.findById(id.trim());
    }

    @GetMapping("/order")
    public List<RatingResponse> getByOrder(@RequestParam String orderId) {
        return ratingService.findByOrder(orderId);
    }

    @GetMapping("/target")
    public List<RatingResponse> getByTarget(
            @RequestParam RatingTarget target,
            @RequestParam String targetId
    ) {
        return ratingService.findByTarget(target, targetId);
    }
}
