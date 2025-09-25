package com.dpsk.dpsk_quiz_sys_java.repository;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.Conversation;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConversationRepository extends CrudRepository<Conversation, Integer> {
    List<Conversation> findByUserId(Long userId);

    @Transactional
    @Modifying
    @Query("update Conversation set title = :newTitle where conversationId = :conversationId")
    void updateTitle(@Param("newTitle") String newTitle, @Param("conversationId") Integer id);
}
