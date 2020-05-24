package com.ncquizbot.ncbot.service.impl;

import com.ncquizbot.ncbot.model.ImageForReaction;
import com.ncquizbot.ncbot.repo.ImageForReactionRepository;
import com.ncquizbot.ncbot.service.ImageForReactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImageForReactionServiceImpl implements ImageForReactionService {
    @Autowired
    private ImageForReactionRepository imageForReactionRepository;

    @Override
    public void save(ImageForReaction imageForReaction) {
        imageForReactionRepository.save(imageForReaction);
    }

    @Override
    public void delete(ImageForReaction imageForReaction) {
        imageForReactionRepository.delete(imageForReaction);
    }

    @Override
    public byte[] getImageByReaction(String s) {
        ImageForReaction imageForReaction = imageForReactionRepository.findImageForReactionsByType(s).stream()
                .findFirst()
                .orElse(null);
        return imageForReaction.getImage();
    }
}
