package cn.nihility.uim;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UimUserUpdatePassword {

    /*public static void main(String[] args) {
        String password = "";
        String syncId = "";
        String putQueryParam = "USER_ID=" + syncId;
        String URL = "http://10.0.41.80/ums/service/V1/res/users/";
        String ts = Long.toString(System.currentTimeMillis());
        String appKey = "KIAM";
        String signMethod = "SHA-256";
        String pwd = "14b919466d9cc0caaa8e6a83725977567eee5085";
        // 创建HttpClient对象
        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpPut httpPut = new HttpPut(URL);
        // 模拟POST/PUT的body中数据，需转为JSON进行签名。GET则没有这部分内容。
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("PWD", DigestUtils.sha256Hex(passwordNew.getBytes()));
        String bodyParam = new Gson().toJson(dataMap);
        String postAllParamUrl = URL + "&" + putQueryParam + "&bodyData=" + bodyParam;

        StringEntity bodyData = new StringEntity(bodyParam.toString(), "UTF-8");
        httpPut.setEntity(bodyData);
        // 对参数签名，并放入请求header中的signData参数中
        try {
            // 签名数据
            String signData = TokenUtil.getSignature(pwd, postAllParamUrl, signMethod);
            // 添加header参数 appCode、timestamp、 signatureOnce、signature
            httpPut.addHeader("appKey", appKey);
            httpPut.addHeader("ts", ts.toString());
            httpPut.addHeader("once", String.valueOf(request.getSession().getAttribute("uuid")));

            httpPut.addHeader("signData", signData);
            String urlStr = httpPut.getURI().toString();

            // 公共参数URL
            System.out.println("commonParamter:" + urlStr);

            if (StringUtils.endsWith(urlStr, "/")) {
                urlStr = StringUtils.removeEnd(urlStr, "/");
            }

            httpPut.setURI(new URI(urlStr));
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(3000)
                    .setConnectionRequestTimeout(3000).setSocketTimeout(3000).build();
            httpPut.setConfig(requestConfig);
            System.out.println("urlStr in request:" + httpPut.getURI().toString());

            // 执行请求
            CloseableHttpResponse response = httpclient.execute(httpPut);
            // 取响应的结果
            int statusCode = response.getStatusLine().getStatusCode();
            // 打印响应结果
            if (statusCode == HttpStatus.SC_OK) {
                String resp = EntityUtils.toString(response.getEntity(), "utf-8");
                System.out.println("status:" + statusCode);
                System.out.println("result:" + resp);
            }
            return ResultUtil.success("修改成功");
        } catch (URISyntaxException e) {
            //签名失败
            e.printStackTrace();
            logger.error(e.getMessage());
            return ResultUtil.error("修改失败");
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return ResultUtil.error("修改失败");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return ResultUtil.error("修改失败");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return ResultUtil.error("修改失败");
        }
    }*/

    public static void main1(String[] args) throws IOException {

        BasicCookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();

        String id = "1342018652626509824";
        HttpPut request = new HttpPut("http://10.0.41.80:50012/ums/service/V1/res/users/" + id);

        String appKey = "KIAM";
        String appPwd = "KIAM";
        String signMethod = "SHA-256";
        String ts = Long.toString(System.currentTimeMillis());
        String once = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        String bodyData = "";

        request.addHeader(UimRequestUtil.ONCE, once);
        request.addHeader(UimRequestUtil.TS, ts);
        request.addHeader(UimRequestUtil.APP_KEY, appKey);
        request.addHeader(UimRequestUtil.SIGN_METHOD, signMethod);

        StringBuilder signParams = new StringBuilder();
        signParams.append(String.format("appKey=%s&ts=%s&once=%s", appKey, ts, once));

        if (request.getMethod().equals("PUT") || request.getMethod().equals("POST")) {
            signParams.append("&bodyData=").append(bodyData).append("&signMethod=").append(signMethod);
            if (request.getMethod().equals("PUT")) {
                signParams.append("&ID=");
                signParams.append(id);
            }
        } else if (request.getMethod().equals("DELETE")) {
            signParams.append("&signMethod=").append(signMethod);
            signParams.append("&ID=");
            signParams.append(id);
        } else {
            signParams.append("&signMethod=").append(signMethod);
        }

        String signData = signatureData(appPwd, signParams.toString(), signMethod);
        request.addHeader(UimRequestUtil.SIGN_DATA, signData);

        CloseableHttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        System.out.println(EntityUtils.toString(entity));

        cookieStore.getCookies().forEach(ck -> System.out.println(ck.getName() + ":" + ck.getValue()));

        HttpGet httpGet = new HttpGet("http://localhost:8080/start/show");
        CloseableHttpResponse resp = httpClient.execute(httpGet);
        System.out.println(EntityUtils.toString(resp.getEntity()));

        httpClient.close();
        response.close();
        resp.close();


    }

    private static String signatureData(String appPwd, String toString, String signMethod) {
        return  null;
    }

    /*private static String signatureData(String appPwd, String signParam, String signMethod) {
        if (null != signParam && ) {
            try {
                signParam = URLDecoder.decode(signParam, "UTF-8");
            } catch (Exception e) {
                //不是urlencode编码的
            }
        }

        String[] paraArray = new String[]{};
        if (StringUtils.isNotBlank(paramUrl)) {
            String[] queryArray = paramUrl.split("&");
            paraArray = ArrayUtils.addAll(queryArray, paraArray);
        }

        Arrays.sort(paraArray);

        StringBuffer buffer = new StringBuffer();
        buffer.append(pwd);
        buffer.append(":");

        for (int i = 0; i < paraArray.length; i++) {
            buffer.append(paraArray[i]);
            buffer.append("&");
        }
        buffer.deleteCharAt(buffer.length() - 1);
        buffer.append(":");
        buffer.append(pwd);
        String encode = null;
        if ("SM3".equals(signMethod)){
            byte[] hash = SM3Util.hash(buffer.toString().getBytes(StandardCharsets.UTF_8));
            encode = Hex.toHexString(hash);
        }else {
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance(signMethod, "BC");
                md.update(buffer.toString().getBytes(StandardCharsets.UTF_8));
            } catch (NoSuchAlgorithmException e) {
                log.error("生成signData失败:", e);
                throw e;
            }
            encode = HexUtils.bytes2Hex(md.digest());
        }
        return encode;
    }*/


}
