package com.or2go.vendor.weavvy;

import android.util.Log;

public class GposLogger {
    //TODO Create a detail Enum to define the log detail level.
    //TODO Create a method that halt every log possible based on the development mode Ex: (PRODUCTION, DEVELOPMENT, DEBUG) that can manage what can be and can't be logged
    //======================================================================================
    //ENUMS
    //======================================================================================

    /**
     * <strong>LoggerDepth Enum</strong> <br/>
     * <ul>
     <li>ACTUAL_METHOD(4) </li>
     <li>LOGGER_METHOD(3) </li>
     <li>STACK_TRACE_METHOD(1)</li>
     <li>JVM_METHOD(0)</li>
     <ul/>
     */
    public enum LOGGER_DEPTH{
        ACTUAL_METHOD(4),
        LOGGER_METHOD(3),
        STACK_TRACE_METHOD(1),
        JVM_METHOD(0);

        private final int value;
        private LOGGER_DEPTH(final int newValue){
            value = newValue;
        }
        public int getValue(){
            return value;
        }
    }

    //======================================================================================
    //CONSTANTS
    //======================================================================================
    private static final String personalTAG = "Logger";

    //======================================================================================
    //FIELDS
    //======================================================================================
    private StringBuilder sb;

    //======================================================================================
    //CONSTRUCTORS
    //======================================================================================
    /**
     * private Constructor
     * The Perfect Singleton Pattern as Joshua Bosch Explained at his Effective Java Reloaded talk at Google I/O 2008
     */
    private GposLogger(){
        if(LoggerLoader.instance != null){
            Log.e(personalTAG,"Error: Logger already instantiated");
            throw new IllegalStateException("Already Instantiated");
        }else{

            this.sb = new StringBuilder(255);
        }
    }

    //======================================================================================
    //METHODS
    //======================================================================================
    /**
     * getLogger Method
     * The Perfect Singleton Pattern as Joshua Bosch Explained at his Effective Java Reloaded talk at Google I/O 2008
     * @return Logger (This instance)
     */
    public static GposLogger getLogger(){
        return LoggerLoader.instance;
    }

    /**
     * Method that creates the tag automatically
     * @param depth (Defines the depth of the Logging)
     * @return
     */
    private String getTag(LOGGER_DEPTH depth){
        try{
            String className = Thread.currentThread().getStackTrace()[depth.getValue()].getClassName();
            sb.append(className.substring(className.lastIndexOf(".")+1));
            sb.append("[");
            sb.append(Thread.currentThread().getStackTrace()[depth.getValue()].getMethodName());
            sb.append("] - ");
            sb.append(Thread.currentThread().getStackTrace()[depth.getValue()].getLineNumber());
            return sb.toString();
        }catch (Exception ex){
            ex.printStackTrace();
            Log.d(personalTAG, ex.getMessage());
        }finally{
            sb.setLength(0);
        }
        return null;
    }

    /**
     * Simple d Method will log in default depth ACTUAL_METHOD
     * @param msg
     */
    public void d(String msg) {
        //if (DEBUG) {
        try {
            Log.d(getTag(LOGGER_DEPTH.ACTUAL_METHOD), msg);
        } catch (Exception exception) {
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), "Logger failed, exception: " + exception.getMessage());
        }
        //}
    }


    /**
     * Simple e Method will log in default depth ACTUAL_METHOD
     * @param msg
     */
    public void e(String msg){
        //if (DEBUG) {
        try {
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), msg);

        } catch (Exception exception) {
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), "Logger failed, exception: " + exception.getMessage());
        }
        //}
    }


    /**
     * Simple w Method will log in default depth ACTUAL_METHOD
     * @param msg
     */
    public void w(String msg){
        //if (DEBUG) {
        try {
            Log.w(getTag(LOGGER_DEPTH.ACTUAL_METHOD), msg);

        } catch (Exception exception) {
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), "Logger failed, exception: " + exception.getMessage());
        }
        //}
    }


    /**
     * Simple v Method will log in default depth ACTUAL_METHOD
     * @param msg
     */
    public void v(String msg){
        //if (DEBUG) {
        try {
            Log.v(getTag(LOGGER_DEPTH.ACTUAL_METHOD), msg);

        } catch (Exception exception) {
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), "Logger failed, exception: " + exception.getMessage());
        }
        //}
    }


    /**
     * Simple i Method will log in default depth ACTUAL_METHOD
     * @param msg
     */
    public void i(String msg){
        //if (DEBUG) {
        try {
            Log.i(getTag(LOGGER_DEPTH.ACTUAL_METHOD), msg);

        } catch (Exception exception) {
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), "Logger failed, exception: " + exception.getMessage());
        }
        //}
    }


    /**
     * Simple wtf Method will log in default depth ACTUAL_METHOD
     * @param msg
     */
    public void wtf(String msg){
        //if (DEBUG) {
        try {
            Log.wtf(getTag(LOGGER_DEPTH.ACTUAL_METHOD), msg);

        } catch (Exception exception) {
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), "Logger failed, exception: " + exception.getMessage());
        }
        //}
    }



    //======================================================================================
    //INNER CLASSES
    //======================================================================================

    /**
     * Logger Loader Class
     * The Perfect Singleton Pattern as Joshua Bosch Explained at his Effective Java Reloaded talk at Google I/O 2008
     */
    private static class LoggerLoader {
        private static final GposLogger instance = new GposLogger();
    }
}
