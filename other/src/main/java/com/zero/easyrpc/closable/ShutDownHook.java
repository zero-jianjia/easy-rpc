package com.zero.easyrpc.closable;

import java.util.ArrayList;
import java.util.Collections;

public class ShutDownHook extends Thread {
    //Smaller the priority is,earlier the resource is to be closed,default Priority is 20
    private static final int defaultPriority = 20;
    //only global resource should be register to ShutDownHook,don't register connections to it.
    private static ShutDownHook instance;
    private ArrayList<closableObject> resourceList = new ArrayList<closableObject>();

    private ShutDownHook() {
    }


    private static void init() {
        if (instance == null) {
            instance = new ShutDownHook();
        }
    }

    @Override
    public void run() {
        closeAll();
    }

    public static void runHook(boolean sync) {
        if (instance != null) {
            if (sync)
                instance.run();
            else
                instance.start();
        }
    }

    //synchronized method to close all the resources in the list
    private synchronized void closeAll() {
        Collections.sort(resourceList);
        for (closableObject resource : resourceList) {
            try {
                resource.closable.close();
            } catch (Exception e) {
            }
        }
        resourceList.clear();
    }

    public static void registerShutdownHook(Closable closable) {
        registerShutdownHook(closable, defaultPriority);
    }

    public static synchronized void registerShutdownHook(Closable closable, int priority) {
        if (instance == null) {
            init();
        }
        instance.resourceList.add(new closableObject(closable, priority));
    }

    private static class closableObject implements Comparable<closableObject> {
        Closable closable;
        int priority;

        public closableObject(Closable closable, int priority) {
            this.closable = closable;
            this.priority = priority;
        }

        @Override
        public int compareTo(closableObject o) {
            if (this.priority > o.priority) return -1;
            else if (this.priority == o.priority) return 0;
            else return 1;
        }
    }
}

