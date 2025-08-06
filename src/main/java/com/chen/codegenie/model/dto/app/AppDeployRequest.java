package com.chen.codegenie.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * @author chen
 */
@Data
public class AppDeployRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long appId;

    private static final long serialVersionUID = 1L;
}
