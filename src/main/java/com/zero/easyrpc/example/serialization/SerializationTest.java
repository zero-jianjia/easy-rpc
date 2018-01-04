package com.zero.easyrpc.example.serialization;


import com.zero.easyrpc.common.serialization.Serializer;
import com.zero.easyrpc.common.serialization.SerializerFactory;
import com.zero.easyrpc.common.serialization.SerializerType;

import java.io.Serializable;

/**
 * Created by zero on 2018/1/4.
 */

class Stu implements Serializable {
    private String name;
    private int age;

    public Stu(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Stu{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}

public class SerializationTest {
    public static void main(String[] args) {
        Serializer serializer = SerializerFactory.getSerializer(SerializerType.PROTO_STUFF.value());

        Stu s1 = new Stu("zero", 18);
        byte[] bytes = serializer.writeObject(s1);
        Stu s2 = serializer.readObject(bytes, Stu.class);
        System.out.println(s2);

        Serializer serializer1 = SerializerFactory.getSerializer(SerializerType.HESSIAN.value());
        Stu s11 = new Stu("zero", 18);
        byte[] bytes1 = serializer1.writeObject(s11);
        Stu s21 = serializer1.readObject(bytes1, Stu.class);
        System.out.println(s21);
    }
}
