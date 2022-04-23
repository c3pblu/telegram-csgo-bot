package com.telegram.bot.csgo.repository;

import com.telegram.bot.csgo.domain.Flag;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FlagRepo extends CrudRepository<Flag, Integer> {

    Optional<Flag> findByCode(String code);

    Optional<Flag> findByName(String name);
}
