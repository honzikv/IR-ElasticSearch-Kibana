package data;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReviewDocument {

    private long id; // this will suffice for now

    private String gameName;

    private String reviewerName;

    private Date dateReviewed;

    private Double score;

    private String text;

    private boolean isCriticReview;
}
