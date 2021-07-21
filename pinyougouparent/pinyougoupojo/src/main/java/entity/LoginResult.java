package entity;

/**
 * 包含登录名的结果对象
 */
public class LoginResult {

    private boolean success;
    private String loginname;//登录名
    private Object data;//数据

    public LoginResult(boolean success, String loginname, Object data) {
        this.success = success;
        this.loginname = loginname;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getLoginname() {
        return loginname;
    }

    public void setLoginname(String loginname) {
        this.loginname = loginname;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
