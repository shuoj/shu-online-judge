package cn.kastner.oj.util;

import cn.kastner.oj.constant.CommonConstant;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HttpUtil {

  private final OkHttpClient client;
    private final String token;

  @Autowired
  public HttpUtil(OkHttpClient client, @Value("${judger.token}") String token) {
    this.client = client;
      this.token = token;
  }

  public String post(String url, String json) throws IOException {
    RequestBody body = RequestBody.create(CommonConstant.JSON, json);
      Request request = new Request
              .Builder()
              .header("Content-Type", "application/json")
              .header("X-Judge-Server-Token", token)
              .url(url)
              .post(body)
              .build();
    try (Response response = client.newCall(request).execute()) {
      return response.body().string();
    }
  }
}
