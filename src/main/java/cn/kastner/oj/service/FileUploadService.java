package cn.kastner.oj.service;

import cn.kastner.oj.exception.FileException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public interface FileUploadService {

  String uploadFile(MultipartFile file, HttpServletRequest request)
      throws FileException, IOException;

  /**
   * @param tempPath          临时存储路径
   * @param relativeDirectory 相对存储目录
   * @return relativePath 相对存储路径
   * @throws IOException
   */
  String saveFile(String tempPath, String relativeDirectory) throws IOException;
}
