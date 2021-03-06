package net.feedbacky.app.service.idea.subscribe;

import net.feedbacky.app.config.UserAuthenticationToken;
import net.feedbacky.app.data.idea.Idea;
import net.feedbacky.app.data.user.User;
import net.feedbacky.app.data.user.dto.FetchUserDto;
import net.feedbacky.app.exception.FeedbackyRestException;
import net.feedbacky.app.exception.types.InvalidAuthenticationException;
import net.feedbacky.app.exception.types.ResourceNotFoundException;
import net.feedbacky.app.repository.UserRepository;
import net.feedbacky.app.repository.idea.IdeaRepository;
import net.feedbacky.app.service.ServiceUser;
import net.feedbacky.app.util.RequestValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * @author Plajer
 * <p>
 * Created at 28.05.2020
 */
@Service
public class SubscriptionServiceImpl implements SubscriptionService {

  private final IdeaRepository ideaRepository;
  private final UserRepository userRepository;

  @Autowired
  public SubscriptionServiceImpl(IdeaRepository ideaRepository, UserRepository userRepository) {
    this.ideaRepository = ideaRepository;
    this.userRepository = userRepository;
  }

  @Override
  public FetchUserDto postSubscribe(long id) {
    UserAuthenticationToken auth = RequestValidator.getContextAuthentication();
    User user = userRepository.findByEmail(((ServiceUser) auth.getPrincipal()).getEmail())
            .orElseThrow(() -> new InvalidAuthenticationException("User session not found. Try again with new token"));
    Idea idea = ideaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Idea with id " + id + " does not exist."));
    if(idea.getSubscribers().contains(user)) {
      throw new FeedbackyRestException(HttpStatus.BAD_REQUEST, "Idea with id " + id + " is already subscribed by you.");
    }
    idea.getSubscribers().add(user);
    ideaRepository.save(idea);
    //no need to expose
    return user.convertToDto().exposeSensitiveData(false);
  }

  @Override
  public ResponseEntity deleteSubscribe(long id) {
    UserAuthenticationToken auth = RequestValidator.getContextAuthentication();
    User user = userRepository.findByEmail(((ServiceUser) auth.getPrincipal()).getEmail())
            .orElseThrow(() -> new InvalidAuthenticationException("User session not found. Try again with new token"));
    Idea idea = ideaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Idea with id " + id + " does not exist."));
    if(!idea.getSubscribers().contains(user)) {
      throw new FeedbackyRestException(HttpStatus.BAD_REQUEST, "Idea with id " + id + " is not subscribed by you.");
    }
    idea.getSubscribers().remove(user);
    ideaRepository.save(idea);
    //no need to expose
    return ResponseEntity.noContent().build();
  }
}
