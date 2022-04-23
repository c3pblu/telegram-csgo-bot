package com.telegram.bot.csgo.service;

import java.util.List;
import com.telegram.bot.csgo.domain.Sticker;
import com.telegram.bot.csgo.repository.StickerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StickerService {

    private final StickerRepo stickerRepo;

    @Cacheable("stickers")
    public List<Sticker> getAllStickers() {
        return stickerRepo.findAll();
    }
}
