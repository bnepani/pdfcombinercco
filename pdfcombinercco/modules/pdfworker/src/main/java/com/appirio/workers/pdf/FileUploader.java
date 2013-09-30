package com.appirio.workers.pdf;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * @author jesus
 *
 * This class uploads a file to specified url in http rest fashion.
 */
public class FileUploader {
	 
    /**
     * A generic method to execute any type of Http Request and constructs a response object
     * @param requestBase the request that needs to be exeuted
     * @return server response as <code>String</code>
     */
    private static String executeRequest(HttpRequestBase requestBase){
        String responseString = "" ;
 
        InputStream responseStream = null ;
        HttpClient client = new DefaultHttpClient () ;
        try{
            HttpResponse response = client.execute(requestBase) ;
            if (response != null){
                HttpEntity responseEntity = response.getEntity() ;
 
                if (responseEntity != null){
                    responseStream = responseEntity.getContent() ;
                    if (responseStream != null){
                        BufferedReader br = new BufferedReader (new InputStreamReader (responseStream)) ;
                        String responseLine = br.readLine() ;
                        String tempResponseString = "" ;
                        while (responseLine != null){
                            tempResponseString = tempResponseString + responseLine + System.getProperty("line.separator") ;
                            responseLine = br.readLine() ;
                        }
                        br.close() ;
                        if (tempResponseString.length() > 0){
                            responseString = tempResponseString ;
                        }
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if (responseStream != null){
                try {
                    responseStream.close() ;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        client.getConnectionManager().shutdown() ;
 
        return responseString ;
    }
 
    /**
     * Method that builds the multi-part form data request
     * @param urlString the urlString to which the file needs to be uploaded
     * @param file the actual file instance that needs to be uploaded
     * @param jsonBody
     * @return server response as <code>String</code>
     */
    public String executeMultiPartRequest(String urlString, File file, String jsonBody, Map<String, String>headerMap) {
 
        HttpPost postRequest = new HttpPost (urlString) ;

        if(headerMap != null) {
	        for(String headerKey : headerMap.keySet()) {
	            postRequest.addHeader(headerKey, headerMap.get(headerKey));
	        }
        }

        MultipartEntity multiPartEntity = new MultipartEntity ();

		// add json body
		StringBody jsonContentStringBody = org.apache.http.entity.mime.content.StringBody.create(jsonBody, "application/json", Charset.forName("UTF-8"));
		multiPartEntity.addPart("entity_content", jsonContentStringBody);

		/*Need to construct a FileBody with the file that needs to be attached and specify the mime type of the file. Add the fileBody to the request as an another part.
		This part will be considered as file part and the rest of them as usual form-data parts*/
		FileBody fileBody = new FileBody(file, "application/octect-stream");
		multiPartEntity.addPart("VersionData", fileBody);
 
		postRequest.setEntity(multiPartEntity) ;
 
        return executeRequest (postRequest) ;
    }
}