package com.suimz.open.chatgptweb.java.core.exception;

import com.suimz.open.chatgptweb.java.bean.resp.R;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handle Exceptions
 *
 * @author https://github.com/suimz
 */
@RestControllerAdvice
public class AdviceException {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<R> map(BizException e) {
        return ResponseEntity.status(e.getHttpStatus()).body(R.error(e.getMessage()));
    }

    @ExceptionHandler(UnauthorizedBizException.class)
    public R map(UnauthorizedBizException e) {
        return R.builder()
                .status("Unauthorized")
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R map(HttpMessageNotReadableException e) {
        return R.error("bad request");
    }

    @ExceptionHandler(BindException.class)
    public R map(BindException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        Map<String, Object> error = this.getValidError(fieldErrors);
        return R.error(error.get("errorMsg").toString());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R map(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        Map<String, Object> error = this.getValidError(fieldErrors);
        return R.error(error.get("errorMsg").toString());
    }

    private Map<String, Object> getValidError(List<FieldError> fieldErrors) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        List<String> errorList = new ArrayList<String>();
        StringBuffer errorMsg = new StringBuffer();
        for (FieldError error : fieldErrors) {
            errorList.add(error.getDefaultMessage());
            errorMsg.append(error.getDefaultMessage());
            // first
            break;
        }
        map.put("errorList", errorList);
        map.put("errorMsg", errorMsg);
        return map;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R map(MethodArgumentTypeMismatchException e) {
        return R.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public R map(Exception e) {
        return R.error();
    }

}
