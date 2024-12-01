package com.example.anew.navifromftp;

import android.util.Log;

import com.example.anew.Constellations.Time;
import com.example.anew.coord.SatellitePosition;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * <p>
 * Class for parsing RINEX navigation files
 * </p>
 *
 * @author Eugenio Realini, Cryms.com
 */
public class RinexNavigationParserGpsGLONASS extends EphemerisSystemGai implements NavigationIono{

    private File fileNav;
    private FileInputStream streamNav;
    private InputStreamReader inStreamNav;
    private BufferedReader buffStreamNav;

    private FileOutputStream cacheOutputStream;
    private OutputStreamWriter cacheStreamWriter;

    public static String newline = System.getProperty("line.separator");

    private final String TAG = this.getClass().getSimpleName();

    private ArrayList<EphGps> eph = new ArrayList<EphGps>(); /* GPS broadcast ephemerides */
    private int count=0;
    private int count_glonass=0;
    //private double[] iono = new double[8]; /* Ionosphere model parameters */
    private IonoGps iono = null; /* Ionosphere model parameters */
    //	private double A0; /* Delta-UTC parameters: A0 */
    //	private double A1; /* Delta-UTC parameters: A1 */
    //	private double T; /* Delta-UTC parameters: T */
    //	private double W; /* Delta-UTC parameters: W */
    //	private int leaps; /* Leap seconds */

    public float ver_f;

    private IonoGalileo ionoGalileo = null;
    private IonoGps ionoGps = null;
    private IonoBD ionoBD=null;
    // RINEX Read constructors
    public RinexNavigationParserGpsGLONASS(File fileNav) {
        this.fileNav = fileNav;
    }


