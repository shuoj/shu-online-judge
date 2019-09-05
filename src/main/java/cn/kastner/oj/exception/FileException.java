package cn.kastner.oj.exception;

import org.springframework.http.HttpStatus;

public class FileException extends AppException {

  public static final String EMPTY_FILE = "文件内容为空"; // code -2

  public static final String EMPTY_ZIP_FILE = "压缩文件内容为空"; // code -3

  public static final String FILE_NOT_EXIST = "文件不存在"; // code -4

  public static final String NOT_EXCEL_FILE = "不是有效 Excel 文件";

  public static final String EXCEL_FORMAT_ERROR = "Excel 文件内容格式错误";

  public FileException(String message) {
    super(message);
    switch (message) {
      case EMPTY_FILE:
        this.code = -2;
        this.status = HttpStatus.UNPROCESSABLE_ENTITY;
        break;
      case EMPTY_ZIP_FILE:
        this.code = -3;
        this.status = HttpStatus.UNPROCESSABLE_ENTITY;
        break;
      case FILE_NOT_EXIST:
        this.code = -4;
        this.status = HttpStatus.UNPROCESSABLE_ENTITY;
        break;
      case NOT_EXCEL_FILE:
        this.code = -5;
        this.status = HttpStatus.UNPROCESSABLE_ENTITY;
        break;
      case EXCEL_FORMAT_ERROR:
        this.code = -6;
        this.status = HttpStatus.UNPROCESSABLE_ENTITY;
        break;
      default:
        this.code = -1;
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        break;
    }
  }

  @Override
  public Integer getCode() {
    return this.code;
  }

  @Override
  public HttpStatus getStatus() {
    return status;
  }
}
