package cn.kastner.oj.util;

import cn.kastner.oj.domain.User;
import cn.kastner.oj.domain.security.Authority;
import cn.kastner.oj.domain.security.AuthorityName;
import cn.kastner.oj.exception.CommonException;
import cn.kastner.oj.exception.FileException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CommonUtil {

  public static final String allChar =
      "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

  public static boolean isNull(String str) {
    return str == null || "".equals(str);
  }

  public static boolean isAdmin(User user) {
    List<Authority> authorityList = user.getAuthorities();
    for (Authority authority : authorityList) {
      if (AuthorityName.ROLE_ADMIN.equals(authority.getName())) {
        return true;
      }
    }
    return false;
  }

  public static List<File> unzip(String path) throws FileException {
    String destDirectoryPath = path.substring(0, path.lastIndexOf(File.separator));
    File destDirectory = new File(destDirectoryPath);
    List<File> fileList = new ArrayList<>();
    byte[] buffer = new byte[1024];

    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(path))) {
      ZipEntry zipEntry = zis.getNextEntry();
      if (zipEntry == null) {
        throw new FileException(FileException.EMPTY_ZIP_FILE);
      }
      while (zipEntry != null) {
        fileList.add(new File(destDirectoryPath + File.separator + zipEntry.getName()));
        File unzippedFile = newFile(destDirectory, zipEntry);
        try (FileOutputStream fos = new FileOutputStream(unzippedFile)) {
          int len;
          while ((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
          }
        }
        zipEntry = zis.getNextEntry();
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new FileException(CommonException.COMMON_EXCEPTION);
    }
    File file = new File(path);
    file.delete();
    return fileList;
  }

  private static File newFile(File destDirectory, ZipEntry zipEntry) throws IOException {
    File destFile = new File(destDirectory, zipEntry.getName());

    String destDirectoryPath = destDirectory.getCanonicalPath();
    String destFilePath = destFile.getCanonicalPath();

    if (!destFilePath.startsWith(destDirectoryPath + File.separator)) {
      throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
    }
    return destFile;
  }

  public static String md5(byte[] byteArray) throws NoSuchAlgorithmException {
    MessageDigest md5 = MessageDigest.getInstance("MD5");
    byte[] encryption = md5.digest(byteArray);
    StringBuffer strBuf = new StringBuffer();
    for (int i = 0; i < encryption.length; i++) {
      if (Integer.toHexString(0xff & encryption[i]).length() == 1) {
        strBuf.append("0").append(Integer.toHexString(0xff & encryption[i]));
      } else {
        strBuf.append(Integer.toHexString(0xff & encryption[i]));
      }
    }
    return strBuf.toString();
  }

  public static String md5(String string) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("MD5");
    md.update(string.getBytes());
    return new BigInteger(1, md.digest()).toString(16);
  }

  public static String generateStr(int len) {
    StringBuffer sb = new StringBuffer();
    Random random = new Random();
    for (int i = 0; i < len; i++) {
      sb.append(allChar.charAt(random.nextInt(allChar.length())));
    }
    return sb.toString();
  }
}
