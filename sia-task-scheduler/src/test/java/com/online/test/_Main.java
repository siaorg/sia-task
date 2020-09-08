package com.online.test;

import com.sia.task.core.http.RestTemplateFactroy;
import com.sia.task.core.http.SiaHttpResponse;
import com.sia.task.core.util.Constant;
import com.sia.task.core.util.JsonHelper;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class _Main {


    public static void main(String[] args) throws InterruptedException {

          http();

       // response();

    }

    private static void response() {
        Map<String, String> info = new HashMap<String, String>();
        info.put("status", "failure");
        info.put("status", "success");
        info.put("result", "disclosurePlanJobAmtTask success");

        String s = JsonHelper.toString(info);

        SiaHttpResponse response = JsonHelper.toObject(s, SiaHttpResponse.class);
        System.out.println(response);
        System.out.println(response.getStatus());
        System.out.println(response.getStatus().getStatus());
    }

    private static void http() throws InterruptedException {
        AsyncRestTemplate asyncRestTemplate = null;
        String url = null;
        try {
            asyncRestTemplate = RestTemplateFactroy.getAsyncRestTemplate(30 * 1000);
            //url = "http://10.143.131.132:8080/zma-product/product/disclosurePlanJobAmtTask";
            url = "http://10.10.168.129:12129/v5/product/disclosurePlanJobAmtTask";
        } catch (Exception e) {
            e.printStackTrace();
        }
        asyncRestTemplate.postForEntity(url, paramWapper(), SiaHttpResponse.class).addCallback(new ListenableFutureCallback<ResponseEntity<SiaHttpResponse>>() {
            @Override
            public void onFailure(Throwable ex) {
                System.out.println(ex);
            }

            @Override
            public void onSuccess(ResponseEntity<SiaHttpResponse> result) {
                System.out.println(Constant.LOG_PREFIX + ">>->>->>->>->> onSuccess [{}] <<-<<-<<-<<-<<");
                SiaHttpResponse response = result.getBody();
                System.out.println(response);
            }
        });

        for (; ; ) {
            TimeUnit.SECONDS.sleep(300);
        }
    }

    /**
     * headers ： ByteBuffer
     * CRC(4B):CRC码
     * VERSION(1B):版本
     * TIMESTAMP(8B):时间戳
     * REQUEST_ID(8B):请求ID
     * VALUE:
     * requestId : 标识唯一请求
     * @return
     */
    private static HttpEntity<Object> paramWapper() {

        HttpEntity<Object> httpEntity;
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
//        headers.add();
        httpEntity = new HttpEntity<>("test hello", headers);
        return httpEntity;
    }
}
