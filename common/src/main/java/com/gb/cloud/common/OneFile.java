package com.gb.cloud.common;

import java.io.Serializable;

public class OneFile implements Serializable {
    public String name;
    public byte [] data;
    public int numberOfParts;
    public int partNumber;


    public void printOneFile(){
        System.out.println("name: " + name);
        System.out.println("numberOfParts: " + numberOfParts);
        System.out.println("partNumber: " + partNumber);
        System.out.println("Array size: " + data.length);
        System.out.println("-------------------" + System.currentTimeMillis());


    }

}
