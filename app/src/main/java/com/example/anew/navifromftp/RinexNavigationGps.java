/*
 * Copyright (c) 2011 Eugenio Realini, Mirko Reguzzoni, Cryms sagl - Switzerland. All Rights Reserved.
 *
 * This file is part of goGPS Project (goGPS).
 *
 * goGPS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * goGPS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with goGPS.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.example.anew.navifromftp;

import android.util.Log;

import com.example.anew.Constellations.Time;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

/**
 * @author Lorenzo Patocchi, cryms.com
 * <p>
 * This class retrieve RINEX file on-demand from known server structures
 */
public class RinexNavigationGps implements NavigationIono {

    private final static String TAG = "RinexNavigationGps";
    /**
     * cache for negative answers
     */
    private Hashtable<String, Date> negativeChache = new Hashtable<String, Date>();

    /**
     * Folder containing downloaded files
     */
    public String RNP_CACHE = "./rnp-cache";

    //private RinexNavigationParserGps rnp = null;//用于解析导航电文的对象
    private RinexNavigationParserGpsGLONASS rnp=null;
    public RinexNavigationParserGpsGLONASS getRnp()
    {
        return rnp;
    }



    public void getFromFTP(String urltemplate) throws IOException {


        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Calendar c = Calendar.getInstance();
        Time t = new Time(c.getTimeInMillis());
        String url = t.formatTemplateM(urltemplate);
        RinexNavigationParserGpsGLONASS rnp=null;

        if (negativeChache.containsKey(url)) {
            if (System.currentTimeMillis() - negativeChache.get(url).getTime() < 20 * 60 * 1000) {
            } else {
                negativeChache.remove(url);
                System.out.println("移除");
            }
        }

        String filename = url.replaceAll("[ ,/:]", "_");
        if (filename.endsWith(".gz")) filename = filename.substring(0, filename.length() - 3);

        File rnf = new File(RNP_CACHE, filename);
        if (rnf.exists()) {
            System.out.println(url + " from cache file " + rnf);
            try {
                //若文件存在，对其进行读取
                //rnp = new RinexNavigationParserGps(rnf);
                rnp=new RinexNavigationParserGpsGLONASS(rnf);
                rnp.init();
                this.rnp = rnp;
            } catch (Exception e) {
                rnf.delete();
            }
        }

        // if the file doesn't exist of is invalid
        System.out.println(url + " from the net.");
        FTPClient ftp = new FTPClient();


        try {

            Log.w(TAG, "getFromFTP: Getting data from FTP server...");

            int reply;
            System.out.println("URL: " + url);
            url = url.substring("ftp://".length());
            final String server = url.substring(0, url.indexOf('/'));
            System.out.println("sever:" + server);
            String remoteFile = url.substring(url.indexOf('/'));
            String remotePath = remoteFile.substring(0, remoteFile.lastIndexOf('/'));
            remoteFile = remoteFile.substring(remoteFile.lastIndexOf('/') + 1);

            try {
                ftp.connect(server);
                ftp.login("anonymous", "");
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 获取响应字符串
            String replyString = ftp.getReplyString();
            if (replyString == null) {
                System.out.println("FTP 响应字符串为 null");
            } else {
                System.out.println("FTP 响应字符串: " + replyString);
                if (replyString.startsWith("230")) {
                    negativeChache.put(url, new Date());
                }
            }

            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                try {
                    ftp.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.err.println("FTP server refused connection.");
            }

            ftp.enterLocalPassiveMode();
            ftp.setRemoteVerificationEnabled(false);


            System.out.println("cwd to " + remotePath + " " + ftp.changeWorkingDirectory(remotePath));
            System.out.println(ftp.getReplyString());

            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            System.out.println(ftp.getReplyString());

            //下载文件

//            OutputStream out = null;
//
//            out = new FileOutputStream(rnf, false);

            ftp.setRestartOffset(0); //设置从哪里开始下，就是断点下载

            InputStream is = null;

            is = ftp.retrieveFileStream(remoteFile);


            System.out.println(ftp.getReplyString());


            InputStream uis = is;


            System.out.println("open " + remoteFile);

            //解压
//            if (remoteFile.endsWith(".Z")) {
//                uis = new UncompressInputStream(is);
//            }
//            rnp = new RinexNavigationParserGps(uis, rnf);
//            rnp.init();
//            this.rnp = rnp;

            //解压
            if (remoteFile.endsWith(".gz")) {
                //10.14改解压方式,使用GZIPInputStream解压.gz
                uis = new GZIPInputStream(is);
                //uis = new UncompressInputStream(is);

            }
            //rnp = new RinexNavigationParserGps(uis, rnf);
            rnp = new RinexNavigationParserGpsGLONASS(uis, rnf);
            rnp.init();
            this.rnp = rnp;
            Log.d("MyTag", "gpsNAV");



/*
            //解压之后再下载文件
            byte[] b = new byte[1024];
            int length = 0;
            while ((length = uis.read(b)) != -1) {
                out.write(b, 0, length);
            }

            out.flush();
            out.close();

*/
            is.close();


            ftp.completePendingCommand();

            ftp.logout();


            Log.w(TAG, "getFromFTP: Received data from server");

        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                    // do nothing
                }
            }
        }

    }


//    public SatellitePosition getSatPositionAndVelocities(long unixTime, double range, int satID, char satType, double receiverClockError) {
//        //RinexNavigationParserGps rnp = this.rnp;
//        RinexNavigationParserGpsGLONASS rnp=this.rnp;
//        if (rnp != null) {
//            if (rnp.isTimestampInEpocsRange(unixTime)) {
//                return rnp.getSatPositionAndVelocities(unixTime,range , satID, satType, receiverClockError);
//            } else {
//                return null;
//            }
//        }
//
//        return null;
//    }

    @Override
    public IonoGps getIonoGps() {
        return this.rnp.getIonoGps();
    }

    @Override
    public IonoGalileo getIonoGalileo() {
        return null;
    }
}
