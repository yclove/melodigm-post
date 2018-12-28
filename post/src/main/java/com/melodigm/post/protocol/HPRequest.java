package com.melodigm.post.protocol;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import com.google.gson.Gson;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.data.AddCabinetMusicReq;
import com.melodigm.post.protocol.data.AppVersionRes;
import com.melodigm.post.protocol.data.GetActionLogReq;
import com.melodigm.post.protocol.data.GetCabinetDataRes;
import com.melodigm.post.protocol.data.GetCabinetDeleteReq;
import com.melodigm.post.protocol.data.GetCabinetDescReq;
import com.melodigm.post.protocol.data.GetCabinetDescRes;
import com.melodigm.post.protocol.data.GetCalendarDateReq;
import com.melodigm.post.protocol.data.GetCalendarDateRes;
import com.melodigm.post.protocol.data.GetColorImageReq;
import com.melodigm.post.protocol.data.GetColorImageRes;
import com.melodigm.post.protocol.data.GetInitInfoRes;
import com.melodigm.post.protocol.data.GetMusicPathReq;
import com.melodigm.post.protocol.data.GetMusicPathRes;
import com.melodigm.post.protocol.data.GetMusicPaymentReq;
import com.melodigm.post.protocol.data.GetMusicSecurityReq;
import com.melodigm.post.protocol.data.GetMusicSecurityRes;
import com.melodigm.post.protocol.data.GetNotificationCenterReq;
import com.melodigm.post.protocol.data.GetNotificationCenterRes;
import com.melodigm.post.protocol.data.GetOstDataReq;
import com.melodigm.post.protocol.data.GetOstDataRes;
import com.melodigm.post.protocol.data.GetOstRepleDeleteReq;
import com.melodigm.post.protocol.data.GetOstRepleReq;
import com.melodigm.post.protocol.data.GetOstRepleRes;
import com.melodigm.post.protocol.data.GetOstRepleWriteReq;
import com.melodigm.post.protocol.data.GetPostDataReq;
import com.melodigm.post.protocol.data.GetPostDataRes;
import com.melodigm.post.protocol.data.GetPostPositionDataReq;
import com.melodigm.post.protocol.data.GetPostUserInfoRes;
import com.melodigm.post.protocol.data.GetSearchLocationReq;
import com.melodigm.post.protocol.data.GetSearchLocationRes;
import com.melodigm.post.protocol.data.GetSearchOstRelatedReq;
import com.melodigm.post.protocol.data.GetSearchOstRelatedRes;
import com.melodigm.post.protocol.data.GetSearchOstReq;
import com.melodigm.post.protocol.data.GetSearchOstRes;
import com.melodigm.post.protocol.data.GetSearchStoryReq;
import com.melodigm.post.protocol.data.GetSendMailReq;
import com.melodigm.post.protocol.data.GetShareImageUploadDataRes;
import com.melodigm.post.protocol.data.GetStampDataReq;
import com.melodigm.post.protocol.data.GetStampDataRes;
import com.melodigm.post.protocol.data.GetTimeLineReq;
import com.melodigm.post.protocol.data.GetTimeLineRes;
import com.melodigm.post.protocol.data.GetUpdateNotificationReq;
import com.melodigm.post.protocol.data.NotiSettingItem;
import com.melodigm.post.protocol.data.PostDataItem;
import com.melodigm.post.protocol.data.RegCabinetDataReq;
import com.melodigm.post.protocol.data.RegOstDataReq;
import com.melodigm.post.protocol.data.RegPostDataReq;
import com.melodigm.post.protocol.data.RegPostUserReq;
import com.melodigm.post.protocol.data.RegPostUserRes;
import com.melodigm.post.protocol.data.SetCabinetSortReq;
import com.melodigm.post.protocol.data.SetOstDeleteReq;
import com.melodigm.post.protocol.data.SetOstTitleReq;
import com.melodigm.post.protocol.data.SetPostDeleteReq;
import com.melodigm.post.protocol.data.SetPostLikeReq;
import com.melodigm.post.protocol.data.SetPostNotifyReq;
import com.melodigm.post.protocol.data.SetPostUserInfoReq;
import com.melodigm.post.protocol.data.SetSnsAccountRestoreReq;
import com.melodigm.post.protocol.data.SetSnsAccountRestoreRes;
import com.melodigm.post.protocol.data.SetSnsAccountSyncReq;
import com.melodigm.post.protocol.network.FileDownloadRequest;
import com.melodigm.post.protocol.network.MultiPartBitmapRequest;
import com.melodigm.post.protocol.network.Request;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.protocol.network.SimpleRequest;
import com.melodigm.post.protocol.network.URLRequestHandler;
import com.melodigm.post.util.LogUtil;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HPRequest {
	public final int ERROR_NONE = 0;
	private URLRequestHandler requestHandler;
	Context ctx;
	
	public static final String ROOT_PATH = Environment.getExternalStorageDirectory() + "";
    public static final String ROOTING_PATH_1 = "/system/bin/su";
    public static final String ROOTING_PATH_2 = "/system/xbin/su";
    public static final String ROOTING_PATH_3 = "/system/app/SuperUser.apk";
    public static final String ROOTING_PATH_4 = "/data/data/com.noshufou.android.su";
    
    public String[] RootFilesPath = new String[]{
            ROOT_PATH + ROOTING_PATH_1 ,
            ROOT_PATH + ROOTING_PATH_2 , 
            ROOT_PATH + ROOTING_PATH_3 , 
            ROOT_PATH + ROOTING_PATH_4
    };
    
    boolean isRootingFlag = false;

	public HPRequest(Context context) {
		ctx = context;
		requestHandler = URLRequestHandler.getHandler(context.getCacheDir().getPath());
		requestHandler.setContext(context);
	}
	
	public boolean checkRooting() {
		try {
            Runtime.getRuntime().exec("su");
            isRootingFlag = true;
        } 
		catch ( Exception e) {
            // Exception 나면 루팅 false;
            isRootingFlag = false;
        }
		
		if(!isRootingFlag) {
            isRootingFlag = checkRootingFiles(createFiles(RootFilesPath));
        }
		
		return isRootingFlag;
	}
	
	/**
     * 루팅파일 의심 Path를 가진 파일들을 생성 한다.
     */
    private File[] createFiles(String[] sfiles) {
        File[] rootingFiles = new File[sfiles.length];
        for(int i=0 ; i < sfiles.length; i++) {
            rootingFiles[i] = new File(sfiles[i]);
        }
        return rootingFiles;
    }
     
    /**
     * 루팅파일 여부를 확인 한다.
     */
    private boolean checkRootingFiles(File... file) {
        boolean result = false;
        for(File f : file) {
            if(f != null && f.exists() && f.isFile()) {
                result = true;
                break;
            }
            else {
                result = false;
            }
        }
        return result;
    }

	public String convertStreamToString(InputStream is) throws IOException {
		if (is != null) {
			Writer writer = new StringWriter();
			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n > buffer.length ? buffer.length:n);
				}
			}
			catch(Exception e) {
				
			}
			finally {
				is.close();
			}
			try {
				return writer.toString();
			}
			catch(Exception e) {
				return "";
			}
			
		} else {
			return "";
		}
	}
	
	public static String convertStreamToString2(InputStream is, String type) {
		BufferedReader reader;
		try {
			if(type == null) {
				reader = new BufferedReader(new InputStreamReader(is));
			}
			else {
				reader = new BufferedReader(new InputStreamReader(is, type));
			}
		}
		catch (Exception e) {
			reader = new BufferedReader(new InputStreamReader(is));
	    }
	     
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    try {
	      while ((line = reader.readLine()) != null) {
	        sb.append(line + "\n");
	      }
	    } 
	    catch (IOException e) {
	      e.printStackTrace();
	    } 
	    finally {
	      try {
	        is.close();
	      } 
	      catch (IOException e) {
	        e.printStackTrace();
	      }
	    }
	    return sb.toString();
	}

	private SimpleRequest postRequest(String uri, AbstractHttpEntity paramEntity, Map<String, String> addHeader) throws RequestException {
		SimpleRequest request = new SimpleRequest();
		request.setURL(Constants.API_POST + uri);

        /**
         * YCNOTE - currentTimeMillis
         * 표준시간(협정 세계시 (UTC))와의 사이에 발생하는 차이로, 즉, UTC 1970년 1월 1일 00:00:00.000 을 기준으로한 현제 시간의 차이를 long형으로 반환한다.
         */
        if (System.currentTimeMillis() - Constants.API_LASTEST_TIMESTAMP > 1800000) {
            Constants.API_UV_LOGGING = "Y";
        } else {
            Constants.API_UV_LOGGING = "N";
        }
        Constants.API_LASTEST_TIMESTAMP = System.currentTimeMillis();

		request.addHeader = addHeader;
		request.setRequestType(Request.REQ_TYPE_SYNC);
		request.setRequestContentsType(Request.REQ_CONTENTS_FILE);
		request.setProtocol(Request.REQ_PROTOCOL_HTTPS);
		request.setMethod(Request.REQ_HTTP_METHOD_POST);
		request.setParamList(paramEntity);
		requestHandler.addRequest(request);

		return request;
	}

    private MultiPartBitmapRequest postBitmapMultiPartRequest(String[] key, Bitmap[] bm, String uri, String[] imageName, HashMap<String, String> paramMap, Map<String, String> addHeader) throws RequestException {
		MultiPartBitmapRequest request = new MultiPartBitmapRequest();
		request.setURL(Constants.API_POST + "" + uri); //URL 미리 만들어서 반환하기!

        if (System.currentTimeMillis() - Constants.API_LASTEST_TIMESTAMP > 1800000) {
            Constants.API_UV_LOGGING = "Y";
        } else {
            Constants.API_UV_LOGGING = "N";
        }
        Constants.API_LASTEST_TIMESTAMP = System.currentTimeMillis();

        request.addHeader = addHeader;
        request.setRequestType(Request.REQ_TYPE_SYNC);
        request.setRequestContentsType(Request.REQ_CONTENTS_FILE);
        request.setProtocol(Request.REQ_PROTOCOL_HTTPS);
        request.setMethod(Request.REQ_HTTP_METHOD_POST);
        request.setImgParameter(key, imageName, bm, paramMap);
        requestHandler.addRequest(request);
		return request;
	}

    private FileDownloadRequest postFileDownloadRequest(String uri, String downloadedFileName, AbstractHttpEntity paramEntity) throws RequestException {
        FileDownloadRequest request = new FileDownloadRequest();
        request.setURL(Constants.API_POST + uri);

        request.setRequestType(Request.REQ_TYPE_SYNC);
        request.setStorageType(FileDownloadRequest.STORAGE_TYPE_CACHE);
        request.setDownloadedFileName(downloadedFileName);
        request.setRequestContentsType(Request.REQ_CONTENTS_FILE);
        request.setProtocol(Request.REQ_PROTOCOL_HTTPS);
        request.setMethod(Request.REQ_HTTP_METHOD_POST);
        request.setParamList(paramEntity);
        requestHandler.addRequest(request);

        return request;
    }
	
	private JSONObject checkResopnse(SimpleRequest request) throws POSTException, JSONException, IOException {
		String jsonStr = convertStreamToString2(request.getInputStream(), "UTF-8");

        LogUtil.e(jsonStr);

		JSONObject jsonObj = null;
		String MESSAGE = "";
        String CODE = "";

		try {
			jsonObj = new JSONObject(jsonStr);
            MESSAGE = jsonObj.getString("MESSAGE");
            CODE = jsonObj.getString("CODE");
			if ("0000".equals(CODE)) {
                if (!jsonObj.isNull("RESPONSE")) {
                    jsonObj = jsonObj.getJSONObject("RESPONSE");
                }
			} else {
                throw new POSTException(CODE, MESSAGE);
            }
		} catch(JSONException e) {
			LogUtil.e("checkResopnse ERROR : " + jsonStr);
			throw new POSTException("500", "상태수신에 실패하였습니다.");
		}

		return jsonObj;
	}

    public AppVersionRes getAppVersion() throws RequestException, POSTException {
        AppVersionRes appVersionRes = null;

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", ""));

        SimpleRequest request;

        try {
            request = postRequest("app0101v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("앱 버전 정보 요청 : " + request.getURL());

            JSONObject jsonObj = checkResopnse(request);
            appVersionRes = new AppVersionRes(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return appVersionRes;
    }

    public RegPostUserRes regPostUser(RegPostUserReq regPostUserReq) throws RequestException, POSTException {
        RegPostUserRes regPostUserRes = null;

        Gson gson = new Gson();
        String strData = gson.toJson(regPostUserReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("usr0201v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("사용자 정보 등록 요청 : " + request.getURL());
            LogUtil.e(strData);

            JSONObject jsonObj = checkResopnse(request);
            regPostUserRes = new RegPostUserRes(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return regPostUserRes;
    }

    public GetPostUserInfoRes getPostUserInfo() throws RequestException, POSTException {
        GetPostUserInfoRes getPostUserInfoRes = null;

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", ""));

        SimpleRequest request;

        try {
            request = postRequest("usr0202v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("사용자 정보 조회 요청 : " + request.getURL());

            JSONObject jsonObj = checkResopnse(request);
            getPostUserInfoRes = new GetPostUserInfoRes(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return getPostUserInfoRes;
    }

    public void setPostUserInfo(SetPostUserInfoReq setPostUserInfoReq) throws RequestException, POSTException {
        Gson gson = new Gson();
        String strData = gson.toJson(setPostUserInfoReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("usr0222v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("사용자 디바이스 정보 수정 요청 : " + request.getURL());
            LogUtil.e(strData);

            checkResopnse(request);
        }
        catch (Exception e) {}
    }

    public GetPostDataRes getPostData(GetPostDataReq getPostDataReq) throws RequestException, POSTException {
        GetPostDataRes getPostDataRes = null;

        Gson gson = new Gson();
        String strData = gson.toJson(getPostDataReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("pst0302v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("POST 리스트 요청 : " + request.getURL());
            LogUtil.e(strData);

            JSONObject jsonObj = checkResopnse(request);
            getPostDataRes = new GetPostDataRes(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return getPostDataRes;
    }

    public GetSearchLocationRes getSearchLocation(GetSearchLocationReq getSearchLocationReq) throws RequestException, POSTException {
        GetSearchLocationRes getSearchLocationRes = null;

        Gson gson = new Gson();
        String strData = gson.toJson(getSearchLocationReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("app0112v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("장소명 조회 요청 : " + request.getURL());
            LogUtil.e(strData);

            JSONObject jsonObj = checkResopnse(request);
            getSearchLocationRes = new GetSearchLocationRes(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return getSearchLocationRes;
    }

    public void regPost(RegPostDataReq regPostDataReq, String[] imageName, String[] mContentKey, Bitmap[] mContentBitmap) throws RequestException, POSTException {
        MultiPartBitmapRequest request;

        Gson gson = new Gson();
        String strData = gson.toJson(regPostDataReq);

        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("REQ_DATA", strData);

        try {
            request = postBitmapMultiPartRequest(mContentKey, mContentBitmap, "pst0303v1.post", imageName, paramMap, null);
            LogUtil.e("POST 등록 요청 : " + request.getURL());
            LogUtil.e(paramMap.toString());
            checkResopnse(request);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }
    }

    public void regOst(RegOstDataReq regOstDataReq) throws RequestException, POSTException {
        Gson gson = new Gson();
        String strData = gson.toJson(regOstDataReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("pst0306v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("OST 등록 요청 : " + request.getURL());
            LogUtil.e(strData);

            checkResopnse(request);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }
    }

    public void setPostLike(SetPostLikeReq setPostLikeReq) throws RequestException, POSTException {
        Gson gson = new Gson();
        String strData = gson.toJson(setPostLikeReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("pst0310v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("좋아요/이모티콘/컬러 설정 요청 : " + request.getURL());
            LogUtil.e(strData);

            checkResopnse(request);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }
    }

    public GetOstDataRes getOstData(GetOstDataReq getOstDataReq) throws RequestException, POSTException {
        GetOstDataRes getOstDataRes = null;

        Gson gson = new Gson();
        String strData = gson.toJson(getOstDataReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("pst0305v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("OST 리스트 요청 : " + request.getURL());
            LogUtil.e(strData);

            JSONObject jsonObj = checkResopnse(request);
            getOstDataRes = new GetOstDataRes(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return getOstDataRes;
    }

    public void setPostNotify(SetPostNotifyReq setPostNotifyReq) throws RequestException, POSTException {
        Gson gson = new Gson();
        String strData = gson.toJson(setPostNotifyReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("pst0311v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("POST/OST/Radio 신고 등록 요청 : " + request.getURL());
            LogUtil.e(strData);

            checkResopnse(request);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }
    }

    // POST 삭제 Request
    public void setPostDelete(SetPostDeleteReq setPostDeleteReq) throws RequestException, POSTException {
        Gson gson = new Gson();
        String strData = gson.toJson(setPostDeleteReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("pst0304v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("POST 삭제 요청 : " + request.getURL());
            LogUtil.e(strData);

            checkResopnse(request);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }
    }

    // OST 삭제 Request
    public void setOstDelete(SetOstDeleteReq setOstDeleteReq) throws RequestException, POSTException {
        Gson gson = new Gson();
        String strData = gson.toJson(setOstDeleteReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("pst0307v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("OST 삭제 요청 : " + request.getURL());
            LogUtil.e(strData);

            checkResopnse(request);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }
    }

    public void setSnsAccountSync(SetSnsAccountSyncReq setSnsAccountSyncReq) throws RequestException, POSTException {
        Gson gson = new Gson();
        String strData = gson.toJson(setSnsAccountSyncReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("usr0203v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("사용자 SNS 계정 등록 요청 : " + request.getURL());
            LogUtil.e(strData);

            checkResopnse(request);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }
    }

    public SetSnsAccountRestoreRes setSnsAccountRestore(SetSnsAccountRestoreReq setSnsAccountRestoreReq) throws RequestException, POSTException {
        SetSnsAccountRestoreRes setSnsAccountRestoreRes = null;

        Gson gson = new Gson();
        String strData = gson.toJson(setSnsAccountRestoreReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("usr0204v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("사용자 데이터 복원 요청 : " + request.getURL());
            LogUtil.e(strData);

            JSONObject jsonObj = checkResopnse(request);
            setSnsAccountRestoreRes = new SetSnsAccountRestoreRes(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return setSnsAccountRestoreRes;
    }

    public void setOstTitle(SetOstTitleReq setOstTitleReq) throws RequestException, POSTException {
        Gson gson = new Gson();
        String strData = gson.toJson(setOstTitleReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("pst0312v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("POST 타이틀곡 등록 요청 : " + request.getURL());
            LogUtil.e(strData);

            checkResopnse(request);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }
    }

    public GetSearchOstRelatedRes getSearchOstRelated(GetSearchOstRelatedReq getSearchOstRelatedReq, String searchType) throws RequestException, POSTException {
        GetSearchOstRelatedRes getSearchOstRelatedRes = null;

        Gson gson = new Gson();
        String strData = gson.toJson(getSearchOstRelatedReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            // OST 음원 자동완성  검색
            if ("OST".equals(searchType)) {
                request = postRequest("sac0501v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
                LogUtil.e("OST 음원 자동완성  검색 요청 : " + request.getURL());
            }
            // 이야기 검색 자동완성  검색
            else if ("STORY".equals(searchType)) {
                request = postRequest("sac0506v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
                LogUtil.e("이야기 자동완성 검색어 요청 : " + request.getURL());
            }
            // 노래 (앨범/음원/아티스트) 자동완성 검색
            else {
                request = postRequest("sac0503v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
                LogUtil.e("노래 (앨범/음원/아티스트) 자동완성 검색 요청 : " + request.getURL());
            }
            LogUtil.e(strData);

            JSONObject jsonObj = checkResopnse(request);
            getSearchOstRelatedRes = new GetSearchOstRelatedRes(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return getSearchOstRelatedRes;
    }

    public GetSearchOstRes getSearchOst(GetSearchOstReq getSearchOstReq, String searchType) throws RequestException, POSTException {
        GetSearchOstRes getSearchOstRes = null;

        Gson gson = new Gson();
        String strData = gson.toJson(getSearchOstReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            // OST 음원 상세 검색
            if ("OST".equals(searchType)) {
                request = postRequest("sac0502v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
                LogUtil.e("OST 음원 상세 검색 요청 : " + request.getURL());
            }
            // 노래 (앨범/음원/아티스트) 상세 검색
            else {
                request = postRequest("sac0504v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
                LogUtil.e("노래 (앨범/음원/아티스트) 상세 검색  요청 : " + request.getURL());
            }
            LogUtil.e(strData);

            JSONObject jsonObj = checkResopnse(request);
            getSearchOstRes = new GetSearchOstRes(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return getSearchOstRes;
    }

    public GetCabinetDataRes getCabinetData() throws RequestException, POSTException {
        GetCabinetDataRes getCabinetDataRes = null;

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", ""));

        SimpleRequest request;

        try {
            request = postRequest("mus0407v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("보관함 목록 요청 : " + request.getURL());

            JSONObject jsonObj = checkResopnse(request);
            getCabinetDataRes = new GetCabinetDataRes(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return getCabinetDataRes;
    }

    public void regCabinetData(RegCabinetDataReq regCabinetDataReq, String[] imageName, String[] mContentKey, Bitmap[] mContentBitmap) throws RequestException, POSTException {
        MultiPartBitmapRequest request;

        Gson gson = new Gson();
        String strData = gson.toJson(regCabinetDataReq);

        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("REQ_DATA", strData);

        try {
            request = postBitmapMultiPartRequest(mContentKey, mContentBitmap, "mus0408v1.post", imageName, paramMap, null);
            LogUtil.e("보관함 생성 요청 : " + request.getURL());
            LogUtil.e(paramMap.toString());
            checkResopnse(request);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }
    }

    public void editCabinetData(RegCabinetDataReq regCabinetDataReq, String[] imageName, String[] mContentKey, Bitmap[] mContentBitmap) throws RequestException, POSTException {
        MultiPartBitmapRequest request;

        Gson gson = new Gson();
        String strData = gson.toJson(regCabinetDataReq);

        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("REQ_DATA", strData);

        try {
            request = postBitmapMultiPartRequest(mContentKey, mContentBitmap, "mus0410v1.post", imageName, paramMap, null);
            LogUtil.e("보관함 수정 요청 : " + request.getURL());
            LogUtil.e(paramMap.toString());
            checkResopnse(request);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }
    }

    public GetCabinetDescRes getCabinetDesc(GetCabinetDescReq getCabinetDescReq) throws RequestException, POSTException {
        GetCabinetDescRes getCabinetDescRes = null;

        Gson gson = new Gson();
        String strData = gson.toJson(getCabinetDescReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("mus0412v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("보관함 내 음원 목록 요청 : " + request.getURL());
            LogUtil.e(strData);

            JSONObject jsonObj = checkResopnse(request);
            getCabinetDescRes = new GetCabinetDescRes(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return getCabinetDescRes;
    }

    public void addCabinetMusic(AddCabinetMusicReq addCabinetMusicReq) throws RequestException, POSTException {
        Gson gson = new Gson();
        String strData = gson.toJson(addCabinetMusicReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("mus0413v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("보관함에 음원 등록 요청 : " + request.getURL());
            LogUtil.e(strData);

            checkResopnse(request);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }
    }

    public void setCabinetSort(SetCabinetSortReq setCabinetSortReq) throws RequestException, POSTException {
        Gson gson = new Gson();
        String strData = gson.toJson(setCabinetSortReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("mus0414v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("보관함에 음원 정렬 및 삭제 요청 : " + request.getURL());
            LogUtil.e(strData);

            checkResopnse(request);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }
    }

    public GetInitInfoRes getInitInfo() throws RequestException, POSTException {
        GetInitInfoRes getInitInfoRes = null;

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", ""));

        SimpleRequest request;

        try {
            request = postRequest("usr0217v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("사용자 활동 내역 건수(사용자 초기화시 사용) 요청 : " + request.getURL());

            JSONObject jsonObj = checkResopnse(request);
            getInitInfoRes = new GetInitInfoRes(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return getInitInfoRes;
    }

    public void initPost() throws RequestException, POSTException {
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", ""));

        SimpleRequest request;

        try {
            request = postRequest("usr0216v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("사용자 서비스 초기화 요청 : " + request.getURL());
            checkResopnse(request);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }
    }

    public GetTimeLineRes getTimeLine(GetTimeLineReq getTimeLineReq) throws RequestException, POSTException {
        GetTimeLineRes getTimeLineRes = null;

        Gson gson = new Gson();
        String strData = gson.toJson(getTimeLineReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("pst0321v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("MY POST 타임라인 목록 요청 : " + request.getURL());
            LogUtil.e(strData);

            JSONObject jsonObj = checkResopnse(request);
            getTimeLineRes = new GetTimeLineRes(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return getTimeLineRes;
    }

    public GetNotificationCenterRes getNotificationCenter(GetNotificationCenterReq getNotificationCenterReq) throws RequestException, POSTException {
        GetNotificationCenterRes getNotificationCenterRes;

        Gson gson = new Gson();
        String strData = gson.toJson(getNotificationCenterReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("usr0220v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("MY POST 알림센터 목록 요청 : " + request.getURL());
            LogUtil.e(strData);

            JSONObject jsonObj = checkResopnse(request);
            getNotificationCenterRes = new GetNotificationCenterRes(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return getNotificationCenterRes;
    }

    public GetOstRepleRes getOstReple(GetOstRepleReq getOstRepleReq) throws RequestException, POSTException {
        GetOstRepleRes getOstRepleRes = null;

        Gson gson = new Gson();
        String strData = gson.toJson(getOstRepleReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("pst0320v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("OST 대댓글 목록 요청 : " + request.getURL());
            LogUtil.e(strData);

            JSONObject jsonObj = checkResopnse(request);
            getOstRepleRes = new GetOstRepleRes(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return getOstRepleRes;
    }

    public void getOstRepleWrite(GetOstRepleWriteReq getOstRepleWriteReq) throws RequestException, POSTException {
        Gson gson = new Gson();
        String strData = gson.toJson(getOstRepleWriteReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("pst0318v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("OST 대댓글 등록 요청 : " + request.getURL());
            LogUtil.e(strData);

            checkResopnse(request);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }
    }

    public void getOstRepleDelete(GetOstRepleDeleteReq getOstRepleDeleteReq) throws RequestException, POSTException {
        Gson gson = new Gson();
        String strData = gson.toJson(getOstRepleDeleteReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("pst0319v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("OST 대댓글 삭제 요청 : " + request.getURL());
            LogUtil.e(strData);

            checkResopnse(request);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }
    }

    public GetColorImageRes getColorImage(GetColorImageReq getColorImageReq) throws RequestException, POSTException {
        GetColorImageRes getColorImageRes = null;

        Gson gson = new Gson();
        String strData = gson.toJson(getColorImageReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("pst0308v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("POST/RADIO 칼러 랜덤 배경 이미지 요청 : " + request.getURL());
            LogUtil.e(strData);

            JSONObject jsonObj = checkResopnse(request);
            getColorImageRes = new GetColorImageRes(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return getColorImageRes;
    }

    public PostDataItem getPostPositionData(GetPostPositionDataReq getPostPositionDataReq) throws RequestException, POSTException {
        PostDataItem mPostDataItem = null;

        Gson gson = new Gson();
        String strData = gson.toJson(getPostPositionDataReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("pst0315v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("POST 특정 정보 조회 요청 : " + request.getURL());
            LogUtil.e(strData);

            JSONObject jsonObj = checkResopnse(request);
            mPostDataItem = new PostDataItem(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return mPostDataItem;
    }

    public GetStampDataRes getStampData(GetStampDataReq getStampDataReq) throws RequestException, POSTException {
        GetStampDataRes getStampDataRes = null;

        Gson gson = new Gson();
        String strData = gson.toJson(getStampDataReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("usr0218v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("사용자 우표 활동 지수 목록 조회 요청 : " + request.getURL());
            LogUtil.e(strData);

            JSONObject jsonObj = checkResopnse(request);
            getStampDataRes = new GetStampDataRes(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return getStampDataRes;
    }

    public void getCabinetDelete(GetCabinetDeleteReq getCabinetDeleteReq) throws RequestException, POSTException {
        Gson gson = new Gson();
        String strData = gson.toJson(getCabinetDeleteReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("mus0409v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("보관함 삭제 요청 : " + request.getURL());
            LogUtil.e(strData);

            checkResopnse(request);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }
    }

    public GetCalendarDateRes getCalendarDate(GetCalendarDateReq getCalendarDateReq) throws RequestException, POSTException {
        GetCalendarDateRes getCalendarDateRes;

        Gson gson = new Gson();
        String strData = gson.toJson(getCalendarDateReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("pst0322v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("타임라인 달력 활동날짜 내역정보 요청 : " + request.getURL());
            LogUtil.e(strData);

            JSONObject jsonObj = checkResopnse(request);
            getCalendarDateRes = new GetCalendarDateRes(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return getCalendarDateRes;
    }

    public GetCalendarDateRes getCalendarDateForNotificationCenter(GetCalendarDateReq getCalendarDateReq) throws RequestException, POSTException {
        GetCalendarDateRes getCalendarDateRes;

        Gson gson = new Gson();
        String strData = gson.toJson(getCalendarDateReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("usr0221v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("알림센터 달력 활동날짜 내역정보 요청 : " + request.getURL());
            LogUtil.e(strData);

            JSONObject jsonObj = checkResopnse(request);
            getCalendarDateRes = new GetCalendarDateRes(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return getCalendarDateRes;
    }

    public GetCalendarDateRes getCalendarDateForPost(GetCalendarDateReq getCalendarDateReq) throws RequestException, POSTException {
        GetCalendarDateRes getCalendarDateRes;

        Gson gson = new Gson();
        String strData = gson.toJson(getCalendarDateReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("pst0317v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("사연 달력 활동날짜 내역정보 요청 : " + request.getURL());
            LogUtil.e(strData);

            JSONObject jsonObj = checkResopnse(request);
            getCalendarDateRes = new GetCalendarDateRes(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return getCalendarDateRes;
    }

    public GetPostDataRes getSearchStory(GetSearchStoryReq getSearchStoryReq) throws RequestException, POSTException {
        GetPostDataRes getPostDataRes = null;

        Gson gson = new Gson();
        String strData = gson.toJson(getSearchStoryReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("sac0507v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("이야기 상세 검색 요청 : " + request.getURL());
            LogUtil.e(strData);

            JSONObject jsonObj = checkResopnse(request);
            getPostDataRes = new GetPostDataRes(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return getPostDataRes;
    }

    public GetShareImageUploadDataRes uploadShareImage(String[] imageName, String[] mContentKey, Bitmap[] mContentBitmap) throws RequestException, POSTException {
        GetShareImageUploadDataRes getShareImageUploadDataRes = null;

        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("REQ_DATA", "");

        MultiPartBitmapRequest request;

        try {
            request = postBitmapMultiPartRequest(mContentKey, mContentBitmap, "app0113v1.post", imageName, paramMap, null);
            LogUtil.e("임시파일 업로드 요청 : " + request.getURL());
            LogUtil.e(paramMap.toString());

            JSONObject jsonObj = checkResopnse(request);
            getShareImageUploadDataRes = new GetShareImageUploadDataRes(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return getShareImageUploadDataRes;
    }

    public GetMusicPathRes getMusicPath(GetMusicPathReq getMusicPathReq) throws RequestException, POSTException {
        GetMusicPathRes getMusicPathRes;

        Gson gson = new Gson();
        String strData = gson.toJson(getMusicPathReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("mus0401v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("미디어 파일 다운로드 요청 : " + request.getURL());
            LogUtil.e(strData);

            JSONObject jsonObj = checkResopnse(request);
            getMusicPathRes = new GetMusicPathRes(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return getMusicPathRes;
    }

    public void getMusicPayment(GetMusicPaymentReq getMusicPaymentReq) throws RequestException, POSTException {
        Gson gson = new Gson();
        String strData = gson.toJson(getMusicPaymentReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("mus0402v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("음원 재생 Play 시간 등록 요청 : " + request.getURL());
            LogUtil.e(strData);

            checkResopnse(request);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }
    }

    public GetMusicSecurityRes getMusicSecurity(GetMusicSecurityReq getMusicSecurityReq) throws RequestException, POSTException {
        GetMusicSecurityRes getMusicSecurityRes;

        Gson gson = new Gson();
        String strData = gson.toJson(getMusicSecurityReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("mus0400v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("미디어 파일 서버 대칭키 요청 : " + request.getURL());
            LogUtil.e(strData);

            JSONObject jsonObj = checkResopnse(request);
            getMusicSecurityRes = new GetMusicSecurityRes(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return getMusicSecurityRes;
    }

    public void getSendMail(GetSendMailReq getSendMailReq) throws RequestException, POSTException {
        Gson gson = new Gson();
        String strData = gson.toJson(getSendMailReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("usr0224v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("사용자 커뮤니터 등록 요청 : " + request.getURL());
            LogUtil.e(strData);

            checkResopnse(request);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }
    }

    public NotiSettingItem getSelectNotification() throws RequestException, POSTException {
        NotiSettingItem notiSettingItem = null;

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", ""));

        SimpleRequest request;

        try {
            request = postRequest("usr0225v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("사용자 알림 설정 정보 요청 : " + request.getURL());

            JSONObject jsonObj = checkResopnse(request);
            notiSettingItem = new NotiSettingItem(jsonObj);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }

        return notiSettingItem;
    }

    public void getUpdateNotification(GetUpdateNotificationReq getUpdateNotificationReq) throws RequestException, POSTException {
        Gson gson = new Gson();
        String strData = gson.toJson(getUpdateNotificationReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("usr0223v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("사용자 알림 설정 정보 등록 요청 : " + request.getURL());
            LogUtil.e(strData);

            checkResopnse(request);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }
    }

    public void getActionLog(GetActionLogReq getActionLogReq) throws RequestException, POSTException {
        Gson gson = new Gson();
        String strData = gson.toJson(getActionLogReq);

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("REQ_DATA", strData));

        SimpleRequest request;

        try {
            request = postRequest("pst0301v1.post", new UrlEncodedFormEntity(nameValuePairs, Constants.SERVICE_CHAR_SET), null);
            LogUtil.e("POST 공유하기(SNS) 및 기타 로그 등록 요청 : " + request.getURL());
            LogUtil.e(strData);

            checkResopnse(request);
        } catch (JSONException e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        } catch (POSTException e) {
            throw new POSTException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new POSTException(POSTException.INVALID_DATA, e.getMessage());
        }
    }
}