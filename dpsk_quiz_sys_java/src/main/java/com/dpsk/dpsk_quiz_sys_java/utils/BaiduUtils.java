package com.dpsk.dpsk_quiz_sys_java.utils;


import okhttp3.*;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;
import java.io.*;


public class BaiduUtils {
    public static final String API_KEY = "6gLVAhZrWwKQbwbWrBhNYPTj";
    public static final String SECRET_KEY = "V7FPoCOhXkU4H36v2hDged30Wwyv1OiO";

    public static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().readTimeout(300, TimeUnit.SECONDS).build();

    public static String getImgResponse(String imgBase64) throws IOException{
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        String bodystr ="image=" + imgBase64 + "&detect_direction=false&paragraph=false&probability=false&multidirectional_recognize=false";
        RequestBody body = RequestBody.create(mediaType, bodystr);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic?access_token=" + getAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        return  response.body().string();
    }

    public static String getPdfResponse(String imgBase64) throws IOException{
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        String bodystr ="pdf_file=" + imgBase64 + "&detect_direction=false&paragraph=false&probability=false&multidirectional_recognize=false";
        RequestBody body = RequestBody.create(mediaType, bodystr);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic?access_token=" + getAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        return  response.body().string();
    }


    /**
     * 从用户的AK，SK生成鉴权签名（Access Token）
     *
     * @return 鉴权签名（Access Token）
     * @throws IOException IO异常
     */
    public static String getAccessToken() {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + API_KEY
                + "&client_secret=" + SECRET_KEY);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = null;
        try {
            response = HTTP_CLIENT.newCall(request).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            return new JSONObject(response.body().string()).getString("access_token");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //将字符串中的/=+，分别转换为为%2F，%3D，%2B
    public static String replaceSpecialCharacters(String str) {
        // 使用StringBuilder来构建新的字符串
        StringBuilder sb = new StringBuilder();
        // 遍历字符串的每个字符
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '/':
                    sb.append("%2F");
                    break;
                case '=':
                    sb.append("%3D");
                    break;
                case '+':
                    sb.append("%2B");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
