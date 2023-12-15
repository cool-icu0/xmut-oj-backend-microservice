package com.cool.judgeservice.judge;

import cn.hutool.json.JSONUtil;
import com.cool.backendcommon.common.ErrorCode;
import com.cool.backendcommon.exception.BusinessException;
import com.cool.backendmodel.model.codesandbox.ExecuteCodeRequest;
import com.cool.backendmodel.model.codesandbox.ExecuteCodeResponse;
import com.cool.backendmodel.model.codesandbox.JudgeInfo;
import com.cool.backendmodel.model.dto.question.JudgeCase;
import com.cool.backendmodel.model.entity.Question;
import com.cool.backendmodel.model.entity.QuestionSubmit;
import com.cool.backendmodel.model.enums.QuestionSubmitStatusEnum;
import com.cool.judgeservice.judge.codesandbox.CodeSandBoxProxy;
import com.cool.judgeservice.judge.codesandbox.CodeSandbox;
import com.cool.judgeservice.judge.codesandbox.CodeSandboxFactory;
import com.cool.judgeservice.judge.strategy.JudgeContext;
import com.cool.questionservice.service.QuestionService;
import com.cool.questionservice.service.QuestionSubmitService;
import com.cool.serviceclient.service.QuestionFeignClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 判题服务实现类
 */
@Service
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private QuestionFeignClient questionFeignClient;

    @Resource
    private QuestionService questionService;

    @Resource
    private JudgeManager judgeManager;

    @Value("${codesandbox.type:example}")
    private String judgeType;

    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        // 1、传入题目的提交 id，获取到对应的题目、提交信息（包含代码、编程语言等）
        QuestionSubmit questionSubmit = questionFeignClient.getQuestionSubmitById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }
        // 通过提交的信息中的题目id 获取到题目的全部信息
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionFeignClient.getQuestionById(questionId);
        if (questionId == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        // 2、如果题目提交状态不为等待中，就不用重复执行了
        if (!questionSubmit.getSubmitState().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中");
        }
        // 3、更改判题（题目提交）的状态为 “判题中”，防止重复执行，也能让用户即时看到状态
        QuestionSubmit updateQuestionSubmit = new QuestionSubmit();
        updateQuestionSubmit.setId(questionSubmitId);
        updateQuestionSubmit.setSubmitState(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean updateState = questionFeignClient.updateQuestionSubmitById(updateQuestionSubmit);
        if (!updateState) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新失败");
        }
        //4、调用沙箱，获取到执行结果
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(judgeType);
        codeSandbox = new CodeSandBoxProxy(codeSandbox);
        String submitLanguage = questionSubmit.getSubmitLanguage();
        String submitCode = questionSubmit.getSubmitCode();
        // 获取输入用例
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCasesList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        // 通过Lambda表达式获取到每个题目的输入用例
        List<String> inputList = judgeCasesList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        // 调用沙箱
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(submitCode)
                .language(submitLanguage)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        List<String> outputList = executeCodeResponse.getOutputList();
        // 5、根据沙箱的执行结果，设置题目的判题状态和信息
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(outputList);
        judgeContext.setJudgeCaseList(judgeCasesList);
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);
        // 进入到代码沙箱，执行程序，返回执行结果
        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);
        // 6、修改判题结果
        updateQuestionSubmit = new QuestionSubmit();
        updateQuestionSubmit.setId(questionSubmitId);
        updateQuestionSubmit.setSubmitState(QuestionSubmitStatusEnum.SUCCEED.getValue());
        updateQuestionSubmit.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        updateState = questionFeignClient.updateQuestionSubmitById(updateQuestionSubmit);
        //判完题目进行数据增加（通过率）
        System.out.println("test01:"+updateQuestionSubmit);
        //提交数+1
        // todo 1 增加一个判断
        if (question.getSubmitNum() == null ){
            question.setSubmitNum(1);
        } else {
            question.setSubmitNum(question.getSubmitNum() +1);
        }
        // 如果通过了，则通过数+1
        // 创建 Gson 对象
        Gson gson = new Gson();
        // 将 JSON 字符串解析为 JsonObject 对象
        JsonObject jsonObject = gson.fromJson(updateQuestionSubmit.getJudgeInfo(), JsonObject.class);
        // 获取 message 字段的值
        String message = jsonObject.get("message").getAsString();
        // 打印获取到的 message 值
//        System.out.println("message的值为：" + message);
        if (message.equals("成功")){
            if (question.getAcceptedNum()==null){
                question.setAcceptedNum(1);
            }else {
                question.setAcceptedNum(question.getAcceptedNum() + 1);
            }
        }
        //进行题目更新操作
        questionService.updateById(question);
        System.out.println("test12:"+question.toString());
        if (!updateState) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新失败");
        }
        // 再次查询数据库，返回最新提交信息
        QuestionSubmit questionSubmitResult = questionSubmitService.getById(questionId);
        return questionSubmitResult;
    }
}
