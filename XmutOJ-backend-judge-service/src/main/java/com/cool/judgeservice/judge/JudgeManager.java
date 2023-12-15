package com.cool.judgeservice.judge;


import com.cool.backendmodel.model.codesandbox.JudgeInfo;
import com.cool.backendmodel.model.entity.QuestionSubmit;
import com.cool.judgeservice.judge.strategy.DefaultJudgeStrategy;
import com.cool.judgeservice.judge.strategy.JavaLanguageJudgeStrategy;
import com.cool.judgeservice.judge.strategy.JudgeContext;
import com.cool.judgeservice.judge.strategy.JudgeStrategy;
import org.springframework.stereotype.Service;

/**
 * 判题管理（简化调用）
 */
@Service
public class JudgeManager {

    /**
     * 执行判题
     *
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext) {
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getSubmitLanguage();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if ("java".equals(language)) {
            judgeStrategy = new JavaLanguageJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);
    }
}