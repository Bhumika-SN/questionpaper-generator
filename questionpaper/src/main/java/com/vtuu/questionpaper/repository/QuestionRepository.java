package com.vtuu.questionpaper.repository;

import com.vtuu.questionpaper.model.Question;  // ✅ Make sure this import exists
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    // ✅ Custom query methods
    List<Question> findByModule(String module);

    List<Question> findByModuleAndPart(String module, String part);
}