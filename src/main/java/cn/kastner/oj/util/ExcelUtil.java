package cn.kastner.oj.util;

import cn.kastner.oj.exception.FileException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ExcelUtil {

  private static final String XLS = "xls";

  private static final String XLSX = "xlsx";

  public static Workbook getWorkbook(InputStream is, File file) throws IOException {
    if (file.getName().endsWith(XLS)) {
      return new HSSFWorkbook(is);
    } else if (file.getName().endsWith(XLSX)) {
      return new XSSFWorkbook(is);
    }
    return null;
  }

  public static void validExcel(File file) throws FileException {
    if (!file.exists()) {
      throw new FileException(FileException.FILE_NOT_EXIST);
    }
    if (!(file.isFile() && (file.getName().endsWith(XLS) || file.getName().endsWith(XLSX)))) {
      throw new FileException(FileException.NOT_EXCEL_FILE);
    }
  }
}
