package net.feedbacky.app.service.board.roadmap;

import net.feedbacky.app.data.roadmap.FetchRoadmapElement;
import net.feedbacky.app.service.FeedbackyService;

import java.util.List;

/**
 * @author Plajer
 * <p>
 * Created at 28.05.2020
 */
public interface RoadmapService extends FeedbackyService {

  List<FetchRoadmapElement> getAll(String discriminator, int page, int pageSize);

}
