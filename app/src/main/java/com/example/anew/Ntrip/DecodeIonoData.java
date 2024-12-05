package com.example.anew.Ntrip;

public class DecodeIonoData {

    public static Iono iono= new Iono();//用于存储电离层改正数

    public static long getbitu(byte[] buff, int pos, int len) {
        long bits = 0L;
        int i;
        for (i = pos; i < pos + len; i++)
            bits = (bits << 1) + ((buff[i / 8] >> (7 - i % 8)) & 1L);
        return bits;
    }

    private static double getbitg(byte[] buff, int pos, int len) {
        double value = getbitu(buff, pos + 1, len - 1);
        return getbitu(buff, pos, 1) != 0 ? -value : value;
    }

    private static int getbits(byte[] buff, int pos, int len) {
        long  bits = getbitu(buff, pos, len);
        if (len <= 0 || 32 <= len || (bits & (1L << (len - 1))) == 0) return (int) bits;
        return (int) (bits | (~0L << len));
    }


    public static void decodeionodata(byte[] data) {
        int type = (int)getbitu(data, 0, 12);
        if(type == 1264){
           int i=12+3+8;
           iono.getEphTime().setMsec((long)(getbitu(data, i,20)*1E3));
           iono.getEphTime().setFraction(getbitu(data, i,20)*1E3-(long)(getbitu(data, i,20)*1E3));
           i=i+20+4+1;
           iono.setIod(getbitu(data, i,4));
           i=i+4+16+4;
           iono.setQual(getbitu(data, i,9));
           i=i+9;
           iono.setLayers((int)getbitu(data, i,2));
           i=i+2;
           for (int lay=0;lay<iono.getLayers();lay++) {
               iono.setHeight(getbitu(data, i, 8),lay);
               i = i + 8;
               iono.setDegree((int) getbitu(data, i, 4),lay);
               i = i + 4;
               iono.setOrder((int) getbitu(data, i, 4),lay);
               i = i + 4;
               for (int n = 0; n < iono.getDegree(lay) + 1; n++) {
                   for (int m = 0; m < Math.min(n, iono.getOrder(lay)) + 1; m++) {
                       iono.setCosineC1(getbits(data, i, 16), n, m);
                       i = i + 16;
                   }
               }
               for (int n = 1; n < iono.getDegree(lay) + 1; n++) {
                   for (int m = 1; m < Math.min(n, iono.getOrder(lay)) + 1; m++) {
                       iono.setSineS1(getbits(data, i, 16), n, m);
                       i = i + 16;
                   }
               }
           }
        }
        else{

        }
    }
}
