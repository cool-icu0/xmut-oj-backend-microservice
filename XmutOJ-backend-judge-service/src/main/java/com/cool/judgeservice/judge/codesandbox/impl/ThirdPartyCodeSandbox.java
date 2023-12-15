package com.cool.judgeservice.judge.codesandbox.impl;


import com.cool.judgeservice.judge.codesandbox.CodeSandbox;
import com.cool.backendmodel.model.codesandbox.ExecuteCodeRequest;
import com.cool.backendmodel.model.codesandbox.ExecuteCodeResponse;

/**
 * 第三方代码沙箱（调用网上现成的代码沙箱）
 */
public class ThirdPartyCodeSandbox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("第三方代码沙箱");
        return null;
    }
}
