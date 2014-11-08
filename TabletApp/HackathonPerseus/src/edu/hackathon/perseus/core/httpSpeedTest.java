/*
 *   wan delay model analysis via http protocol
 *   Copyright (C) 2014  CagatayS
 *   05.07.2013
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */

package edu.hackathon.perseus.core;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;

public class httpSpeedTest {
	public static enum REGION { EU, USA, ASIA }
	private static final String USER_AGENT = "Mozilla/5.0";
	private final String CrLf = "\r\n";
	private String amazonDomain = "";
	private static String amazonEuDomain = "http://amazoneu.cagatay.me";
	private static String amazonUsaDomain = "http://amazonusa.cagatay.me";
	private static String amazonAsiaDomain = "http://amazonasia.cagatay.me";

	public httpSpeedTest(String domain){
		amazonDomain = domain;
	}

	//Returns Mbps (Mega bits per seconds)
	public int testPing(){
		String result="0";
		String domain = amazonDomain.replace("http://", "");
		String keyword = "Average = ";
		String command = "ping " + domain + " -W 5";
		try {
			Process p = Runtime.getRuntime().exec(command);
			BufferedReader inputStream = new BufferedReader(
					new InputStreamReader(p.getInputStream()));

			String s = "";
			// reading output stream of the command
			while ((s = inputStream.readLine()) != null) {
				if(s.contains(keyword))
					result = s.substring(s.indexOf(keyword)+keyword.length(),s.lastIndexOf("ms"));
			}

		} catch (Exception e) {
			System.out.println("Exception is fired in download test. error:" + e.getMessage());
			result = "0";
		}
		return Integer.parseInt(result);
	}

	//Returns Mbps (Mega bits per seconds)
	public double testDownload(){
		double bw=0.0;

		try {
			Date oldTime = new Date();
			URL obj = new URL(amazonDomain + "/download_test.bin");
			HttpURLConnection httpGetCon = (HttpURLConnection) obj.openConnection();
			// optional default is GET
			httpGetCon.setRequestMethod("GET");
			httpGetCon.setConnectTimeout(5000); //set timeout to 5 seconds
			httpGetCon.setReadTimeout(5000);
			//add request header
			httpGetCon.setRequestProperty("User-Agent", USER_AGENT);
			if(httpGetCon.getResponseCode()==200){
				Date newTime = new Date();
				double milliseconds = newTime.getTime() - oldTime.getTime();
				int lenght = httpGetCon.getContentLength();

				bw = ((double)lenght*8)/(milliseconds*(double)1000);
			}

			//			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			//			String inputLine;
			//			StringBuffer response = new StringBuffer();
			//			while ((inputLine = in.readLine()) != null) {
			//				response.append(inputLine);
			//			}
			//			in.close();
			//
			//			//print result
			//			System.out.println(response.toString());
		} catch (MalformedURLException e) {
			System.out.println("MalformedURLException is fired!");
		} catch (IOException e) {
			System.out.println("Exception is fired in download test. error:" + e.getMessage());
		}

		return bw;
	}

	public double testUpload(InputStream uploadFileIs) {
		return doUpload("upload_test.php", uploadFileIs, "upload_test.bin");
	}

	public double uploadResults(InputStream uploadFileIs, String fileName) {
		return doUpload("upload_results.php", uploadFileIs, fileName);
	}

	private double doUpload(String phpFile, InputStream uploadFileIs, String fileName) {
		URLConnection conn = null;
		OutputStream os = null;
		InputStream is = null;
		double bw = 0.0;

		try {
			String response="";
			Date oldTime = new Date();
			URL url = new URL(amazonDomain + "/" + phpFile);
			String boundary= "---------------------------4664151417711";
			conn = url.openConnection();
			conn.setDoOutput(true);
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);

			byte[] fileData = new byte[uploadFileIs.available()];
			uploadFileIs.read(fileData);
			uploadFileIs.close();

			String message1 = "--" + boundary + CrLf;
			message1 += "Content-Disposition: form-data;";
			message1 += "name=\"uploadedfile\"; filename=\""+fileName+"\"" + CrLf;
			message1 += "Content-Type: text/plain; charset=UTF-8" + CrLf + CrLf;

			// the file is sent between the messages in the multipart message.
			String message2 = CrLf + "--" + boundary + "--" + CrLf;

			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

			int contentLenght=message1.length() + message2.length() + fileData.length;

			// might not need to specify the content-length when sending chunked data.
			conn.setRequestProperty("Content-Length", String.valueOf(contentLenght));

			os = conn.getOutputStream();

			os.write(message1.getBytes());

			// SEND THE IMAGE
			int index = 0;
			int size = 1024;
			do {
				if ((index + size) > fileData.length) {
					size = fileData.length - index;
				}
				os.write(fileData, index, size);
				index += size;
			} while (index < fileData.length);

			os.write(message2.getBytes());
			os.flush();

			is = conn.getInputStream();

			char buff = 512;
			int len;
			byte[] data = new byte[buff];
			do {
				len = is.read(data);

				if (len > 0) {
					response += new String(data, 0, len);
				}
			} while (len > 0);

			if(response.equals("200")){
				Date newTime = new Date();
				double milliseconds = newTime.getTime() - oldTime.getTime();
				bw = ((double)contentLenght*8)/(milliseconds*(double)1000);
			}
		} catch (Exception e) {
			System.out.println("Exception is fired in upload test. error:" + e.getMessage());
		} finally {
			try {
				os.close();
			} catch (Exception e) {
				//System.out.println("Exception is fired in os.close. error:" + e.getMessage());
			}
			try {
				is.close();
			} catch (Exception e) {
				//System.out.println("Exception is fired in is.close. error:" + e.getMessage());
			}
		}
		return bw;
	}

	// HTTP POST request
	public void sendPost() throws Exception {

		String url = "https://selfsolve.apple.com/wcResults.do";
		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
				new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		//print result
		System.out.println(response.toString());
	}

	public static String getRedirectUrl(REGION region){
		String result = "";

		switch (region) {
		case EU:
			result = amazonEuDomain;
			break;
		case USA:
			result = amazonUsaDomain;
			break;
		case ASIA:
			result = amazonAsiaDomain;
			break;
		}

		System.out.println("Trying to get real IP address of " + result);
		try {
			/*
			HttpHead headRequest = new HttpHead(result);
			HttpClient client = new DefaultHttpClient();
		    HttpResponse response = client.execute(headRequest);
		    final int statusCode = response.getStatusLine().getStatusCode();
		    if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY ||
		        statusCode == HttpStatus.SC_MOVED_TEMPORARILY)
		    {
		        String location = response.getHeaders("Location")[0].toString();
		        String redirecturl = location.replace("Location: ", "");
		        result = redirecturl;
		    }
			*/
			
			URL url = new URL(result);
			HttpURLConnection httpGetCon = (HttpURLConnection) url.openConnection();
			httpGetCon.setInstanceFollowRedirects(false);
			httpGetCon.setRequestMethod("GET");
			httpGetCon.setConnectTimeout(5000); //set timeout to 5 seconds

			httpGetCon.setRequestProperty("User-Agent", USER_AGENT);
			
			int status = httpGetCon.getResponseCode();
			System.out.println("code: " + status);
			if(status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status ==    HttpURLConnection.HTTP_SEE_OTHER)
				result = httpGetCon.getHeaderField("Location");
			
		} catch (Exception e) {
			System.out.println("Exception is fired in redirector getter. error:" + e.getMessage());
			e.printStackTrace();
		}
		System.out.println("Real IP address is " + result);
		return result;
	}
}
