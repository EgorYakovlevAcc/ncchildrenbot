package com.ncquizbot.ncbot.repo;

import com.ncquizbot.ncbot.model.ImageForReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageForReactionRepository extends JpaRepository<ImageForReaction, Integer> {
    ImageForReaction findImageForReactionById(Integer id);

    List<ImageForReaction> findImageForReactionsByType(String s);
}
