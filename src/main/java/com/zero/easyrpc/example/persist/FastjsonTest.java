package com.zero.easyrpc.example.persist;

import java.io.File;
import java.io.IOException;
import java.util.List;


import com.alibaba.fastjson.JSON;
import com.zero.easyrpc.common.rpc.MetricsReporter;
import com.zero.easyrpc.common.utils.PersistUtils;

public class FastjsonTest {
	
	
	private static String storePathRootDir = System.getProperty("user.home") + File.separator + "test" + File.separator + "historyMetrics.json";
	public static void main(String[] args) throws IOException {
		
		
		String existStr = PersistUtils.file2String(storePathRootDir);
		
		List<MetricsReporter> metricsReporters = JSON.parseArray(existStr.trim(), MetricsReporter.class);
		
		for(MetricsReporter s :metricsReporters){
			System.out.println(s);
		}
		
	}

}
