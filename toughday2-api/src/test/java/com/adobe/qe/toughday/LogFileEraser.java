package com.adobe.qe.toughday;

import com.adobe.qe.toughday.internal.core.Timestamp;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class LogFileEraser {

    private LogFileEraser() {}

    public static void deteleFiles(org.apache.logging.log4j.core.config.Configuration config) {
//        context.reconfigure();
        File folder = new File("logs_" + Timestamp.START_TIME);

        if (folder.exists()) {
//            org.apache.logging.log4j.core.config.Configuration config = context.getConfiguration();
            if(config.getLoggerContext() != null) {
                config.getLoggerContext().reconfigure();
            }

            for (Map.Entry<String, Appender> appenderEntry : config.getAppenders().entrySet()) {
                appenderEntry.getValue().stop();
            }

            try {
                boolean allDeleted = true;
                for (File file : folder.listFiles()) {
                    if (!file.delete()) {
                        allDeleted = false;
                    }
                }

                if (allDeleted) {
                    FileUtils.deleteDirectory(folder);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
