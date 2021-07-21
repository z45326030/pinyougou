package entity;

import java.io.Serializable;

/**
 * 返回结果实现类
 */

public class Result implements Serializable{
    private boolean success;

    private String message;

    public String getMessage() {
        return message;
    }

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {

        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
