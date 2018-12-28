package com.melodigm.post.protocol.network;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.network.ssl.SFSSLSocketFactory;
import com.melodigm.post.protocol.storage.ITransformBytesAdaptor;
import com.melodigm.post.protocol.storage.Storage;
import com.melodigm.post.protocol.storage.StorageException;
import com.melodigm.post.util.KeyUtil;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.util.TimeUtil;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class URLRequestHandler implements Runnable {
    private static final String CRLF = "\r\n";
    private static final String BOUNDARY_PREFIX = "--";
    private static final String _BOUNDARY = "*************com.melodigm*************";
    private static URLRequestHandler urlRequestHandler;

    private ArrayList<Request> queue = new ArrayList<Request>();
    private boolean 	isRunning;
    private boolean 	isPause;
    Storage cacheStorage;
    Storage 			externalStorage;
    Thread				downloadThread;
    int					connectionTimeout;
    String				tempFileName = "tmp.data";
    SchemeRegistry		schemeRegistry;
    public Context 	mContext;

    URLRequestHandler() {
        cacheStorage = new Storage();
        externalStorage = new Storage();
        isRunning = false;
        isPause = false;
        connectionTimeout = 30 * 1000;
        System.setProperty("http.keepAlive", "false");
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            TrustManager[] tms = { new TempTrustManager() };
            sc.init(null, tms, new SecureRandom());
            SSLSocketFactory ssf = sc.getSocketFactory();
            HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        schemeRegistry = new SchemeRegistry();
        schemeRegistry.register (new Scheme ("http", PlainSocketFactory.getSocketFactory(), 80));

        /*org.apache.http.conn.ssl.SSLSocketFactory sslSocketFactory = org.apache.http.conn.ssl.SSLSocketFactory.getSocketFactory();
        sslSocketFactory.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));*/

        try {
            //SSL no pear certification 에러가 발생하는 모델명 명시. 예외적으로 ALLOW_ALL_HOSTNAME_VERIFIER 처리한다.
            boolean isWeakSSLModel = false;
            /*
             * 현재까지 확인된 문제발생 모델은 : SHW-M250S (os ver : 4.0.3)
             * 그래서 갤럭시S2만 대상으로 잡음 // cjkim 2015-07-22 g pro 모델 추가
             *
             */
            String [] weakSSLModels = {"SHW-M250S","SHW-M250K", "SHW-M250L" , "LG-F240S"};
            if(weakSSLModels!=null && weakSSLModels.length>0){
                for(String model : weakSSLModels){
                    if(!TextUtils.isEmpty(model) && model.equalsIgnoreCase(Build.MODEL)) {
                        isWeakSSLModel = true;
                        break;
                    }
                }
            }

            //예외처리 대상 모델폰들
            if(isWeakSSLModel){
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                SFSSLSocketFactory sslSocketFactory = new SFSSLSocketFactory(trustStore);
                sslSocketFactory.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
            }
            //정상폰들
            else{
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                SFSSLSocketFactory sslSocketFactory = new SFSSLSocketFactory(trustStore);
                sslSocketFactory.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
                schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
            }

        } catch (Exception e) {
            e.printStackTrace();
            //에러시 기존코드로 동작시킴
            try {
                org.apache.http.conn.ssl.SSLSocketFactory sslSocketFactory = org.apache.http.conn.ssl.SSLSocketFactory.getSocketFactory();
                sslSocketFactory.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
                schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    public void setContext(Context a) {
        mContext = a;
    }

    class TempTrustManager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    public void setConnectionTimeout(int milsec) {
        if(milsec < 5) return;
        this.connectionTimeout = milsec;
    }

    public void setCacheDirectory(String dir) {
        cacheStorage.setCurrentDirectory(dir);
        cacheStorage.createTempFile(dir);
    }

    public String getCacheDirectory() {
        return cacheStorage.getCurrentDirectory();
    }

    public String getExternalDirectory() {
        if(externalStorage != null) {
            return externalStorage.getCurrentDirectory();
        }
        return null;
    }

    public void setExternalDirectory(String dir) throws StorageException {
        externalStorage.createTempFile(dir);
        externalStorage.setCurrentDirectory(dir);
        externalStorage.makeDirectory();
    }

    public void setTransformAdaptor(ITransformBytesAdaptor adaptor) {
        externalStorage.setTranformBytesAdaptor(adaptor);
    }

    public String existFileFromExternalStorage(String url) {
        String filename = URLRequestHandler.getImageFileNameFromURL(url);
        filename = externalStorage.existFile(filename);
        return filename;
    }

    public String existFileFromCache(String url) {
        String filename = URLRequestHandler.getImageFileNameFromURL(url);
        filename = cacheStorage.existFile(filename);
        return filename;
    }

    public InputStream getInputStreamFromCacheStorage(String filename) {
        try {
            return cacheStorage.getInputStream(filename);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public InputStream getInputStreamFromExternalStorage(String filename) {
        try {
            return externalStorage.getInputStream(filename);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // GET (HttpURLConnection 사용)
    private InputStream getServerData(HttpURLConnection urlConn) throws Exception {
        urlConn.setRequestProperty("device-os", "android");
        urlConn.setRequestProperty("device-osversion", android.os.Build.VERSION.RELEASE);
        urlConn.setRequestProperty("device-name", android.os.Build.MODEL);
        urlConn.setRequestProperty("app-version", String.valueOf(Constants.APP_VERSION_CODE));
        urlConn.setDoInput(true);
        urlConn.setConnectTimeout(connectionTimeout);
        //Log.d("", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!![InputStream getServerData]["+urlConn.getURL().toString()+"]!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        return urlConn.getInputStream();
    }

    // GET (HttpURLConnection 사용, 헤더를 직접 입력하는 경우)
    private InputStream getServerDataWithHeader(HttpURLConnection urlConn) throws Exception {
        //Log.d("", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!![X-Access-Token]["+urlConn.getRequestProperty("X-Access-Token")+"]!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        //Log.d("", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!![InputStream getServerData]["+urlConn.getURL().toString()+"]!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        return urlConn.getInputStream();
    }

    // POST, PUT (HttpGet/HttpPost/HttpPut 객체를 이용, GET 방식도 가능함)
    public HttpEntity getServerData(Request req) throws Exception {
        HttpEntity respEntity = null;
        HttpClient httpClient = null;
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setTcpNoDelay(httpParams, true);
        HttpConnectionParams.setConnectionTimeout(httpParams, Constants.TIMEOUT_HTTP_CONNECTION);
        HttpConnectionParams.setSoTimeout(httpParams, Constants.TIMEOUT_HTTP_SOCKET);

        ThreadSafeClientConnManager  cm = new ThreadSafeClientConnManager (httpParams, schemeRegistry);
        httpClient = new DefaultHttpClient(cm, httpParams);
        HttpUriRequest request = null;
        if(req.method.equalsIgnoreCase(Request.REQ_HTTP_METHOD_GET)) {
            request = new HttpGet(req.getURL());
        }
        else if(req.method.equalsIgnoreCase(Request.REQ_HTTP_METHOD_POST)) {
            HttpPost tmpPost = new HttpPost(req.getURL());
            if(req.paramEntity != null) {
                tmpPost.setEntity(req.paramEntity);
            }
            request = tmpPost;
        }
        else if(req.method.equalsIgnoreCase(Request.REQ_HTTP_METHOD_PUT)) {
            HttpPut tmpPut = new HttpPut(req.getURL());
            if(req.paramEntity != null) {
                tmpPut.setEntity(req.paramEntity);
            }
            request = tmpPut;
        }
        else {
            req.errorCode = Request.ERROR_INVALID_METHOD;
            req.errorMsg = "'" + req.method + "' 지원하지 않는 요청 방식입니다.";
            return null;
        }

        if(((SimpleRequest)req).addHeader != null && ((SimpleRequest)req).addHeader.size() > 0) {
            Iterator<String> keyIt = ((SimpleRequest)req).addHeader.keySet().iterator();
            while(keyIt.hasNext()) {
                String key = keyIt.next();
                String value = ((SimpleRequest)req).addHeader.get(key);
                request.addHeader(key, value);
            }
            // YCLOVE : POST 통신 시 기본헤더 추가 건
            request.addHeader("device-os", "android");
            request.addHeader("device-osversion", Build.VERSION.RELEASE);
            request.addHeader("device-name", Build.MODEL);
        }
        // 기본헤더
        else {
            String timeStamp = TimeUtil.getTimeStamp();
            String tokenKey = KeyUtil.getTokenKey(Constants.POST_SECRET_KEY, Constants.POST_ACCESS_KEY, timeStamp);

            if (req instanceof MultiPartFormRequest) {
                request.addHeader("Content-Type", Constants.HEADER_CONTENT_TYPE_MULTIPART);
            } else {
                request.addHeader("Content-Type", Constants.HEADER_CONTENT_TYPE_FORM);
            }
            request.addHeader("device-os-version", android.os.Build.VERSION.RELEASE);
            request.addHeader("device-model", android.os.Build.MODEL);
            request.addHeader("device-type", Constants.HEADER_DEVICE_TYPE);
            request.addHeader("post-app-version", String.valueOf(Constants.APP_VERSION_CODE));
            request.addHeader("post-app-user-id", SPUtil.getSharedPreference(mContext, Constants.SP_USER_ID));
            request.addHeader("post-access-token", tokenKey);
            request.addHeader("post-access-key", Constants.POST_ACCESS_KEY);
            request.addHeader("post-timestamp", timeStamp);
            request.addHeader("post-app-user-log", Constants.API_UV_LOGGING);

            if (Constants.DEBUG_REQUEST) {
                List<Header> httpHeaders = Arrays.asList(request.getAllHeaders());
                LogUtil.e("Request Header : " + request.getURI());
                for (Header header : httpHeaders) {
                    LogUtil.e(header.getName() + "(" + header.getValue() + ")");
                }
            }
        }

        HttpResponse resp = httpClient.execute(request);
        respEntity = resp.getEntity();

        List<Header> httpHeaders = Arrays.asList(resp.getAllHeaders());
        for (Header header : httpHeaders) {
            if ("post-stampuse-num".equals(header.getName()))
                Constants.STAMP_COUNT = Integer.valueOf(header.getValue());

            if ("post-rightuse-num".equals(header.getName()))
                Constants.RIGHT_COUNT = Integer.valueOf(header.getValue());
        }

        if (Constants.DEBUG_REQUEST) {
            LogUtil.e("Response Header : " + request.getURI());
            for (Header header : httpHeaders) {
                LogUtil.e(header.getName() + "(" + header.getValue() + ")");
            }
       }

        return respEntity;
    }

    private InputStream postServerData(HttpURLConnection urlConn, Request req) throws Exception {
        if (req instanceof SimpleRequest) {
            if(((SimpleRequest)req).addHeader != null && ((SimpleRequest)req).addHeader.size() > 0) {
                Iterator<String> keyIt = ((SimpleRequest)req).addHeader.keySet().iterator();
                while(keyIt.hasNext()) {
                    String key = keyIt.next();
                    String value = ((SimpleRequest)req).addHeader.get(key);
                    urlConn.setRequestProperty(key, value);
                }
            }
            urlConn.setRequestProperty("device-os", "android");
            urlConn.setRequestProperty("device-osversion", Build.VERSION.RELEASE);
            urlConn.setRequestProperty("device-name", Build.MODEL);
            urlConn.setRequestMethod(Request.REQ_HTTP_METHOD_POST);
            urlConn.setDoInput(true);
            urlConn.setConnectTimeout(connectionTimeout);
        } else if (req instanceof FileDownloadRequest) {

        }

        if(req instanceof MultiPartFormRequest) {
            return postMultiPart(urlConn, (MultiPartFormRequest)req);
        } else if(req instanceof MultiPartBitmapRequest){
            return postMultiPart(urlConn, (MultiPartBitmapRequest)req);
        } else {
            urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            if(req.params != null && req.params.length() > 0) {
                //urlConn.setRequestProperty("Content-Length", "" + req.params.getBytes().length);
                urlConn.setDoOutput(true);
                //urlConn.setUseCaches(false);
                DataOutputStream dataStream = null;

                dataStream = new DataOutputStream(urlConn.getOutputStream());
                dataStream.writeBytes(req.params);
                dataStream.flush();
                dataStream.close();
            }
        }

        return urlConn.getInputStream();
    }

    private InputStream postMultiPart(HttpURLConnection urlConn, MultiPartBitmapRequest req) throws Exception {
        String timeStamp = TimeUtil.getTimeStamp();
        String tokenKey = KeyUtil.getTokenKey(Constants.POST_SECRET_KEY, Constants.POST_ACCESS_KEY, timeStamp);

        urlConn.setRequestProperty("device-os-version", android.os.Build.VERSION.RELEASE);
        urlConn.setRequestProperty("device-model", android.os.Build.MODEL);
        urlConn.setRequestProperty("device-type", Constants.HEADER_DEVICE_TYPE);
        urlConn.setRequestProperty("post-app-version", String.valueOf(Constants.APP_VERSION_CODE));
        urlConn.setRequestProperty("post-app-user-id", SPUtil.getSharedPreference(mContext, Constants.SP_USER_ID));
        urlConn.setRequestProperty("post-access-token", tokenKey);
        urlConn.setRequestProperty("post-access-key", Constants.POST_ACCESS_KEY);
        urlConn.setRequestProperty("post-timestamp", timeStamp);
        urlConn.setRequestProperty("post-app-user-log", Constants.API_UV_LOGGING);

        urlConn.setRequestProperty("Connection", "Keep-Alive");
        urlConn.setRequestProperty("Content-Type","multipart/form-data; boundary=" + _BOUNDARY);

        urlConn.setDoInput(true);
        urlConn.setDoOutput(true);
        DataOutputStream dataStream = null;
        dataStream = new DataOutputStream(urlConn.getOutputStream());

//		if(req.imgParam != null) {
        multiPartParamWrite(dataStream, req.key, req.imgParam, req.mParamMap, req.imageName);
//		}

        dataStream.flush();
        dataStream.close();

        return urlConn.getInputStream();
    }

    private InputStream postMultiPart(HttpURLConnection urlConn, MultiPartFormRequest req) throws Exception {
        urlConn.setRequestProperty("device-os", "android");
        urlConn.setRequestProperty("device-osversion", Build.VERSION.RELEASE);
        urlConn.setRequestProperty("device-name", Build.MODEL);
        urlConn.setRequestProperty("Content-Type","multipart/form-data;boundary=" + _BOUNDARY);
        urlConn.setDoOutput(true);
        DataOutputStream dataStream = null;
        dataStream = new DataOutputStream(urlConn.getOutputStream());

        if(req.params != null && req.params.size() > 0) {
            Iterator<String> keyIt = req.params.keySet().iterator();
            while(keyIt.hasNext()) {
                String key = keyIt.next();
                String value = req.params.get(key);
                multiPartParamWrite(dataStream, key, value);
            }
        }
        if(req.fileParams != null && req.fileParams.size() > 0) {
            Iterator<String> keyIt = req.fileParams.keySet().iterator();
            while(keyIt.hasNext()) {
                String key = keyIt.next();
                String value = req.fileParams.get(key);
                multiPartFileWrite(dataStream, null, key, value);
            }
        }

        dataStream.flush ();
        dataStream.close();

        return urlConn.getInputStream();
    }

    //이미지 업로드 멀티파트
    private void multiPartParamWrite(DataOutputStream dataStream,String[] key, Bitmap[] bm, HashMap<String, String> paramMap, String[] imageName) throws Exception {
        for (int i = 0; i < bm.length; i++) {
            if(bm[i] != null && imageName[i].length() != 0){  // 파일이 있고 이미지 네임이 있을 경우에만 이미지 전송
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bm[i].compress(Bitmap.CompressFormat.JPEG, 75, baos);
                byte[] data = baos.toByteArray();
                dataStream.writeBytes(BOUNDARY_PREFIX + _BOUNDARY + CRLF);
                dataStream.writeBytes("Content-Disposition: form-data; name=\""+ key[i] +"\";filename=\"");
                dataStream.write(imageName[i].getBytes("UTF-8"));
                dataStream.writeBytes("\"" + CRLF);
                dataStream.writeBytes("Content-Type: image/jpeg " + CRLF);
                dataStream.writeBytes(CRLF);
                dataStream.write(data);
                dataStream.writeBytes(CRLF);
            }
        }

        if (key.length > 2 && key[2].equals(Constants.REQUEST_FILE_USE_TYPE_VOICE)) {
            String mFilePath = mContext.getDir(Constants.SERVICE_VOICE_FILE_PATH, mContext.MODE_PRIVATE).getAbsolutePath();
            String mFileName = Constants.SERVICE_VOICE_FILE_NAME;
            FileInputStream is = new FileInputStream(mFilePath + "/" + mFileName);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int bytesRead = 0;
            while ( (bytesRead = is.read(b)) != -1) {
                bos.write(b, 0, bytesRead);
            }
            byte[] bytes = bos.toByteArray();

            dataStream.writeBytes(BOUNDARY_PREFIX + _BOUNDARY + CRLF);
            dataStream.writeBytes("Content-Disposition: form-data; name=\""+ key[2] +"\";filename=\"");
            dataStream.write(mFileName.getBytes("UTF-8"));
            dataStream.writeBytes("\"" + CRLF);
            dataStream.writeBytes("Content-Type: audio/mpeg " + CRLF);
            dataStream.writeBytes(CRLF);
            dataStream.write(bytes);
            dataStream.writeBytes(CRLF);
        }

        Iterator<String> iterator = paramMap.keySet().iterator();
        while (iterator.hasNext()) {
            String strKey = (String) iterator.next();
            String strData = paramMap.get(strKey);
            multiPartParamWrite(dataStream, strKey, strData);
        }
        dataStream.writeBytes(BOUNDARY_PREFIX + _BOUNDARY + BOUNDARY_PREFIX + CRLF);
    }

    private void multiPartParamWrite(DataOutputStream dataStream, String fieldName, String fieldValue) throws Exception {
        dataStream.writeBytes(BOUNDARY_PREFIX + _BOUNDARY + CRLF);
        dataStream.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"" + CRLF);
        dataStream.writeBytes(CRLF);
        dataStream.write(fieldValue.getBytes("UTF-8"));
        dataStream.writeBytes(CRLF);
    }

    private void multiPartFileWrite(DataOutputStream dataStream, String mimeType, String fieldName, String fieldValue) throws Exception {
        dataStream.writeBytes(BOUNDARY_PREFIX + _BOUNDARY + CRLF);
        dataStream.writeBytes("Content-Disposition: form-data; name=\""
                + fieldName
                + "\";filename=\""
                + fieldValue.substring(fieldValue.lastIndexOf("/") + 1, fieldValue.length())
                + "\""
                + CRLF);
        if(mimeType != null) {
            dataStream.writeBytes("Content-Type: " + mimeType +  CRLF);
        }
        dataStream.writeBytes(CRLF);
        FileInputStream is = new FileInputStream(fieldValue);
        int bytesAvailable = is.available();
        int maxBufferSize = 1024;
        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
        byte[] buffer = new byte[bufferSize];

        int bytesRead = is.read(buffer, 0, bufferSize);
        while (bytesRead > 0) {
            dataStream.write(buffer, 0, bufferSize);
            bytesAvailable = is.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = is.read(buffer, 0, bufferSize);
        }
        is.close();
        dataStream.writeBytes(CRLF);
    }

    private void downloadFile(Storage storage, FileDownloadRequest req) throws RequestException {
        String filename = URLRequestHandler.getContentsFileNameFromReq(req);
        req.state = Request.STATE_BEFORE_DOWNLOAD;

        if(req.downloadStateListener != null) {
            req.downloadStateListener.onDownloadState(req);
        }

        if(filename != null) {
            boolean isCreate = true;

            if(req.useAleadyExist && storage.existFile(filename) != null) {
                req.state = Request.STATE_COMPLETE_DOWNLOAD;
                filename = storage.getCurrentDirectory() + File.separator + filename;
    			/*if(req.downloadStateListener != null) {
    			 	req.downloadStateListener.onDownloadState(req);
    			} */
                isCreate = false;
            }

            if(isCreate) {
                InputStream in = null;
                HttpURLConnection urlConn = null;
                try {
                    urlConn = (HttpURLConnection)new URL(req.requestURL).openConnection();
                    if(req.method.equalsIgnoreCase(Request.REQ_HTTP_METHOD_GET)) {
                        in = getServerData(urlConn);
                    }
                    else if(req.method.equalsIgnoreCase(Request.REQ_HTTP_METHOD_POST)) {
                        in = postServerData(urlConn, req);
                    }
                    else {
                        req.errorCode = Request.ERROR_INVALID_METHOD;
                        req.errorMsg = "'" + req.method + "' 지원하지 않는 요청 방식입니다.";
                    }
                }
                catch(Exception e) {
                    LogUtil.e(e.getMessage());
                    req.errorCode = Request.ERROR_NETWORK_ERROR;
                    req.errorMsg = "네트워크 접속에 실패 하였습니다.";
                    e.printStackTrace();
                }

                if(in != null) {
                    try {
                        File tempFile = new File(storage.getCurrentDirectory() + File.separator + tempFileName);

                        if(urlConn.getContentEncoding() == null) {
                            tempFile = storage.writeFile(in, tempFileName);
                        }
                        else {
                            tempFile = storage.writeFile(in, tempFileName, urlConn.getContentEncoding());
                        }
                        in.close();

                        filename = storage.getCurrentDirectory() + File.separator + filename;
                        File newFile = new File(filename);
                        if(newFile.exists()) {
                            newFile.delete();
                        }
                        tempFile.renameTo(newFile);
                        req.state = Request.STATE_COMPLETE_DOWNLOAD;
                    }
                    catch(Exception e) {
                        req.errorCode = Request.ERROR_WRITE_FILE;
                        req.errorMsg = e.getMessage();
                        e.printStackTrace();
                    }
                }
            }
        }
        if(req.errorCode == Request.ERROR_NONE) {
            req.downloadedFileName = filename;
        }
    }

    // 현재 여기 탐
    // GET, POST, PUT 여기서 분기
    private void readServerData(SimpleRequest req) throws RequestException {
        if(req.method.equalsIgnoreCase(Request.REQ_HTTP_METHOD_GET)) {
            try {
                HttpURLConnection urlConn = (HttpURLConnection)new URL(req.requestURL).openConnection();

                if(((SimpleRequest)req).addHeader != null && ((SimpleRequest)req).addHeader.size() > 0) {
                    // GET인데 기본 헤더가 아닌 헤더
                    Iterator<String> keyIt = ((SimpleRequest)req).addHeader.keySet().iterator();
                    while(keyIt.hasNext()) {
                        String key = keyIt.next();
                        String value = ((SimpleRequest)req).addHeader.get(key);
                        urlConn.setRequestProperty(key, value);
                    }
                    urlConn.setRequestProperty("device-os", "android");
                    urlConn.setRequestProperty("device-osversion", Build.VERSION.RELEASE);
                    urlConn.setRequestProperty("device-name", Build.MODEL);
                    urlConn.setDoInput(true);
                    urlConn.setConnectTimeout(connectionTimeout);

                    req.in = getServerDataWithHeader(urlConn);
                }
                else {
                    req.in = getServerData(urlConn);
                }
            }
            catch(Exception e) {
                LogUtil.e(e.getMessage());
                throw RequestException.createException("", Request.ERROR_NETWORK_ERROR);
            }
        }
        else if(req.method.equalsIgnoreCase(Request.REQ_HTTP_METHOD_POST)) {
            // 파라미터를 스트링으로 직접 입력한 경우, HttpURLConnection으로 POST 통신
            if((req.params != null && req.params.length() > 0) || (req instanceof MultiPartBitmapRequest)) {
                try {
                    HttpURLConnection urlConn = (HttpURLConnection)new URL(req.requestURL).openConnection();
                    req.in = postServerData(urlConn, req);
                }
                catch(Exception e) {
                    LogUtil.e(e.getMessage());
                    throw RequestException.createException("", Request.ERROR_NETWORK_ERROR);
                }
            }
            // HttpPost 객체를 이용, 스트링이 아닌 paramEntity를 이용함
            else {
                try {
                    req.in = getServerData(req).getContent();
                }
                catch(Exception e) {
                    LogUtil.e(e.getMessage());
                    throw RequestException.createException("", Request.ERROR_NETWORK_ERROR);
                }
            }
        }
        else if(req.method.equalsIgnoreCase(Request.REQ_HTTP_METHOD_PUT)) {
            try {
                req.in = getServerData(req).getContent();
            }
            catch(Exception e) {
                LogUtil.e(e.getMessage());
                throw RequestException.createException("", Request.ERROR_NETWORK_ERROR);
            }
        }
        else {
            throw RequestException.createException(req.method, Request.ERROR_INVALID_METHOD);
        }

        req.state = Request.STATE_COMPLETE_DOWNLOAD;
    }

    private boolean processRequest(Request req) throws RequestException {
        if(req == null) return false;

        if(req.showProgressDialog()) {
            req.showDialog();
        }

        if(req.storageType == Request.STORAGE_TYPE_MEM) {
            // 현재 여기 탐
            readServerData((SimpleRequest)req);
        }
        else if(req.storageType == FileDownloadRequest.STORAGE_TYPE_CACHE) {
            downloadFile(cacheStorage, (FileDownloadRequest)req);
        }
        else if(req.storageType == FileDownloadRequest.STORAGE_TYPE_EXTERNAL) {
            downloadFile(externalStorage, (FileDownloadRequest)req);
        }
        else {
            throw new RequestException("Invalid Storage", Request.ERROR_INVALID_STORAGE);
        }

        if(req.downloadStateListener != null) {
            req.downloadStateListener.onDownloadState(req);
        }

        req.onComplete();
        return true;
    }

    public void run() {
        while(isRunning) {
            if(isPause) {
                try{
                    Thread.sleep(500);
                }
                catch(Exception e) {}
                continue;
            }

            Request req = pop();
            try {
                if(processRequest((Request)req)) {
                    try{
                        Thread.sleep(20);
                    } catch(Exception e) {}
                }
                else {
                    try{
                        Thread.sleep(100);
                    }
                    catch(Exception e) {}
                }
            }
            catch(RequestException re) {
                if(req.downloadStateListener != null) {
                    req.downloadStateListener.onDownloadState(req);
                }
            }
        }
    }

    public void start() {
        isPause = false;
        isRunning = true;
        removeAll();
        downloadThread = new Thread(this);
        downloadThread.start();
    }

    public boolean isStarted() {
        return isRunning;
    }

    public void stop() {
        isRunning = false;
    }

    public void pause() {
        isPause = true;
    }

    public void resume() {
        isPause = false;
    }

    public synchronized void removeAll() {

        for(int i = 0; i < queue.size(); i++) {
            Request req = (Request)queue.get(i);
            if(req.getOnDownloadStateListener() != null) {
                req.state = Request.STATE_REMOVE_ALL;
                req.downloadStateListener.onDownloadState(req);
            }
        }
        queue.clear();
    }

    public synchronized void removeAllXML() {
        Iterator<Request> it = queue.iterator();
        while(it.hasNext()) {
            Request req = it.next();
            if(req.requestContentsType == Request.REQ_CONTENTS_XML) {
                if(req.getOnDownloadStateListener() != null) {
                    req.state = Request.STATE_REMOVE_ALL;
                    req.downloadStateListener.onDownloadState(req);
                }
                queue.remove(req);
            }
        }
    }

    public synchronized Request pop() {
        if(queue.size() > 0) {
            return queue.remove(0);
        }
        return null;
    }

    class ReqProgressDialog implements Runnable {
        Activity activity;
        ProgressDialog dialog;
        public ReqProgressDialog(Activity activity) {
            this.activity = activity;
        }

        public void run() {
            dialog = ProgressDialog.show(activity, "","Loading...", true, true);
        }

        public void show() {
            if(activity != null) {
                activity.runOnUiThread(this);
            }
        }

        public void cancel() {
            if(dialog != null) {
                dialog.cancel();
            }
        }
    }

    public synchronized void addRequest(Request req)
            throws RequestException {
        int reqType = req.getRequestType();
        if(reqType == Request.REQ_TYPE_ASYNC) {
            req.handler = this;
            queue.add(req);
        }
        else if(reqType == Request.REQ_TYPE_INDEPENDENT_ASYNC) {
            Thread thread = new Thread(new IndependentAsyncRunnable(this, req));
            thread.start();
        }
        else { // 현재 여기 탐.....
            processRequest(req);
        }
    }

    class IndependentAsyncRunnable implements Runnable {
        URLRequestHandler handler;
        Request req;
        IndependentAsyncRunnable(URLRequestHandler handler, Request req) {
            this.handler = handler;
            this.req = req;
        }
        public void run() {
            try {
                handler.processRequest(req);
            }
            catch(Exception e) {
                if(req.downloadStateListener != null) {
                    req.downloadStateListener.onDownloadState(req);
                }
            }
        }
    }

    public static String getImageFileNameFromURL(String url) {
        String tmpFileName = null;
        String path = url;
        String[] tmpBuf = path.split("/");
        for(int i = tmpBuf.length - 1; i >= 0; i--) {
            if(tmpBuf[i].length() > 0) {
                tmpFileName = tmpBuf[i];
                int index = tmpFileName.lastIndexOf(".");
                if(index > 0) {
                    tmpFileName = tmpFileName.substring(0, index) + ".jpg";
                }
                else {
                    tmpFileName = tmpFileName + ".jpg";
                }
                break;
            }
        }

        return tmpFileName;
    }

    public static String getFileNameFromXML(String url) {
        String tmpFileName = null;
        String path = url;
        String[] tmpBuf = path.split("/");
        for(int i = tmpBuf.length - 1; i >= 0; i--) {
            if(tmpBuf[i].length() > 0) {
                tmpFileName = tmpBuf[i];
                int index = tmpFileName.lastIndexOf(".");
                if(index > 0) {
                    tmpFileName = tmpFileName.substring(0, index) + ".xml";
                }
                else {
                    tmpFileName = tmpFileName + ".xml";
                }
                break;
            }
        }

        return tmpFileName;
    }

    public static String getContentsFileNameFromReq(Request req) {
        String filename = null;
        switch(req.getRequestContentsType()) {
            case Request.REQ_CONTENTS_IMAGE :
                filename = getImageFileNameFromURL(req.getURL());
                break;
            case Request.REQ_CONTENTS_FILE :
                filename = ((FileDownloadRequest)req).downloadedFileName;
                break;
            case Request.REQ_CONTENTS_XML :
                filename = getFileNameFromXML(req.getURL());
                break;
        }

        return filename;
    }

    public void copyCacheToExternal(String url){
        try {
            String filename = getImageFileNameFromURL(url);
            String srcFile = cacheStorage.getCurrentDirectory() + filename;
            String destFile = filename;
            File f1 = new File(srcFile);
            InputStream in = new FileInputStream(f1);

            externalStorage.writeFile(in, destFile);
            in.close();
        }
        catch(FileNotFoundException ex) {}
        catch(IOException e) {}
    }

    public static URLRequestHandler getHandler() {
        if(urlRequestHandler == null) {
            urlRequestHandler = new URLRequestHandler();
        }

        return urlRequestHandler;
    }

    public static URLRequestHandler getHandler(String cacheDir) {
        URLRequestHandler urlRequestHandler = new URLRequestHandler();
        urlRequestHandler.setCacheDirectory(cacheDir);

        return urlRequestHandler;
    }

    public static URLRequestHandler getHandler(String cacheDir, String externalDir) {
        URLRequestHandler urlRequestHandler = new URLRequestHandler();
        urlRequestHandler.setCacheDirectory(cacheDir);
        try {
            String extPath = Environment.getExternalStorageDirectory().getPath();
            urlRequestHandler.setCacheDirectory(extPath + externalDir);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return urlRequestHandler;
    }
}
