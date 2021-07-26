package com.telegram.bot.csgo.repository;

import com.telegram.bot.csgo.model.entity.Flag;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FlagRepository extends CrudRepository<Flag, String> {

    Optional<Flag> findByCode(String code);

    Optional<Flag> findByName(String name);
}
