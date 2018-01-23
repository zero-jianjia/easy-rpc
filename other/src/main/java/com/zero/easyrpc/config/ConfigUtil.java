
package com.zero.easyrpc.config;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * 
 * Config tools
 *
 * @author fishermen
 * @version V1.0 created at: 2013-5-27
 */

public class ConfigUtil {

    /**
     * export fomart: protocol1:port1,protocol2:port2
     * 
     * @param export
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Integer> parseExport(String export) {
//        if (StringUtils.isBlank(export)) {
//            return Collections.emptyMap();
//        }
        Map<String, Integer> pps = new HashMap<String, Integer>();
//        String[] protocolAndPorts = MotanConstants.COMMA_SPLIT_PATTERN.split(export);
//        for (String pp : protocolAndPorts) {
//            if (StringUtils.isBlank(pp)) {
//                continue;
//            }
//            String[] ppDetail = pp.split(":");
//            if (ppDetail.length == 2) {
//                pps.put(ppDetail[0], Integer.parseInt(ppDetail[1]));
//            } else if (ppDetail.length == 1) {
//                if (MotanConstants.PROTOCOL_INJVM.equals(ppDetail[0])) {
//                    pps.put(ppDetail[0], MotanConstants.DEFAULT_INT_VALUE);
//                } else {
//                    int port = MathUtil.parseInt(ppDetail[0], 0);
//                    if (port <= 0) {
//                        throw new MotanServiceException("Export is malformed :" + export);
//                    } else {
//                        pps.put(MotanConstants.PROTOCOL_MOTAN, port);
//                    }
//                }
//
//            } else {
//                throw new MotanServiceException("Export is malformed :" + export);
//            }
//        }
        return pps;
    }

    public static String extractProtocols(String export) {
        Map<String, Integer> protocols = parseExport(export);
        StringBuilder sb = new StringBuilder(16);
        for (String p : protocols.keySet()) {
//            sb.append(p).append(MotanConstants.COMMA_SEPARATOR);
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();

    }
}
