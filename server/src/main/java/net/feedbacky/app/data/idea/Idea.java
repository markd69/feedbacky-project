package net.feedbacky.app.data.idea;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.feedbacky.app.data.board.Board;
import net.feedbacky.app.data.idea.attachment.Attachment;
import net.feedbacky.app.data.idea.comment.Comment;
import net.feedbacky.app.data.idea.dto.FetchIdeaDto;
import net.feedbacky.app.data.tag.Tag;
import net.feedbacky.app.data.tag.dto.FetchTagDto;
import net.feedbacky.app.data.user.User;

import org.hibernate.annotations.CreationTimestamp;
import org.modelmapper.ModelMapper;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Plajer
 * <p>
 * Created at 30.09.2019
 */
@Entity
@Table(name = "ideas")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Idea implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  private Board board;
  private String title;
  @Column(name = "description", columnDefinition = "text", length = 65_535)
  private String description;
  @ManyToOne
  private User creator;
  @ManyToMany(fetch = FetchType.LAZY)
  private Set<User> voters = new HashSet<>();
  //always the same as voters.size()
  private int votersAmount;
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "idea")
  private Set<Comment> comments = new HashSet<>();
  @ManyToMany(fetch = FetchType.LAZY)
  private Set<Tag> tags = new HashSet<>();
  @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "idea")
  private Set<Attachment> attachments = new HashSet<>();
  private IdeaStatus status;
  @CreationTimestamp
  private Date creationDate;
  private boolean edited;

  public void setVoters(Set<User> voters) {
    this.voters = voters;
    this.votersAmount = voters.size();
  }

  public FetchIdeaDto convertToDto(boolean voted) {
    FetchIdeaDto dto = new ModelMapper().map(this, FetchIdeaDto.class);
    dto.setOpen(status == IdeaStatus.OPENED);
    Set<FetchTagDto> tagDtos = new HashSet<>();
    for(Tag tag : tags) {
      tagDtos.add(tag.convertToDto());
    }
    dto.setAttachments(attachments.stream().map(Attachment::convertToDto).collect(Collectors.toList()));
    dto.setBoardDiscriminator(board.getDiscriminator());
    dto.setVotersAmount(voters.size());
    dto.setCommentsAmount(comments.stream().filter(comment -> !comment.isSpecial()).count());
    dto.setTags(tagDtos);
    dto.setUpvoted(voted);
    dto.setUser(creator.convertToDto().exposeSensitiveData(false).convertToSimpleDto());
    return dto;
  }

  public enum IdeaStatus {
    OPENED(true), CLOSED(false);

    private boolean value;

    IdeaStatus(boolean value) {
      this.value = value;
    }

    public static IdeaStatus toIdeaStatus(boolean state) {
      if(state) {
        return OPENED;
      }
      return CLOSED;
    }

    public boolean getValue() {
      return value;
    }

  }

}