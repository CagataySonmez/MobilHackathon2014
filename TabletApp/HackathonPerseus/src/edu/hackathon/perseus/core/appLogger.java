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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import edu.hackathon.perseus.app.MainService;
import edu.hackathon.perseus.core.httpSpeedTest.REGION;


class logItem
{
	private long time;
	private String downloadBw;
	private String uploadBw;
	private int pingDelay;
	
	logItem(long _time)
	{
		time = _time;
		downloadBw="0";
		uploadBw="0";
		pingDelay=0;
	}
	public long getTime()
	{
		return time;
	}
	public void setDownloadBw(String bw)
	{
		downloadBw = bw.replace(',', '.'); //adjust for matlab;
	}
	public void setUploadBw(String bw)
	{
		uploadBw = bw.replace(',', '.'); //adjust for matlab;
	}
	public void setPingDelay(int delay)
	{
		pingDelay = delay;
	}
	public String toString()
	{
		return downloadBw + ";" + uploadBw + ";" + pingDelay;
	}
}

public class appLogger {
	private String userName;
	private String deviceName;
	private double latitude;
	private double longitude;
	private Date appStartTime;
	private Date appEndTime;
	private int offset;
	private int lastEuLogId;
	private int lastUsaLogId;
	private int lastAsiaLogId;
	private Map<Integer, logItem> euMap;
	private Map<Integer, logItem> usaMap;
	private Map<Integer, logItem> asiaMap;
	
	private static appLogger singleton = new appLogger();

	/* A private Constructor prevents any other 
	 * class from instantiating.
	 */
	private appLogger(){
	}

	/* Static 'instance' method */
	public static appLogger getInstance( ) {
		return singleton;
	}
	
	public void init(String username, String devicename){
		latitude=0;
		longitude=0;
		lastEuLogId=0;
		lastUsaLogId=0;
		lastAsiaLogId=0;

        offset = TimeZone.getDefault().getOffset(Calendar.ZONE_OFFSET);
		if(TimeZone.getDefault().useDaylightTime())
            offset += 3600000;
		
		euMap = new HashMap<Integer, logItem>();
		usaMap = new HashMap<Integer, logItem>();
		asiaMap = new HashMap<Integer, logItem>();
		
		userName = username;
		deviceName = devicename;
		setAppStartTime();
	}
	
	public void setLocation(double _latitude, double _longitude){
		latitude=_latitude;
		longitude=_longitude;
	}
	
	private void setAppStartTime(){
		appStartTime = new Date();
	}

	private void setAppEndTime(){
		appEndTime = new Date();
	}
	
	public Date getAppStartTime(){
		return appStartTime;
	}

	public Date getAppEndTime(){
		return appEndTime;
	}
		
	public int getLastLogId(httpSpeedTest.REGION region){
		int result = 0;
		if(region == httpSpeedTest.REGION.USA)
			result = lastEuLogId;	
		else if(region == httpSpeedTest.REGION.EU)
			result = lastUsaLogId;
		else
			result = lastAsiaLogId;

		return result;
	}

	public int initLog(httpSpeedTest.REGION region){
		//get unix time, and add offset to get normalized time
		long unixTime = offset + System.currentTimeMillis();
		//Matlab -> timestamp to str: datestr(unixTime/86400000 + datenum(1970,1,1))
		
		int result = 0;
		if(region == httpSpeedTest.REGION.USA){
			lastEuLogId++;
			usaMap.put(lastEuLogId, new logItem(unixTime));
			result = lastEuLogId;
		}
		else if(region == httpSpeedTest.REGION.EU){
			lastUsaLogId++;
			euMap.put(lastUsaLogId, new logItem(unixTime));
			result = lastUsaLogId;
		}
		else{
			lastAsiaLogId++;
			asiaMap.put(lastAsiaLogId, new logItem(unixTime));
			result=lastAsiaLogId;
		}
		return result;
	}
	
	public void addDownloadLog(int timeId, httpSpeedTest.REGION region, String bw){
		if(region == httpSpeedTest.REGION.USA)
			usaMap.get(timeId).setDownloadBw(bw);
		else if(region == httpSpeedTest.REGION.EU)
			euMap.get(timeId).setDownloadBw(bw);
		else
			asiaMap.get(timeId).setDownloadBw(bw);
	}

	public void addUploadLog(int timeId, httpSpeedTest.REGION region, String bw){
		if(region == httpSpeedTest.REGION.USA)
			usaMap.get(timeId).setUploadBw(bw);
		else if(region == httpSpeedTest.REGION.EU)
			euMap.get(timeId).setUploadBw(bw);
		else
			asiaMap.get(timeId).setUploadBw(bw);
	}
	
	public void addPingLog(int timeId, httpSpeedTest.REGION region, int delay){
		if(region == httpSpeedTest.REGION.USA)
			usaMap.get(timeId).setPingDelay(delay);
		else if(region == httpSpeedTest.REGION.EU)
			euMap.get(timeId).setPingDelay(delay);
		else
			asiaMap.get(timeId).setPingDelay(delay);
	}

