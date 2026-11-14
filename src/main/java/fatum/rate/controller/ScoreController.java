package fatum.rate.controller;

import fatum.rate.dto.response.ServiceScoreResponse;
import fatum.rate.dto.response.UserScoreResponse;
import fatum.rate.model.UserSnapshot;
import fatum.rate.service.ScoreService;
import fatum.rate.service.SnapshotService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/score")
public class ScoreController {

    private final SnapshotService snapshotService;
    private final ScoreService scoreService;

    public ScoreController(SnapshotService snapshotService, ScoreService scoreService) {

        this.snapshotService = snapshotService;
        this.scoreService = scoreService;
    }

    /** The average of a user: 3.00 provisional until the first real rating arrives. */
    @GetMapping("/user")
    public UserScoreResponse getUserScore(@RequestParam String userId) {
        UserSnapshot user = snapshotService.requireUser(userId.trim());
        return new UserScoreResponse(
                user.getUserId(), user.getName(), user.getRole(),
                user.average(), user.getRatingsCount(), user.isProvisional());
    }

    @GetMapping("/service")
    public ServiceScoreResponse getServiceScore(@RequestParam String serviceId) {
        return scoreService.serviceScore(serviceId);
    }
}
