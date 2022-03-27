package data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetacriticGame {

    private String name;

    private Double metacriticRating;

    private Double userRating;

    private List<MetacriticReview> criticReviews;

    @JsonProperty("criticReviews")
    public List<MetacriticReview> getCriticReviews() {
        return criticReviews;
    }

    @JsonProperty("critic_reviews")
    public void setCriticReviews(List<MetacriticReview> criticReviews) {
        this.criticReviews = criticReviews;
    }

    private List<MetacriticReview> userReviews;
}

