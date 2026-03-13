package com.goAbroad.core.community.repository;

import com.goAbroad.core.community.entity.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    @Query("SELECT i FROM Interaction i WHERE i.userId = :userId AND i.targetId = :targetId " +
            "AND i.targetType = :targetType AND i.action = :action")
    Optional<Interaction> findOne(@Param("userId") Long userId,
                                  @Param("targetId") Long targetId,
                                  @Param("targetType") Interaction.TargetType targetType,
                                  @Param("action") Interaction.Action action);

    @Query("SELECT i FROM Interaction i WHERE i.userId = :userId AND i.targetId = :targetId " +
            "AND i.targetType = :targetType")
    List<Interaction> findByTarget(@Param("userId") Long userId,
                                   @Param("targetId") Long targetId,
                                   @Param("targetType") Interaction.TargetType targetType);

    void deleteByUserIdAndTargetIdAndTargetTypeAndAction(Long userId, Long targetId,
                                                         Interaction.TargetType targetType,
                                                         Interaction.Action action);
}
