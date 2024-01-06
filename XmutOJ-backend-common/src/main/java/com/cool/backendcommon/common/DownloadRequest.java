package com.cool.backendcommon.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 下载请求
 *
 */
@Data
public class DownloadRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private List<Long> ids;

    private static final long serialVersionUID = 1L;
}