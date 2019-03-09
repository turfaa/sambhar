package com.google.zxing.client.result;

public final class ExpandedProductResultParser extends ResultParser {
    /* JADX WARNING: Removed duplicated region for block: B:128:0x0207  */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x0256  */
    /* JADX WARNING: Removed duplicated region for block: B:147:0x0254  */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x0252  */
    /* JADX WARNING: Removed duplicated region for block: B:145:0x0250  */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x024e  */
    /* JADX WARNING: Removed duplicated region for block: B:143:0x024c  */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x024a  */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x023d  */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x0235  */
    /* JADX WARNING: Removed duplicated region for block: B:137:0x022c  */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x0214  */
    /* JADX WARNING: Removed duplicated region for block: B:128:0x0207  */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x0256  */
    /* JADX WARNING: Removed duplicated region for block: B:147:0x0254  */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x0252  */
    /* JADX WARNING: Removed duplicated region for block: B:145:0x0250  */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x024e  */
    /* JADX WARNING: Removed duplicated region for block: B:143:0x024c  */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x024a  */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x023d  */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x0235  */
    /* JADX WARNING: Removed duplicated region for block: B:137:0x022c  */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x0214  */
    /* JADX WARNING: Removed duplicated region for block: B:128:0x0207  */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x0256  */
    /* JADX WARNING: Removed duplicated region for block: B:147:0x0254  */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x0252  */
    /* JADX WARNING: Removed duplicated region for block: B:145:0x0250  */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x024e  */
    /* JADX WARNING: Removed duplicated region for block: B:143:0x024c  */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x024a  */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x023d  */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x0235  */
    /* JADX WARNING: Removed duplicated region for block: B:137:0x022c  */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x0214  */
    /* JADX WARNING: Missing block: B:129:0x020b, code skipped:
            r3 = r20;
            r15 = r21;
     */
    /* JADX WARNING: Missing block: B:130:0x020f, code skipped:
            r14 = r22;
     */
    /* JADX WARNING: Missing block: B:131:0x0211, code skipped:
            r2 = null;
     */
    /* JADX WARNING: Missing block: B:138:0x0232, code skipped:
            r3 = r20;
     */
    /* JADX WARNING: Missing block: B:141:0x0244, code skipped:
            r12 = r2;
            r3 = r20;
            r15 = r21;
     */
    public com.google.zxing.client.result.ExpandedProductParsedResult parse(com.google.zxing.Result r24) {
        /*
        r23 = this;
        r0 = r24.getBarcodeFormat();
        r1 = com.google.zxing.BarcodeFormat.RSS_EXPANDED;
        r2 = 0;
        if (r0 == r1) goto L_0x000a;
    L_0x0009:
        return r2;
    L_0x000a:
        r4 = com.google.zxing.client.result.ResultParser.getMassagedText(r24);
        r0 = new java.util.HashMap;
        r0.<init>();
        r5 = r2;
        r6 = r5;
        r7 = r6;
        r8 = r7;
        r9 = r8;
        r10 = r9;
        r11 = r10;
        r12 = r11;
        r13 = r12;
        r14 = r13;
        r15 = r14;
        r16 = r15;
        r17 = r16;
        r3 = 0;
    L_0x0023:
        r1 = r4.length();
        if (r3 >= r1) goto L_0x0258;
    L_0x0029:
        r1 = findAIvalue(r3, r4);
        if (r1 != 0) goto L_0x0030;
    L_0x002f:
        return r2;
    L_0x0030:
        r18 = r1.length();
        r19 = 2;
        r18 = r18 + 2;
        r3 = r3 + r18;
        r2 = findValue(r3, r4);
        r18 = r2.length();
        r3 = r3 + r18;
        r18 = -1;
        r20 = r3;
        r3 = r1.hashCode();
        r21 = r15;
        r15 = 1570; // 0x622 float:2.2E-42 double:7.757E-321;
        r22 = r14;
        r14 = 3;
        if (r3 == r15) goto L_0x01f9;
    L_0x0055:
        r15 = 1572; // 0x624 float:2.203E-42 double:7.767E-321;
        if (r3 == r15) goto L_0x01ef;
    L_0x0059:
        r15 = 1574; // 0x626 float:2.206E-42 double:7.777E-321;
        if (r3 == r15) goto L_0x01e5;
    L_0x005d:
        switch(r3) {
            case 1536: goto L_0x01db;
            case 1537: goto L_0x01d1;
            default: goto L_0x0060;
        };
    L_0x0060:
        switch(r3) {
            case 1567: goto L_0x01c7;
            case 1568: goto L_0x01bd;
            default: goto L_0x0063;
        };
    L_0x0063:
        switch(r3) {
            case 1567966: goto L_0x01b3;
            case 1567967: goto L_0x01a8;
            case 1567968: goto L_0x019d;
            case 1567969: goto L_0x0191;
            case 1567970: goto L_0x0185;
            case 1567971: goto L_0x0179;
            case 1567972: goto L_0x016d;
            case 1567973: goto L_0x0161;
            case 1567974: goto L_0x0155;
            case 1567975: goto L_0x0149;
            default: goto L_0x0066;
        };
    L_0x0066:
        switch(r3) {
            case 1568927: goto L_0x013d;
            case 1568928: goto L_0x0131;
            case 1568929: goto L_0x0125;
            case 1568930: goto L_0x0119;
            case 1568931: goto L_0x010d;
            case 1568932: goto L_0x0101;
            case 1568933: goto L_0x00f5;
            case 1568934: goto L_0x00e9;
            case 1568935: goto L_0x00dd;
            case 1568936: goto L_0x00d1;
            default: goto L_0x0069;
        };
    L_0x0069:
        switch(r3) {
            case 1575716: goto L_0x00c5;
            case 1575717: goto L_0x00b9;
            case 1575718: goto L_0x00ad;
            case 1575719: goto L_0x00a1;
            default: goto L_0x006c;
        };
    L_0x006c:
        switch(r3) {
            case 1575747: goto L_0x0095;
            case 1575748: goto L_0x0089;
            case 1575749: goto L_0x007d;
            case 1575750: goto L_0x0071;
            default: goto L_0x006f;
        };
    L_0x006f:
        goto L_0x0203;
    L_0x0071:
        r3 = "3933";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x0079:
        r3 = 34;
        goto L_0x0204;
    L_0x007d:
        r3 = "3932";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x0085:
        r3 = 33;
        goto L_0x0204;
    L_0x0089:
        r3 = "3931";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x0091:
        r3 = 32;
        goto L_0x0204;
    L_0x0095:
        r3 = "3930";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x009d:
        r3 = 31;
        goto L_0x0204;
    L_0x00a1:
        r3 = "3923";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x00a9:
        r3 = 30;
        goto L_0x0204;
    L_0x00ad:
        r3 = "3922";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x00b5:
        r3 = 29;
        goto L_0x0204;
    L_0x00b9:
        r3 = "3921";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x00c1:
        r3 = 28;
        goto L_0x0204;
    L_0x00c5:
        r3 = "3920";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x00cd:
        r3 = 27;
        goto L_0x0204;
    L_0x00d1:
        r3 = "3209";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x00d9:
        r3 = 26;
        goto L_0x0204;
    L_0x00dd:
        r3 = "3208";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x00e5:
        r3 = 25;
        goto L_0x0204;
    L_0x00e9:
        r3 = "3207";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x00f1:
        r3 = 24;
        goto L_0x0204;
    L_0x00f5:
        r3 = "3206";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x00fd:
        r3 = 23;
        goto L_0x0204;
    L_0x0101:
        r3 = "3205";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x0109:
        r3 = 22;
        goto L_0x0204;
    L_0x010d:
        r3 = "3204";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x0115:
        r3 = 21;
        goto L_0x0204;
    L_0x0119:
        r3 = "3203";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x0121:
        r3 = 20;
        goto L_0x0204;
    L_0x0125:
        r3 = "3202";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x012d:
        r3 = 19;
        goto L_0x0204;
    L_0x0131:
        r3 = "3201";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x0139:
        r3 = 18;
        goto L_0x0204;
    L_0x013d:
        r3 = "3200";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x0145:
        r3 = 17;
        goto L_0x0204;
    L_0x0149:
        r3 = "3109";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x0151:
        r3 = 16;
        goto L_0x0204;
    L_0x0155:
        r3 = "3108";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x015d:
        r3 = 15;
        goto L_0x0204;
    L_0x0161:
        r3 = "3107";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x0169:
        r3 = 14;
        goto L_0x0204;
    L_0x016d:
        r3 = "3106";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x0175:
        r3 = 13;
        goto L_0x0204;
    L_0x0179:
        r3 = "3105";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x0181:
        r3 = 12;
        goto L_0x0204;
    L_0x0185:
        r3 = "3104";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x018d:
        r3 = 11;
        goto L_0x0204;
    L_0x0191:
        r3 = "3103";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x0199:
        r3 = 10;
        goto L_0x0204;
    L_0x019d:
        r3 = "3102";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x01a5:
        r3 = 9;
        goto L_0x0204;
    L_0x01a8:
        r3 = "3101";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x01b0:
        r3 = 8;
        goto L_0x0204;
    L_0x01b3:
        r3 = "3100";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x01bb:
        r3 = 7;
        goto L_0x0204;
    L_0x01bd:
        r3 = "11";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x01c5:
        r3 = 3;
        goto L_0x0204;
    L_0x01c7:
        r3 = "10";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x01cf:
        r3 = 2;
        goto L_0x0204;
    L_0x01d1:
        r3 = "01";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x01d9:
        r3 = 1;
        goto L_0x0204;
    L_0x01db:
        r3 = "00";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x01e3:
        r3 = 0;
        goto L_0x0204;
    L_0x01e5:
        r3 = "17";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x01ed:
        r3 = 6;
        goto L_0x0204;
    L_0x01ef:
        r3 = "15";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x01f7:
        r3 = 5;
        goto L_0x0204;
    L_0x01f9:
        r3 = "13";
        r3 = r1.equals(r3);
        if (r3 == 0) goto L_0x0203;
    L_0x0201:
        r3 = 4;
        goto L_0x0204;
    L_0x0203:
        r3 = -1;
    L_0x0204:
        switch(r3) {
            case 0: goto L_0x0256;
            case 1: goto L_0x0254;
            case 2: goto L_0x0252;
            case 3: goto L_0x0250;
            case 4: goto L_0x024e;
            case 5: goto L_0x024c;
            case 6: goto L_0x024a;
            case 7: goto L_0x023d;
            case 8: goto L_0x023d;
            case 9: goto L_0x023d;
            case 10: goto L_0x023d;
            case 11: goto L_0x023d;
            case 12: goto L_0x023d;
            case 13: goto L_0x023d;
            case 14: goto L_0x023d;
            case 15: goto L_0x023d;
            case 16: goto L_0x023d;
            case 17: goto L_0x0235;
            case 18: goto L_0x0235;
            case 19: goto L_0x0235;
            case 20: goto L_0x0235;
            case 21: goto L_0x0235;
            case 22: goto L_0x0235;
            case 23: goto L_0x0235;
            case 24: goto L_0x0235;
            case 25: goto L_0x0235;
            case 26: goto L_0x0235;
            case 27: goto L_0x022c;
            case 28: goto L_0x022c;
            case 29: goto L_0x022c;
            case 30: goto L_0x022c;
            case 31: goto L_0x0214;
            case 32: goto L_0x0214;
            case 33: goto L_0x0214;
            case 34: goto L_0x0214;
            default: goto L_0x0207;
        };
    L_0x0207:
        r3 = 0;
        r0.put(r1, r2);
    L_0x020b:
        r3 = r20;
        r15 = r21;
    L_0x020f:
        r14 = r22;
    L_0x0211:
        r2 = 0;
        goto L_0x0023;
    L_0x0214:
        r3 = r2.length();
        r15 = 4;
        if (r3 >= r15) goto L_0x021d;
    L_0x021b:
        r3 = 0;
        return r3;
    L_0x021d:
        r3 = 0;
        r15 = r2.substring(r14);
        r3 = 0;
        r17 = r2.substring(r3, r14);
        r16 = r1.substring(r14);
        goto L_0x0232;
    L_0x022c:
        r3 = 0;
        r16 = r1.substring(r14);
        r15 = r2;
    L_0x0232:
        r3 = r20;
        goto L_0x020f;
    L_0x0235:
        r3 = 0;
        r13 = "LB";
        r14 = r1.substring(r14);
        goto L_0x0244;
    L_0x023d:
        r3 = 0;
        r13 = "KG";
        r14 = r1.substring(r14);
    L_0x0244:
        r12 = r2;
        r3 = r20;
        r15 = r21;
        goto L_0x0211;
    L_0x024a:
        r11 = r2;
        goto L_0x020b;
    L_0x024c:
        r10 = r2;
        goto L_0x020b;
    L_0x024e:
        r9 = r2;
        goto L_0x020b;
    L_0x0250:
        r8 = r2;
        goto L_0x020b;
    L_0x0252:
        r7 = r2;
        goto L_0x020b;
    L_0x0254:
        r5 = r2;
        goto L_0x020b;
    L_0x0256:
        r6 = r2;
        goto L_0x020b;
    L_0x0258:
        r22 = r14;
        r21 = r15;
        r1 = new com.google.zxing.client.result.ExpandedProductParsedResult;
        r3 = r1;
        r18 = r0;
        r3.<init>(r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18);
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.zxing.client.result.ExpandedProductResultParser.parse(com.google.zxing.Result):com.google.zxing.client.result.ExpandedProductParsedResult");
    }

    private static String findAIvalue(int i, String str) {
        if (str.charAt(i) != '(') {
            return null;
        }
        String substring = str.substring(i + 1);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i2 = 0; i2 < substring.length(); i2++) {
            char charAt = substring.charAt(i2);
            if (charAt == ')') {
                return stringBuilder.toString();
            }
            if (charAt < '0' || charAt > '9') {
                return null;
            }
            stringBuilder.append(charAt);
        }
        return stringBuilder.toString();
    }

    private static String findValue(int i, String str) {
        StringBuilder stringBuilder = new StringBuilder();
        String substring = str.substring(i);
        for (int i2 = 0; i2 < substring.length(); i2++) {
            char charAt = substring.charAt(i2);
            if (charAt == '(') {
                if (findAIvalue(i2, substring) != null) {
                    break;
                }
                stringBuilder.append('(');
            } else {
                stringBuilder.append(charAt);
            }
        }
        return stringBuilder.toString();
    }
}
