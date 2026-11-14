package fatum.rate.controller;

import fatum.rate.dto.response.UserScoreResponse;
import fatum.rate.model.UserSnapshot;
import fatum.rate.service.SnapshotService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/score")
public class ScoreController {

    private final SnapshotService snapshotService;

    public ScoreController(SnapshotService snapshotService) {
        this.snapshotService = snapshotService;
    }

    /** The average of a user: 3.00 provisional until the first real rating arrives. */
    @GetMapping("/user")
    public UserScoreResponse getUserScore(@RequestParam String userId) {
        UserSnapshot user = snapshotService.requireUser(userId.trim());
        return new UserScoreResponse(
                user.getUserId(), user.getName(), user.getRole(),
                user.average(), user.getRatingsCount(), user.isProvisional());
    }
}
