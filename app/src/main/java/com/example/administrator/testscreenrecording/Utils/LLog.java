package com.example.administrator.testscreenrecording.Utils;

/**
 * Created by linhtruong on 4/26/2016.
 */
public class LLog {

    /*private static final int V = 0;
    private static final int D = 1;
    private static final int I = 2;
    private static final int W = 3;
    private static final int E = 4;


    private static final String TAG = "PlanDay";
    private static final boolean IS_SHOW = true;

    private static int OTHERS = -1;

    public static void v(Object... values) {
        int kind = OTHERS;
        try {
            kind = (Integer) values[0];
        } catch (Exception e) {
            // TODO: handle exception
        }
        print(kind, V, values);
    }

    public static void d(Object... values) {
        int kind = OTHERS;
        try {
            kind = (Integer) values[0];
        } catch (Exception e) {
            // TODO: handle exception
        }
        print(kind, D, values);
    }

    public static void i(Object... values) {
        int kind = OTHERS;
        try {
            kind = (Integer) values[0];
        } catch (Exception e) {
            // TODO: handle exception
        }
        print(kind, I, values);
    }

    public static void w(Object... values) {
        int kind = OTHERS;
        try {
            kind = (Integer) values[0];
        } catch (Exception e) {
            // TODO: handle exception
        }
        print(kind, W, values);
    }

    public static void e(Exception e) {
        int kind = OTHERS;
        print(kind, E, e);
    }

    private static void print(int kind, int type, Object... values) {

        LogInfo info = null;
        String value = "";

        if (values != null && values.length > 0) {
            if (values.length > 0 && values[0].toString().compareTo("+") == 0) {
                info = getLogInfo(4);
                LogInfo tmp = getLogInfo(3);
                values[0] = tmp.methodName;
            } else {
                info = getLogInfo(3);
            }

            value += " " + Arrays.toString(values);
        } else {
            info = getLogInfo(3);
        }

        if (IS_SHOW) {
            String strPrint = info.methodName + "(" + info.lineNumber + ") " + value;

            switch (type) {
                case V:
                    android.util.Log.v(TAG, info.filename + " : " + strPrint);
                    break;
                case D:
                    android.util.Log.d(TAG, info.filename + " : " + strPrint);
                    break;
                case I:
                    android.util.Log.i(TAG, info.filename + " : " + strPrint);
                    break;
                case W:
                    android.util.Log.w(TAG, info.filename + " : " + strPrint);
                    break;
                case E:
                    android.util.Log.e(TAG, info.filename + " : " + strPrint);

                    if (values[0] instanceof Exception) {
                        Exception e = (Exception) values[0];
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    private static LogInfo getLogInfo(int index) {
        LogInfo i = new LogInfo();

        Throwable stack = new Throwable().fillInStackTrace();
        StackTraceElement[] traceElements = stack.getStackTrace();

        if (index + 1 < traceElements.length) {

            if (traceElements[index].getFileName() != null) {
                i.filename = traceElements[index].getFileName().replaceAll(".java", "");
            } else {
                // can not get file name
                i.filename = "-";
            }

            if (i.filename.compareTo("ActivityFont") == 0 || i.filename.compareTo("ActivityThread") == 0) {

                if (traceElements[index + 1].getFileName() != null) {
                    i.filename = traceElements[index].getFileName().replaceAll(".java", "");
                } else {
                    i.filename = "-";
                }

                i.className = traceElements[index + 1].getClassName();
                i.methodName = traceElements[index + 1].getMethodName();
                i.lineNumber = traceElements[index + 1].getLineNumber();


            } else {

                i.className = traceElements[index].getClassName();
                i.methodName = traceElements[index].getMethodName();
                i.lineNumber = traceElements[index].getLineNumber();

            }


        }

        *//*for (int j = traceElements.length - 1; j >= 3 ; j--) {

            String className = traceElements[j].getClassName();
            String methodName = traceElements[j].getMethodName();
            int lineNumber = traceElements[j].getLineNumber();

        }*//*


        return i;
    }

    static class LogInfo {
        public String filename = null;
        public String className = null;
        public String methodName = null;
        public String description = null;
        public int lineNumber = 0;

        public String toString() {
            String shotClassName = className.substring(
                    className.lastIndexOf(".") + 1, className.length());
            return shotClassName + ":" + methodName + "(" + lineNumber + ")";
        }
    }*/
}
