package ai.yunxi.common.exception;

public class CommonException extends RuntimeException {
    private String msg;

    public CommonException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
