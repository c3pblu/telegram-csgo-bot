package com.telegram.bot.csgo.repository;

import java.util.List;
import com.telegram.bot.csgo.domain.Sticker;
import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;

public interface StickerRepo extends CrudRepository<Sticker, Integer> {

    @NonNull
    List<Sticker> findAll();
}