    // RINEX Read constructors
    public RinexNavigationParserGpsGLONASS(InputStream is, File cache) {

        this.inStreamNav = new InputStreamReader(is);//将字节流转换为字符流，便于后续的字符数据处理
        if (cache != null) {
            File path = cache.getParentFile();//获取缓存文件的父目录
            if (!path.exists()) {
                //如果父目录不存在，则创建该目录，并记录日志信息。
                Log.i("RinexNavigationParserGps", "RinexNavigationParserGps: " + path.mkdirs());

            }
            try {
                cacheOutputStream = new FileOutputStream(cache);//创建文件输出流，用于写入缓存文件。
                cacheStreamWriter = new OutputStreamWriter(cacheOutputStream);//创建字符输出流，用于将字符数据写入缓存文件。
            } catch (FileNotFoundException e) {
                System.err.println("Exception writing " + cache+cache.length());
                e.printStackTrace();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.gogpsproject.Navigation#init()
     */
    public void init() throws IOException {
        open();
        int ver = parseHeaderNav();
        if (ver != 0) {
            if (ver == 2) {
                System.out.println("Ver. 2.x");
                parseDataNavV2();
            } else if (ver == 212) {
                System.out.println("Ver. 2.12");
                parseDataNavV2();
            } else if (ver == 3) {
                System.out.println("Ver. 3.01");
                parseDataNavV3();
            }
            close();
        } else {
            close();
            throw new RuntimeException(fileNav.toString() + " is invalid ");
        }
    }

    public EphGps getEph(int index) {
        if(eph.size()>0)
        return eph.get(index);
        else {
            System.out.println("卫星星历列表为空");
            return null;
        }
    }

    /**
     *
     */
    public void open() {
        try {

            if (fileNav != null) streamNav = new FileInputStream(fileNav);
            if (streamNav != null) inStreamNav = new InputStreamReader(streamNav);
            if (inStreamNav != null) buffStreamNav = new BufferedReader(inStreamNav);

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
    }

    public void close() {
        try {
            if (cacheStreamWriter != null) {
                cacheStreamWriter.flush();
                cacheStreamWriter.close();
            }
            if (cacheOutputStream != null) {
                cacheOutputStream.flush();
                cacheOutputStream.close();
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        try {

            if (buffStreamNav != null) buffStreamNav.close();
            if (inStreamNav != null) inStreamNav.close();
            if (streamNav != null) streamNav.close();


        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    /**
     *
     */
    public int parseHeaderNav() throws IOException {
        String sub;
        int ver=0;
        try {
            while (buffStreamNav.ready()) {
                try {
                    String line = buffStreamNav.readLine();
                    if (cacheStreamWriter != null) {
                        cacheStreamWriter.write(line);
                        cacheStreamWriter.write(newline);
                    }
                    String typeField = line.substring(60, line.length());
                    typeField = typeField.trim();

                    if (typeField.equals("RINEX VERSION / TYPE")) {
                        String versionField = line.substring(0, 9).trim();
                        ver_f = Float.parseFloat(versionField);
                        if (!line.substring(20, 21).equals("N")) {
                            // Error if navigation file identifier was not found
                            System.err.println("Navigation file identifier is missing in file " + fileNav.toString() + " header");
                            return 0;
                        } else if (line.substring(5, 7).equals("3.")) {

                            System.out.println("Ver. 3.01");
                            ver = 3;
                        } else if (line.substring(5, 9).equals("2.12")) {

                            System.out.println("Ver. 2.12");
                            ver = 212;
                        } else {

                            System.out.println("Ver. 2.x");
                            ver = 2;
                        }
                    }else if(typeField.equals("PGM / RUN BY / DATE"))
                    {
                        continue;
                    }else if (typeField.equals("COMMENT")){
                        continue;
                    }else if(typeField.equals("IONOSPHERIC CORR"))
                    {
                        String typeField1 = line.substring(0, 4);
                        typeField1 = typeField1.trim();
                        if (typeField1.equals("GPSA")){
                            float a[] = new float[4];
                            sub = line.substring(7, 17).replace('D', 'e');
                            a[0] = Float.parseFloat(sub.trim());

                            sub = line.substring(18, 29).replace('D', 'e');
                            a[1] = Float.parseFloat(sub.trim());

                            sub = line.substring(30, 41).replace('D', 'e');
                            a[2] = Float.parseFloat(sub.trim());

                            sub = line.substring(42, 53).replace('D', 'e');
                            a[3] = Float.parseFloat(sub.trim());

                            if (ionoGps == null) ionoGps = new IonoGps();
                            ionoGps.setAlpha(a);
                        }else if (typeField1.equals("GPSB")) {
                            float b[] = new float[4];

                            sub = line.substring(7, 17).replace('D', 'e');
                            b[0] = Float.parseFloat(sub.trim());

                            sub = line.substring(18, 29).replace('D', 'e');
                            b[1] = Float.parseFloat(sub.trim());

                            sub = line.substring(30, 41).replace('D', 'e');
                            b[2] = Float.parseFloat(sub.trim());

                            sub = line.substring(42, 53).replace('D', 'e');
                            b[3] = Float.parseFloat(sub.trim());

                            if (ionoGps == null) ionoGps = new IonoGps();
                            ionoGps.setBeta(b);
                        }else if (typeField1.equals("GAL")) {
                            float b[] = new float[4];

                            sub = line.substring(7, 17).replace('D', 'e');
                            b[0] = Float.parseFloat(sub.trim());

                            sub = line.substring(18, 29).replace('D', 'e');
                            b[1] = Float.parseFloat(sub.trim());

                            sub = line.substring(30, 41).replace('D', 'e');
                            b[2] = Float.parseFloat(sub.trim());

                            sub = line.substring(42, 53).replace('D', 'e');
                            b[3] = Float.parseFloat(sub.trim());

                            if (ionoGalileo == null) ionoGalileo = new IonoGalileo();
                            ionoGalileo.setBeta(b);
                        }else if (typeField1.equals("BDSA")) {
                            float a[] = new float[4];

                            sub = line.substring(7, 17).replace('D', 'e');
                            a[0] = Float.parseFloat(sub.trim());

                            sub = line.substring(18, 29).replace('D', 'e');
                            a[1] = Float.parseFloat(sub.trim());

                            sub = line.substring(30, 41).replace('D', 'e');
                            a[2] = Float.parseFloat(sub.trim());

                            sub = line.substring(42, 53).replace('D', 'e');
                            a[3] = Float.parseFloat(sub.trim());

                            if (ionoBD == null) ionoBD = new IonoBD();
                            ionoBD.setAlpha(a);
                        }else if (typeField1.equals("BDSB")) {
                            float b[] = new float[4];

                            sub = line.substring(7, 17).replace('D', 'e');
                            b[0] = Float.parseFloat(sub.trim());

                            sub = line.substring(18, 29).replace('D', 'e');
                            b[1] = Float.parseFloat(sub.trim());

                            sub = line.substring(30, 41).replace('D', 'e');
                            b[2] = Float.parseFloat(sub.trim());

                            sub = line.substring(42, 53).replace('D', 'e');
                            b[3] = Float.parseFloat(sub.trim());

                            if (ionoBD == null) ionoBD = new IonoBD();
                            ionoBD.setBeta(b);
                        }
                    }else if(typeField.equals("END OF HEADER"))
                    {
                        return ver;
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } finally {

        }
        return 0;
    }

    /**
     * Read all navigation data
     */
    public void parseDataNavV2() {
        try {

            // Resizable array
            //Navigation.eph = new ArrayList<EphGps>();

            //			int j = 0;

            EphGps eph = null;

            while (buffStreamNav.ready()) {

                String sub;
                char satType = 'G';

                eph = new EphGps();
                addEph(eph);//加到eph列表
                eph.setSatType(satType);

                // read 8 lines
                for (int i = 0; i < 8; i++) {

                    String line = buffStreamNav.readLine();
                    if (cacheStreamWriter != null) {
                        cacheStreamWriter.write(line);
                        cacheStreamWriter.write(newline);
                    }

                    try {

                        int len = line.length();

                        if (len != 0) {

                            if (i == 0) { // LINE 1

                                //Navigation.eph.get(j).refTime = new Time();


                                //Navigation.eph.add(eph);
                                //								addEph(eph);

                                // Get satellite ID
                                sub = line.substring(0, 2).trim();
                                eph.setSatID(Integer.parseInt(sub));

                                // Get and format date and time string
                                String dT = line.substring(2, 22);
                                dT = dT.replace("  ", " 0").trim();
                                dT = "20" + dT;
                                System.out.println(dT);


                                try {
                                    //Time timeEph = new Time(dT);
                                    // Convert String to UNIX standard time in
                                    // milliseconds
                                    //timeEph.msec = Time.dateStringToTime(dT);
                                    //dt 指的是  2020  4  9  2  0  0.0  转换为Time
                                    Time toc = new Time(dT);
                                    eph.setRefTime(toc);
                                    System.out.println(eph.getRefTime().getGpsTime());
                                    System.out.println(eph.getSatID());
                                    eph.setToc(toc.getGpsWeekSec());//将参考时间的 GPS 周秒数设置为 eph 对象的 toc 属性

                                    // sets Iono reference time
                                    if (iono != null && iono.getRefTime() == null)
                                        iono.setRefTime(new Time(dT));

                                } catch (ParseException e) {
                                    System.err.println("Time parsing failed");//电离层参考时间？
                                }

                                sub = line.substring(22, 41).replace('D', 'e');//把D换成e，科学计数法
                                eph.setAf0(Double.parseDouble(sub.trim()));//卫星钟差

                                sub = line.substring(41, 60).replace('D', 'e');
                                eph.setAf1(Double.parseDouble(sub.trim()));//卫星钟偏

                                sub = line.substring(60, len).replace('D', 'e');
                                eph.setAf2(Double.parseDouble(sub.trim()));//卫星钟偏移

                            } else if (i == 1) { // LINE 2

                                sub = line.substring(3, 22).replace('D', 'e');
                                double iode = Double.parseDouble(sub.trim());
                                // TODO check double -> int conversion ?
                                eph.setIode((int) iode);

                                sub = line.substring(22, 41).replace('D', 'e');
                                eph.setCrs(Double.parseDouble(sub.trim()));

                                sub = line.substring(41, 60).replace('D', 'e');
                                eph.setDeltaN(Double.parseDouble(sub.trim()));

                                sub = line.substring(60, len).replace('D', 'e');
                                eph.setM0(Double.parseDouble(sub.trim()));

                            } else if (i == 2) { // LINE 3

                                sub = line.substring(0, 22).replace('D', 'e');
                                eph.setCuc(Double.parseDouble(sub.trim()));

                                sub = line.substring(22, 41).replace('D', 'e');
                                eph.setE(Double.parseDouble(sub.trim()));

                                sub = line.substring(41, 60).replace('D', 'e');
                                eph.setCus(Double.parseDouble(sub.trim()));

                                sub = line.substring(60, len).replace('D', 'e');
                                eph.setRootA(Double.parseDouble(sub.trim()));

                            } else if (i == 3) { // LINE 4

                                sub = line.substring(0, 22).replace('D', 'e');
                                eph.setToe(Double.parseDouble(sub.trim()));

                                System.out.println("Toe:"+eph.getToe());

                                sub = line.substring(22, 41).replace('D', 'e');
                                eph.setCic(Double.parseDouble(sub.trim()));

                                sub = line.substring(41, 60).replace('D', 'e');
                                eph.setOmega0(Double.parseDouble(sub.trim()));

                                sub = line.substring(60, len).replace('D', 'e');
                                eph.setCis(Double.parseDouble(sub.trim()));

                            } else if (i == 4) { // LINE 5

                                sub = line.substring(0, 22).replace('D', 'e');
                                eph.setI0(Double.parseDouble(sub.trim()));

                                sub = line.substring(22, 41).replace('D', 'e');
                                eph.setCrc(Double.parseDouble(sub.trim()));

                                sub = line.substring(41, 60).replace('D', 'e');
                                eph.setOmega(Double.parseDouble(sub.trim()));

                                sub = line.substring(60, len).replace('D', 'e');
                                eph.setOmegaDot(Double.parseDouble(sub.trim()));

                            } else if (i == 5) { // LINE 6

                                sub = line.substring(0, 22).replace('D', 'e');
                                eph.setiDot(Double.parseDouble(sub.trim()));

                                sub = line.substring(22, 41).replace('D', 'e');
                                double L2Code = Double.parseDouble(sub.trim());
                                eph.setL2Code((int) L2Code);

                                sub = line.substring(41, 60).replace('D', 'e');
                                double week = Double.parseDouble(sub.trim());
                                eph.setWeek((int) week);

                                sub = line.substring(60, len).replace('D', 'e');
                                double L2Flag = Double.parseDouble(sub.trim());
                                eph.setL2Flag((int) L2Flag);

                            } else if (i == 6) { // LINE 7

                                sub = line.substring(0, 22).replace('D', 'e');
                                double svAccur = Double.parseDouble(sub.trim());
                                eph.setSvAccur((int) svAccur);

                                sub = line.substring(22, 41).replace('D', 'e');
                                double svHealth = Double.parseDouble(sub.trim());
                                eph.setSvHealth((int) svHealth);

                                sub = line.substring(41, 60).replace('D', 'e');
                                eph.setTgd(Double.parseDouble(sub.trim()));

                                sub = line.substring(60, len).replace('D', 'e');
                                double iodc = Double.parseDouble(sub.trim());
                                eph.setIodc((int) iodc);

                            } else if (i == 7) { // LINE 8

                                sub = line.substring(0, 22).replace('D', 'e');
                                eph.setTom(Double.parseDouble(sub.trim()));

                                if (len > 22) {
                                    sub = line.substring(22, 41).replace('D', 'e');
                                    eph.setFitInt((long) Double.parseDouble(sub.trim()));

                                } else {
                                    eph.setFitInt(0);//拟合区间
                                }
                            }
                        } else {
                            i--;
                        }
                    } catch (NullPointerException e) {
                        // Skip over blank lines
                    }
                }

                // Increment array index
                //				j++;
                // Store the number of ephemerides
                //Navigation.n = j;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    public void parseDataNavV3() {

        try {
            EphGps eph = null;

            while (buffStreamNav.ready()) {

                String sub;
                char satType;

                satType = (char) buffStreamNav.read();
                if (cacheStreamWriter != null) {
                    cacheStreamWriter.write(satType);
                }
                if (satType == 'G' ) {
                    // other than GLONASS and SBAS data
                    // read 8 lines
                    for (int i = 0; i < 8; i++) {
                        String line = buffStreamNav.readLine();
                        if (cacheStreamWriter != null) {
                            cacheStreamWriter.write(line);
                            cacheStreamWriter.write(newline);
                        }
                        try {
                            int len = line.length();
                            if (len != 0) {
                                if (i == 0) { // LINE 1
                                    eph = new EphGps();
                                    addEph(eph);

                                    eph.setSatType(satType);

                                    sub = line.substring(0, 2).trim();
                                    eph.setSatID(Integer.parseInt(sub));

                                    // Get and format date and time string
                                    String dT = line.substring(3, 22);
                                    dT = dT + ".0";
                                    try {
                                        //Time timeEph = new Time(dT);
                                        // Convert String to UNIX standard time in
                                        // milliseconds
                                        //timeEph.msec = Time.dateStringToTime(dT);
                                        Time toc = new Time(dT);
                                        eph.setRefTime(toc);
                                        eph.setToc(toc.getGpsWeekSec());

                                        // sets Iono reference time
                                        if (iono != null && iono.getRefTime() == null)
                                            iono.setRefTime(new Time(dT));

                                    } catch (ParseException e) {
                                        System.err.println("Time parsing failed");
                                    }

                                    sub = line.substring(22, 41).replace('D', 'e');
                                    eph.setAf0(Double.parseDouble(sub.trim()));

                                    sub = line.substring(41, 60).replace('D', 'e');
                                    eph.setAf1(Double.parseDouble(sub.trim()));

                                    sub = line.substring(60, len).replace('D', 'e');
                                    eph.setAf2(Double.parseDouble(sub.trim()));

                                } else if (i == 1) { // LINE 2

                                    sub = line.substring(4, 23).replace('D', 'e');
                                    double iode = Double.parseDouble(sub.trim());
                                    // TODO check double -> int conversion ?
                                    eph.setIode((int) iode);

                                    sub = line.substring(23, 42).replace('D', 'e');
                                    eph.setCrs(Double.parseDouble(sub.trim()));

                                    sub = line.substring(42, 61).replace('D', 'e');
                                    eph.setDeltaN(Double.parseDouble(sub.trim()));

                                    sub = line.substring(61, len).replace('D', 'e');
                                    eph.setM0(Double.parseDouble(sub.trim()));

                                } else if (i == 2) { // LINE 3

                                    sub = line.substring(4, 23).replace('D', 'e');
                                    eph.setCuc(Double.parseDouble(sub.trim()));

                                    sub = line.substring(23, 42).replace('D', 'e');
                                    eph.setE(Double.parseDouble(sub.trim()));

                                    sub = line.substring(42, 61).replace('D', 'e');
                                    eph.setCus(Double.parseDouble(sub.trim()));

                                    sub = line.substring(61, len).replace('D', 'e');
                                    eph.setRootA(Double.parseDouble(sub.trim()));

                                } else if (i == 3) { // LINE 4

                                    sub = line.substring(4, 23).replace('D', 'e');
                                    eph.setToe(Double.parseDouble(sub.trim()));

                                    sub = line.substring(23, 42).replace('D', 'e');
                                    eph.setCic(Double.parseDouble(sub.trim()));

                                    sub = line.substring(42, 61).replace('D', 'e');
                                    eph.setOmega0(Double.parseDouble(sub.trim()));

                                    sub = line.substring(61, len).replace('D', 'e');
                                    eph.setCis(Double.parseDouble(sub.trim()));

                                } else if (i == 4) { // LINE 5

                                    sub = line.substring(4, 23).replace('D', 'e');
                                    eph.setI0(Double.parseDouble(sub.trim()));

                                    sub = line.substring(23, 42).replace('D', 'e');
                                    eph.setCrc(Double.parseDouble(sub.trim()));

                                    sub = line.substring(42, 61).replace('D', 'e');
                                    eph.setOmega(Double.parseDouble(sub.trim()));

                                    sub = line.substring(61, len).replace('D', 'e');
                                    eph.setOmegaDot(Double.parseDouble(sub.trim()));

                                } else if (i == 5) { // LINE 6

                                    sub = line.substring(4, 23).replace('D', 'e');
                                    eph.setiDot(Double.parseDouble(sub.trim()));

                                    sub = line.substring(23, 42).replace('D', 'e');
                                    double L2Code = Double.parseDouble(sub.trim());
                                    eph.setL2Code((int) L2Code);

                                    sub = line.substring(42, 61).replace('D', 'e');
                                    double week = Double.parseDouble(sub.trim());
                                    eph.setWeek((int) week);

                                    sub = line.substring(61, len).replace('D', 'e');
                                    if (!sub.trim().isEmpty()) {
                                        double L2Flag = Double.parseDouble(sub.trim());
                                        eph.setL2Flag((int) L2Flag);
                                    } else {
                                        eph.setL2Flag(0);
                                    }

                                } else if (i == 6) { // LINE 7

                                    sub = line.substring(4, 23).replace('D', 'e');
                                    double svAccur = Double.parseDouble(sub.trim());
                                    eph.setSvAccur((int) svAccur);

                                    sub = line.substring(23, 42).replace('D', 'e');
                                    double svHealth = Double.parseDouble(sub.trim());
                                    eph.setSvHealth((int) svHealth);

                                    sub = line.substring(42, 61).replace('D', 'e');
                                    eph.setTgd(Double.parseDouble(sub.trim()));

                                    sub = line.substring(61, len).replace('D', 'e');
                                    double iodc = Double.parseDouble(sub.trim());
                                    eph.setIodc((int) iodc);

                                } else if (i == 7) { // LINE 8

                                    sub = line.substring(4, 23).replace('D', 'e');
                                    eph.setTom(Double.parseDouble(sub.trim()));

                                    if (line.trim().length() > 22) {
                                        sub = line.substring(23, 42).replace('D', 'e');
                                        //eph.setFitInt(Long.parseLong(sub.trim()));

                                        //Added by Sebastian on 15.01.2018
                                        eph.setFitInt((long) Double.parseDouble(sub.trim()));


                                    } else {
                                        eph.setFitInt(0);
                                    }
                                }
                            } else {
                                i--;
                            }
                        } catch (NullPointerException e) {
                            // Skip over blank lines
                        }
                    }  // End of for
                }else if (satType == 'C' ) {
                    // BDS
                    // read 8 lines
                    for (int i = 0; i < 8; i++) {
                        String line = buffStreamNav.readLine();
                        if (cacheStreamWriter != null) {
                            cacheStreamWriter.write(line);
                            cacheStreamWriter.write(newline);
                        }
                        try {
                            int len = line.length();
                            if (len != 0) {
                                if (i == 0) { // LINE 1
                                    eph = new EphGps();
                                    addEph(eph);

                                    eph.setSatType(satType);

                                    sub = line.substring(0, 2).trim();
                                    eph.setSatID(Integer.parseInt(sub));

                                    // Get and format date and time string
                                    String dT = line.substring(3, 22);
                                    dT = dT + ".0";
                                    try {
                                        //Time timeEph = new Time(dT);
                                        // Convert String to UNIX standard time in
                                        // milliseconds
                                        //timeEph.msec = Time.dateStringToTime(dT);
                                        Time toc = new Time(dT);
                                        eph.setRefTime(toc);
                                        eph.setToc(toc.getGpsWeekSec());

                                        // sets Iono reference time
                                        if (iono != null && iono.getRefTime() == null)
                                            iono.setRefTime(new Time(dT));

                                    } catch (ParseException e) {
                                        System.err.println("Time parsing failed");
                                    }

                                    sub = line.substring(22, 41).replace('D', 'e');
                                    eph.setAf0(Double.parseDouble(sub.trim()));

                                    sub = line.substring(41, 60).replace('D', 'e');
                                    eph.setAf1(Double.parseDouble(sub.trim()));

                                    sub = line.substring(60, len).replace('D', 'e');
                                    eph.setAf2(Double.parseDouble(sub.trim()));

                                } else if (i == 1) { // LINE 2

                                    sub = line.substring(4, 23).replace('D', 'e');
                                    double iode = Double.parseDouble(sub.trim());
                                    // TODO check double -> int conversion ?
                                    eph.setIode((int) iode);

                                    sub = line.substring(23, 42).replace('D', 'e');
                                    eph.setCrs(Double.parseDouble(sub.trim()));

                                    sub = line.substring(42, 61).replace('D', 'e');
                                    eph.setDeltaN(Double.parseDouble(sub.trim()));

                                    sub = line.substring(61, len).replace('D', 'e');
                                    eph.setM0(Double.parseDouble(sub.trim()));

                                } else if (i == 2) { // LINE 3

                                    sub = line.substring(4, 23).replace('D', 'e');
                                    eph.setCuc(Double.parseDouble(sub.trim()));

                                    sub = line.substring(23, 42).replace('D', 'e');
                                    eph.setE(Double.parseDouble(sub.trim()));

                                    sub = line.substring(42, 61).replace('D', 'e');
                                    eph.setCus(Double.parseDouble(sub.trim()));

                                    sub = line.substring(61, len).replace('D', 'e');
                                    eph.setRootA(Double.parseDouble(sub.trim()));

                                } else if (i == 3) { // LINE 4
                                    //北斗周的周内秒
                                    //需要改吗？
                                    sub = line.substring(4, 23).replace('D', 'e');
                                    eph.setToe(Double.parseDouble(sub.trim()));

                                    sub = line.substring(23, 42).replace('D', 'e');
                                    eph.setCic(Double.parseDouble(sub.trim()));

                                    sub = line.substring(42, 61).replace('D', 'e');
                                    eph.setOmega0(Double.parseDouble(sub.trim()));

                                    sub = line.substring(61, len).replace('D', 'e');
                                    eph.setCis(Double.parseDouble(sub.trim()));

                                } else if (i == 4) { // LINE 5

                                    sub = line.substring(4, 23).replace('D', 'e');
                                    eph.setI0(Double.parseDouble(sub.trim()));

                                    sub = line.substring(23, 42).replace('D', 'e');
                                    eph.setCrc(Double.parseDouble(sub.trim()));

                                    sub = line.substring(42, 61).replace('D', 'e');
                                    eph.setOmega(Double.parseDouble(sub.trim()));

                                    sub = line.substring(61, len).replace('D', 'e');
                                    eph.setOmegaDot(Double.parseDouble(sub.trim()));

                                } else if (i == 5) { // LINE 6

                                    sub = line.substring(4, 23).replace('D', 'e');
                                    eph.setiDot(Double.parseDouble(sub.trim()));
                                    //空
                                    sub = line.substring(23, 42).replace('D', 'e');
                                    //北斗周
                                    //需要改吗？
                                    sub = line.substring(42, 61).replace('D', 'e');
                                    double week = Double.parseDouble(sub.trim());
                                    eph.setWeek((int) week);
                                    //空
                                    sub = line.substring(61, len).replace('D', 'e');
                                    if (!sub.trim().isEmpty()) {
                                        double L2Flag = Double.parseDouble(sub.trim());
                                        eph.setL2Flag((int) L2Flag);
                                    } else {
                                        eph.setL2Flag(0);
                                    }

                                } else if (i == 6) { // LINE 7

                                    sub = line.substring(4, 23).replace('D', 'e');
                                    double svAccur = Double.parseDouble(sub.trim());
                                    eph.setSvAccur((int) svAccur);

                                    sub = line.substring(23, 42).replace('D', 'e');
                                    double svHealth = Double.parseDouble(sub.trim());
                                    eph.setSvHealth((int) svHealth);
                                    //B1 和 B3 信号之间的信号延迟
                                    //改
                                    sub = line.substring(42, 61).replace('D', 'e');
                                    eph.setTgd(Double.parseDouble(sub.trim()));
                                    //B2 和 B3 信号之间的信号延迟
                                    //改
                                    sub = line.substring(61, len).replace('D', 'e');
                                    double iodc = Double.parseDouble(sub.trim());
                                    eph.setIodc((int) iodc);

                                } else if (i == 7) { // LINE 8
                                    sub = line.substring(4, 23).replace('D', 'e');
                                    eph.setTom(Double.parseDouble(sub.trim()));
                                    //数据年龄
                                    if (line.trim().length() > 22) {
                                        sub = line.substring(23, 42).replace('D', 'e');
                                        //eph.setFitInt(Long.parseLong(sub.trim()));

                                        //Added by Sebastian on 15.01.2018
                                        eph.setFitInt((long) Double.parseDouble(sub.trim()));
                                    } else {
                                        eph.setFitInt(0);
                                    }
                                }
                            } else {
                                i--;
                            }
                        } catch (NullPointerException e) {
                            // Skip over blank lines
                        }
                    }  // End of for
                } else if(satType == 'E'){
                    // Galileo
                    // read 8 lines
                    for (int i = 0; i < 8; i++) {
                        String line = buffStreamNav.readLine();
                        if (cacheStreamWriter != null) {
                            cacheStreamWriter.write(line);
                            cacheStreamWriter.write(newline);
                        }
                        try {
                            int len = line.length();
                            if (len != 0) {
                                if (i == 0) { // LINE 1
                                    eph = new EphGps();
                                    addEph(eph);

                                    eph.setSatType(satType);

                                    sub = line.substring(0, 2).trim();
                                    eph.setSatID(Integer.parseInt(sub));

                                    // Get and format date and time string
                                    String dT = line.substring(3, 22);
                                    dT = dT + ".0";
                                    try {
                                        //Time timeEph = new Time(dT);
                                        // Convert String to UNIX standard time in
                                        // milliseconds
                                        //timeEph.msec = Time.dateStringToTime(dT);
                                        Time toc = new Time(dT);
                                        eph.setRefTime(toc);
                                        eph.setToc(toc.getGpsWeekSec());

                                        // sets Iono reference time
                                        if (iono != null && iono.getRefTime() == null)
                                            iono.setRefTime(new Time(dT));

                                    } catch (ParseException e) {
                                        System.err.println("Time parsing failed");
                                    }

                                    sub = line.substring(22, 41).replace('D', 'e');
                                    eph.setAf0(Double.parseDouble(sub.trim()));

                                    sub = line.substring(41, 60).replace('D', 'e');
                                    eph.setAf1(Double.parseDouble(sub.trim()));

                                    sub = line.substring(60, len).replace('D', 'e');
                                    eph.setAf2(Double.parseDouble(sub.trim()));

                                } else if (i == 1) { // LINE 2

                                    sub = line.substring(4, 23).replace('D', 'e');
                                    double iode = Double.parseDouble(sub.trim());
                                    // TODO check double -> int conversion ?
                                    eph.setIode((int) iode);

                                    sub = line.substring(23, 42).replace('D', 'e');
                                    eph.setCrs(Double.parseDouble(sub.trim()));

                                    sub = line.substring(42, 61).replace('D', 'e');
                                    eph.setDeltaN(Double.parseDouble(sub.trim()));

                                    sub = line.substring(61, len).replace('D', 'e');
                                    eph.setM0(Double.parseDouble(sub.trim()));

                                } else if (i == 2) { // LINE 3

                                    sub = line.substring(4, 23).replace('D', 'e');
                                    eph.setCuc(Double.parseDouble(sub.trim()));

                                    sub = line.substring(23, 42).replace('D', 'e');
                                    eph.setE(Double.parseDouble(sub.trim()));

                                    sub = line.substring(42, 61).replace('D', 'e');
                                    eph.setCus(Double.parseDouble(sub.trim()));

                                    sub = line.substring(61, len).replace('D', 'e');
                                    eph.setRootA(Double.parseDouble(sub.trim()));

                                } else if (i == 3) { // LINE 4

                                    sub = line.substring(4, 23).replace('D', 'e');
                                    eph.setToe(Double.parseDouble(sub.trim()));

                                    sub = line.substring(23, 42).replace('D', 'e');
                                    eph.setCic(Double.parseDouble(sub.trim()));

                                    sub = line.substring(42, 61).replace('D', 'e');
                                    eph.setOmega0(Double.parseDouble(sub.trim()));

                                    sub = line.substring(61, len).replace('D', 'e');
                                    eph.setCis(Double.parseDouble(sub.trim()));

                                } else if (i == 4) { // LINE 5

                                    sub = line.substring(4, 23).replace('D', 'e');
                                    eph.setI0(Double.parseDouble(sub.trim()));

                                    sub = line.substring(23, 42).replace('D', 'e');
                                    eph.setCrc(Double.parseDouble(sub.trim()));

                                    sub = line.substring(42, 61).replace('D', 'e');
                                    eph.setOmega(Double.parseDouble(sub.trim()));

                                    sub = line.substring(61, len).replace('D', 'e');
                                    eph.setOmegaDot(Double.parseDouble(sub.trim()));

                                } else if (i == 5) { // LINE 6

                                    sub = line.substring(4, 23).replace('D', 'e');
                                    eph.setiDot(Double.parseDouble(sub.trim()));
                                    //Galileo卫星系统中导航数据记录的数据源信息（Data Sources），以整数形式表示，它描述了各个位的含义及其对应的信号源。
                                    //要改
                                    //需要这个吗
                                    sub = line.substring(23, 42).replace('D', 'e');

                                    sub = line.substring(42, 61).replace('D', 'e');
                                    double week = Double.parseDouble(sub.trim());
                                    eph.setWeek((int) week);

                                    //空
                                    sub = line.substring(61, len).replace('D', 'e');
                                    if (!sub.trim().isEmpty()) {
                                        double L2Flag = Double.parseDouble(sub.trim());
                                        eph.setL2Flag((int) L2Flag);
                                    } else {
                                        eph.setL2Flag(0);
                                    }

                                } else if (i == 6) { // LINE 7

                                    sub = line.substring(4, 23).replace('D', 'e');
                                    double svAccur = Double.parseDouble(sub.trim());
                                    eph.setSvAccur((int) svAccur);

                                    sub = line.substring(23, 42).replace('D', 'e');
                                    double svHealth = Double.parseDouble(sub.trim());
                                    eph.setSvHealth((int) svHealth);
                                    //E5a 和 E1 信号之间的信号延迟
                                    //要改
                                    sub = line.substring(42, 61).replace('D', 'e');
                                    eph.setTgd(Double.parseDouble(sub.trim()));
                                    //E5b 和 E1 信号之间的信号延迟
                                    //要改
                                    sub = line.substring(61, len).replace('D', 'e');
                                    double iodc = Double.parseDouble(sub.trim());
                                    eph.setIodc((int) iodc);

                                } else if (i == 7) { // LINE 8
                                    sub = line.substring(4, 23).replace('D', 'e');
                                    eph.setTom(Double.parseDouble(sub.trim()));

                                    //空
                                    if (line.trim().length() > 22) {
                                        sub = line.substring(23, 42).replace('D', 'e');
                                        //eph.setFitInt(Long.parseLong(sub.trim()));

                                        //Added by Sebastian on 15.01.2018
                                        eph.setFitInt((long) Double.parseDouble(sub.trim()));
                                    } else {
                                        eph.setFitInt(0);
                                    }
                                }
                            } else {
                                i--;
                            }


                        } catch (NullPointerException e) {
                            // Skip over blank lines
                        }
                    }  // End of for
                }else if (satType == 'R') {
                // In case of GLONASS data

                    for (int i = 0; i < 4; i++) {
                        String line = buffStreamNav.readLine();
                        if (cacheStreamWriter != null) {
                            cacheStreamWriter.write(line);
                            cacheStreamWriter.write(newline);
                        }
                        try {
                            int len = line.length();

                            if (len != 0) {
                                if (i == 0) { // LINE 1
                                    eph = new EphGps();
                                    addEph(eph);
                                    count_glonass++;
                                    eph.setSatType(satType);

                                    // Get satellite ID
                                    sub = line.substring(0, 2).trim();
                                    eph.setSatID(Integer.parseInt(sub));

                                    // Get and format date and time string
                                    String dT = line.substring(3, 22);
                                    dT = dT + ".0";
                                    try {
                                        Time dtoc = new Time(dT);
                                        eph.setRefTime(dtoc);
                                        int toc = dtoc.getGpsWeekSec();//周内秒
                                        eph.setToc(toc);

                                        int week = dtoc.getGpsWeek();//GPS周
                                        eph.setWeek(week);

                                        double toe = toc;
                                        eph.setToe(toe);

                                        // sets Iono reference time
                                        if (iono != null && iono.getRefTime() == null)
                                            iono.setRefTime(new Time(dT));

                                    } catch (ParseException e) {
                                        System.err.println("Time parsing failed");
                                    }

                                    /* TauN */
                                    sub = line.substring(22, 41).replace('D', 'e');
                                    //										System.out.println(sub);
                                    eph.setTauN(Float.parseFloat(sub.trim()));

                                    /* GammaN */
                                    sub = line.substring(41, 60).replace('D', 'e');
                                    //										System.out.println(sub);
                                    eph.setGammaN(Float.parseFloat(sub.trim()));

                                    /* tb */
                                    sub = line.substring(60, len).replace('D', 'e');
                                    //										System.out.println("tb: " + sub);

                                    /* tb is a time interval within the current day (UTC + 3 hours)*/
                                    double tb = Double.parseDouble(sub.trim());
                                    double tk = tb - 10800;
                                    //										System.out.println("tk: " + tk);
                                    eph.settk(tk);


                                    //										eph.settb(Double.parseDouble(sub.trim()));

                                } else if (i == 1) { // LINE 2

                                    /* X: satellite X coordinate at ephemeris reference time [m] */
                                    sub = line.substring(4, 23).replace('D', 'e');
                                    //										System.out.println(sub);
                                    eph.setX(Double.parseDouble(sub.trim()) * 1e3);

                                    /* Xv: satellite velocity along X at ephemeris reference time [m/s] */
                                    sub = line.substring(23, 42).replace('D', 'e');
                                    //										System.out.println(sub);
                                    eph.setXv(Double.parseDouble(sub.trim()) * 1e3);

                                    /* Xa: acceleration due to lunar-solar gravitational perturbation along X at ephemeris reference time [m/s^2] */
                                    sub = line.substring(42, 61).replace('D', 'e');
                                    //										System.out.println(sub);
                                    eph.setXa(Double.parseDouble(sub.trim()) * 1e3);

                                    /* Bn */
                                    sub = line.substring(61, len).replace('D', 'e');
                                    //										System.out.println(sub);
                                    eph.setBn(Double.parseDouble(sub.trim()));

                                } else if (i == 2) { // LINE 3

                                    /* Y: satellite Y coordinate at ephemeris reference time [m] */
                                    sub = line.substring(4, 23).replace('D', 'e');
                                    //										System.out.println(sub);
                                    eph.setY(Double.parseDouble(sub.trim()) * 1e3);

                                    /* Yv: satellite velocity along Y at ephemeris reference time [m/s] */
                                    sub = line.substring(23, 42).replace('D', 'e');
                                    //										System.out.println(sub);
                                    eph.setYv(Double.parseDouble(sub.trim()) * 1e3);

                                    /* Ya: acceleration due to lunar-solar gravitational perturbation along Y at ephemeris reference time [m/s^2] */
                                    sub = line.substring(42, 61).replace('D', 'e');
                                    //										System.out.println(sub);
                                    eph.setYa(Double.parseDouble(sub.trim()) * 1e3);

                                    /* freq_num */
                                    sub = line.substring(61, len).replace('D', 'e');
                                    //										System.out.println(sub);
                                    eph.setfreq_num((int) Double.parseDouble(sub.trim()));

                                } else if (i == 3) { // LINE 4

                                    /* Z: satellite Z coordinate at ephemeris reference time [m] */
                                    sub = line.substring(4, 23).replace('D', 'e');
                                    //										System.out.println(sub);
                                    eph.setZ(Double.parseDouble(sub.trim()) * 1e3);

                                    /* Zv: satellite velocity along Z at ephemeris reference time [m/s] */
                                    sub = line.substring(23, 42).replace('D', 'e');
                                    //										System.out.println(sub);
                                    eph.setZv(Double.parseDouble(sub.trim()) * 1e3);

                                    /* Za: acceleration due to lunar-solar gravitational perturbation along Z at ephemeris reference time [m/s^2]  */
                                    sub = line.substring(42, 61).replace('D', 'e');
                                    //										System.out.println(sub);
                                    eph.setZa(Double.parseDouble(sub.trim()) * 1e3);

                                    /* En */
                                    sub = line.substring(61, len).replace('D', 'e');
                                    //										System.out.println(sub);
                                    //										eph.setEn(Long.parseLong(sub.trim()));
                                    eph.setEn(Double.parseDouble(sub.trim()));


                                } // End of if

                            } else {
                                i--;
                            }
                            //		}  // End of if


                        } catch (NullPointerException e) {
                            // Skip over blank lines
                        }

                    } // End of for

                } else { //SBAS data

                    for (int i = 0; i < 4; i++) {
                        String line = buffStreamNav.readLine();
                        if (cacheStreamWriter != null) {
                            cacheStreamWriter.write(line);
                            cacheStreamWriter.write(newline);
                        }
                    }

                }  // End of GLO if


                // Increment array index
                //				j++;
                // Store the number of ephemerides
                //Navigation.n = j;
            } // End of while

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }


    }

    private double gpsToUnixTime(Time toc, int tow) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @param unixTime
     * @param satID
     * @return Reference ephemeris set for given time and satellite查找最接近的GPS星历数据
     */
    public EphGps findEph(long unixTime, int satID, char satType) {

        long dt = 0;
        long dtMin = 0;
        long dtMax = 0;
        long delta = 0;
        EphGps refEph = null;

        //long gpsTime = (new Time(unixTime)).getGpsTime();

        for (int i = 0; i < eph.size(); i++) {
            // Find ephemeris sets for given satellite
            if (eph.get(i).getSatID() == satID && eph.get(i).getSatType() == satType) {
                // Consider BeiDou time (BDT) for BeiDou satellites (14 sec difference wrt GPS time)
                if (satType == 'C') {
                    delta = 14000;
                    unixTime = unixTime - delta;
                }
                // Compare current time and ephemeris reference time
                dt = Math.abs(eph.get(i).getRefTime().getMsec() - unixTime /*getGpsTime() - gpsTime*/) / 1000;

                System.out.println("G"+eph.get(i).getSatID()+"   "+eph.get(i).getRefTime().getMsec()+"   "+unixTime);
                // If it's the first round, set the minimum time difference and
                // select the first ephemeris set candidate; if the current ephemeris set
                // is closer in time than the previous candidate, select new candidate
                //查找星历数组中距离观测时间最近的星历
                if (refEph == null || dt < dtMin) {
                    dtMin = dt;
                    refEph = eph.get(i);
                }
            }
        }

        if (refEph == null)
            return null;

        if (refEph.getSvHealth() != 0) {
            //如果找到的星历数据的健康状态不为0（表示卫星不健康）
            return null;
//            return EphGps.UnhealthyEph;
        }

        //maximum allowed interval from ephemeris reference time
        long fitInterval = refEph.getFitInt();

        if (fitInterval != 0) {
            dtMax = fitInterval * 3600 / 2;
        } else {
            switch (refEph.getSatType()) {
                case 'R':
                    dtMax = 950;
                case 'J':
                    dtMax = 3600;
                default:
                    dtMax = 7200;
            }
        }
        if (dtMin > dtMax) {
            //如果找到的最小时间差异超过了允许的最大时间差异
            refEph = null;
        }

        return refEph;
    }

    public int getEphSize() {
        return eph.size();
    }

    public void addEph(EphGps eph) {
        this.eph.add(eph);
        count++;
    }



    public boolean isTimestampInEpocsRange(long unixTime) {
        return eph.size() > 0 /*&&
                eph.get(0).getRefTime().getMsec() <= unixTime *//*&&
		unixTime <= eph.get(eph.size()-1).getRefTime().getMsec() missing interval +epochInterval*/;
    }


    /* (non-Javadoc)
     * @see org.gogpsproject.NavigationProducer#getGpsSatPosition(long, int, double)
     */
    public String getFileName() {
        if (fileNav == null)
            return null;
        else
            return fileNav.getName();
    }


    /**
     *
     * @param unixtime
     * @param satID
     * @param satType
     * @param eph
     * @return
     */
    /**
     *
     * @param unixTime  时间戳，，1970.1.1开始  ms   可以转为gpstime
     * @param range  伪距
     * @param satID
     * @param satType
     * @param receiverClockError  接收机钟差，一般定为0.0
     * @return
     */
    public SatellitePosition getSatPositionAndVelocities(long unixTime,double range, int satID, char satType, double receiverClockError) {

        //寻找最近的星历
        EphGps eph = findEph(unixTime, satID, satType);

        if (eph == null) {
            Log.e(TAG, "getSatPositionAndVelocities: Ephemeris failed to load..." );
            return null;
        }

        //为什么不直接返回null？？？？？？
        if (eph.equals(EphGps.UnhealthyEph))
            return SatellitePosition.UnhealthySat;



        SatellitePosition sp = computeSatPositionAndVelocities(unixTime, range,satID, satType, eph, receiverClockError);

        return sp;
    }

    @Override
    public IonoGps getIonoGps() {
        return this.iono;
    }

    @Override
    public IonoGalileo getIonoGalileo() {
        return null;
    }
}