	public String saveLogsToFile(String folder) throws IOException{
		setAppEndTime();
		String euFile = folder + "/eu_analysis.log";
		String usaFile = folder + "/usa_analysis.log";
		String asiaFile = folder + "/asia_analysis.log";
		saveHelper(euFile,euMap);
		saveHelper(usaFile,usaMap);
		saveHelper(asiaFile,asiaMap);
		
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy_HH-mm-ss");
		String zipFileName = dateFormat.format(appStartTime) + "_" + userName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_") + ".zip";
		FileOutputStream fos = new FileOutputStream(folder + "/" + zipFileName);
        ZipOutputStream zos = new ZipOutputStream(fos);
        ZipEntry ze1 = new ZipEntry(euFile);
        ZipEntry ze2 = new ZipEntry(usaFile);
        ZipEntry ze3 = new ZipEntry(asiaFile);
        
        zos.putNextEntry(ze1);
        //read the file and write to ZipOutputStream
        FileInputStream fis = new FileInputStream(euFile);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = fis.read(buffer)) > 0) {
            zos.write(buffer, 0, len);
        }
        fis.close();
        
        zos.putNextEntry(ze2);
        //read the file and write to ZipOutputStream
        fis = new FileInputStream(usaFile);
        buffer = new byte[1024];
        while ((len = fis.read(buffer)) > 0) {
            zos.write(buffer, 0, len);
        }
        fis.close();
        
        zos.putNextEntry(ze3);
        //read the file and write to ZipOutputStream
        fis = new FileInputStream(asiaFile);
        buffer = new byte[1024];
        while ((len = fis.read(buffer)) > 0) {
            zos.write(buffer, 0, len);
        }
        fis.close();
         
        //Close the zip entry to write to zip file
        zos.closeEntry();
        
        //Close resources
        zos.close();
        fos.close();
        
        //if everything goes well, delete text files. we do not need them since we have zip file.
        File fileToBeDeleted = new File(euFile);
        fileToBeDeleted.delete();
        fileToBeDeleted = new File(usaFile);
        fileToBeDeleted.delete();
        fileToBeDeleted = new File(asiaFile);
        fileToBeDeleted.delete();
        
        return zipFileName;
	}
	
	private void saveHelper(String FileName, Map<Integer, logItem> logMap) throws IOException{
		boolean append=false;
		DateFormat dateFormat = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
		//Matlab conversion: datestr([2003,10,24,12,45,07])
		
		File file = new File(FileName);
		FileWriter fileWriter = new FileWriter(file, append);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		//write analyser or location name
		bufferedWriter.write(userName);
		bufferedWriter.newLine();
		//write deveice name
		bufferedWriter.write(deviceName);
		bufferedWriter.newLine();
		//write latitude
		bufferedWriter.write(String.valueOf(latitude));
		bufferedWriter.newLine();
		//write longitude
		bufferedWriter.write(String.valueOf(longitude));
		bufferedWriter.newLine();
		//write interval of actions
		bufferedWriter.write(MainService.getInterval().toString());
		bufferedWriter.newLine();
		//write time offset
		bufferedWriter.write(String.valueOf(offset));
		bufferedWriter.newLine();
		//write start date of the simulation
		bufferedWriter.write(dateFormat.format(appStartTime));
		bufferedWriter.newLine();
		//write end date of the simulation
		bufferedWriter.write(dateFormat.format(appEndTime));
		bufferedWriter.newLine();
		
//		boolean startSaving = false;
//		int time = 0;
//		//start saving from 00:00 am!
//		for (Map.Entry<Integer, logItem> entry : logMap.entrySet()) {
//			//Integer key = entry.getKey();
//			logItem value = entry.getValue();
//		    
//			if(startSaving==false){
//				if(value.getTime().contains(" 00:00"))
//					startSaving = true;
//				else
//					continue;
//			}
//			
//			bufferedWriter.write(time + ";" + value.toString());
//			bufferedWriter.newLine();
//			time += MainService.getInterval();
//		}
//		
//		//continue saving to complete 24 hours loop
//		for (Map.Entry<Integer, logItem> entry : logMap.entrySet()) {
//			//Integer key = entry.getKey();
//			logItem value = entry.getValue();
//		    
//			if(value.getTime().contains(" 00:00"))
//				break;
//			
//			bufferedWriter.write(time + ";" + value.toString());
//			bufferedWriter.newLine();
//			time += MainService.getInterval();
//		}
		
		for (Map.Entry<Integer, logItem> entry : logMap.entrySet()) {
			logItem value = entry.getValue();
			bufferedWriter.write(value.getTime() + ";" + value.toString());
			bufferedWriter.newLine();
		}
		
		bufferedWriter.close();
	}
}
