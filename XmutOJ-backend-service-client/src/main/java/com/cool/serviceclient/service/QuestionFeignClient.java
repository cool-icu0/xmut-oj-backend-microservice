package com.cool.serviceclient.service;

import com.cool.backendmodel.model.entity.Question;
import com.cool.backendmodel.model.entity.QuestionSubmit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
* @author Cool
* @description 针对表【question(题目)】的数据库操作Service
* @createDate 2023-11-30 14:56:38
*/
@FeignClient(name = "XmutOJ-backend-question-service", path = "/api/question/inner")
public interface QuestionFeignClient{
    @GetMapping("/get/id")
    Question getQuestionById(@RequestParam("questionId") long questionId);

    @GetMapping("/question_submit/get/id")
    QuestionSubmit getQuestionSubmitById(@RequestParam("questionSubmitId") long questionSubmitId);

    @PostMapping("/question_submit/update")
    boolean updateQuestionSubmitById(@RequestBody QuestionSubmit questionSubmit);
}
