package com.deliveranything.domain.reviews.repository;

import com.deliveranything.domain.reviews.entity.Review;
import com.deliveranything.domain.reviews.entity.ReviewPhoto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewPhotoRepository extends JpaRepository<ReviewPhoto, Long> {

  List<ReviewPhoto> findAllByReview(Review review);
}
