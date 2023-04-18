package com.suimz.open.chatgptweb.java.bean.req;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request Params - verify
 *
 * @author https://github.com/suimz
 */
@Data
public class AuthVerifyReq {

    @NotBlank(message = "Secret key is empty")
    private String token;

}
