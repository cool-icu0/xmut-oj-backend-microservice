package com.cool.judgeservice.judge;


import com.cool.backendmodel.model.entity.QuestionSubmit;

/**
 * 判题服务 ：执行代码
 */
public interface JudgeService {
    /**
     * 判题
     *
     * @param questionSubmitId
     * @return
     */
    QuestionSubmit doJudge(long questionSubmitId);
}
