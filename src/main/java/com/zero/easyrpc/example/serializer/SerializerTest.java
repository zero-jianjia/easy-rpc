package com.zero.easyrpc.example.serializer;

import com.zero.easyrpc.example.netty.TestContentBody;

import static com.zero.easyrpc.common.serialization.SerializerFactory.serializerImpl;

/**
 * 
 *
 * 1)使用protoStuff序列化测试
 * 修改org.laopopo.common.serialization.Serializer中的内容为：
 * org.laopopo.common.serialization.proto.ProtoStuffSerializer
 * 
 * 2)使用fastjson序列化测试
 * 修改org.laopopo.common.serialization.Serializer中的内容为：
 * org.laopopo.common.serialization.fastjson.FastjsonSerializer
 * 
 * 3)使用kryo序列化测试
 * 修改org.laopopo.common.serialization.Serializer中的内容为：
 * org.laopopo.common.serialization.kryo.KryoSerializer
 * 
 * @time 2016年8月12日
 * @modifytime
 */
public class SerializerTest {
	
	public static void main(String[] args) {
		
		long beginTime = System.currentTimeMillis();
		
		for(int i = 0;i < 100000;i++){
			TestContentBody.ComplexTestObj complexTestObj = new TestContentBody.ComplexTestObj("attr1", 2);
			TestContentBody commonCustomHeader = new TestContentBody(1, "test",complexTestObj);
			byte[] bytes = serializerImpl().writeObject(commonCustomHeader);
			
			TestContentBody body = serializerImpl().readObject(bytes, TestContentBody.class);
		}
		
		long endTime = System.currentTimeMillis();
		
		System.out.println((endTime - beginTime));
		
	}

}
