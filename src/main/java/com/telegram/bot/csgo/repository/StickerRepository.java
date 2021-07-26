package com.telegram.bot.csgo.repository;

import com.telegram.bot.csgo.model.entity.Sticker;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StickerRepository extends CrudRepository<Sticker, Integer> {

    @Cacheable("stickers")
    List<Sticker> findAll();
}
