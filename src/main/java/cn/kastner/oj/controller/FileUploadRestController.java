package cn.kastner.oj.controller;

import cn.kastner.oj.exception.FileException;
import cn.kastner.oj.service.FileUploadService;
import cn.kastner.oj.util.NetResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
public class FileUploadRestController {

  private final FileUploadService fileUploadService;

  @Autowired
  public FileUploadRestController(FileUploadService fileUploadService) {
    this.fileUploadService = fileUploadService;
  }

  @RequestMapping(value = "/api/v1/upload")
  public NetResult upload(@RequestBody MultipartFile file, HttpServletRequest request)
      throws FileException, IOException {
    NetResult netResult = new NetResult();
    netResult.data = fileUploadService.uploadFile(file, request);
    netResult.code = 200;
    netResult.message = "";
    return netResult;
  }

  @RequestMapping(value = "/api/v1/save")
  public NetResult save(@RequestParam String path, @RequestParam String prefix) throws IOException {
    NetResult netResult = new NetResult();
    netResult.data = fileUploadService.saveFile(path, prefix);
    netResult.code = 200;
    netResult.message = "";
    return netResult;
  }
}
