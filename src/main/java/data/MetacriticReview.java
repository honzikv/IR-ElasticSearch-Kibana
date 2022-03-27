package data;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MetacriticReview {

    private String reviewerName;

    private Date dateReviewed;

    private Double score;

    private String text;

}
