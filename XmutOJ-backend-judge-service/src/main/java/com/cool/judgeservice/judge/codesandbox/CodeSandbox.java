package com.cool.judgeservice.judge.codesandbox;
import com.cool.backendmodel.model.codesandbox.ExecuteCodeRequest;
import com.cool.backendmodel.model.codesandbox.ExecuteCodeResponse;
/**
 * 代码沙箱接口定义
 */
public interface CodeSandbox {

    /**
     * 代码沙箱执行代码接口
     *
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
