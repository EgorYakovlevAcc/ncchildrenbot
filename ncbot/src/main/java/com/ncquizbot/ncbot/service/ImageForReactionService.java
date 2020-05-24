package com.ncquizbot.ncbot.service;

import com.ncquizbot.ncbot.model.ImageForReaction;

public interface ImageForReactionService {
    void save(ImageForReaction imageForReaction);
    void delete(ImageForReaction imageForReaction);

    byte[] getImageByReaction(String s);
}
