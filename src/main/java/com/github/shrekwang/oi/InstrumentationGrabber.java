package com.github.shrekwang.oi;

import java.lang.instrument.Instrumentation;


public class InstrumentationGrabber {
    private static volatile Instrumentation instrumentation;

    public static void premain(String agentArgs, Instrumentation inst) {
        if (InstrumentationGrabber.instrumentation != null) throw new AssertionError("Already initialized");
        InstrumentationGrabber.instrumentation = inst;
    }

    private static void checkSetup() {
        if (instrumentation == null ) {
          throw new IllegalStateException(String.valueOf("Instrumentation is not setup properly. "
                + "You have to pass -javaagent:path/to/object-explorer.jar to the java interpreter"));
        }
    }

    static Instrumentation instrumentation() {
        checkSetup();
        return instrumentation;
    }
}
