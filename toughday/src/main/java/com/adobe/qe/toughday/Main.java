package com.adobe.qe.toughday;


import com.adobe.qe.toughday.core.engine.Engine;
import com.adobe.qe.toughday.core.config.parsers.cli.CliParser;
import com.adobe.qe.toughday.core.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Main class. Creates a Configuration and an engine and runs the tests.
 */
public class Main {
    private static final Logger LOG = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        CliParser cliParser = new CliParser();
        System.out.println();

        try {
            Configuration configuration = null;
            try {
                configuration = new Configuration(args);
                if (cliParser.printHelp(args)) {
                    System.exit(0);
                }
            } catch (IllegalArgumentException e) {
                LOG.error("Bad configuration: {}", e.getMessage());
                System.out.println();
                System.out.println();
                cliParser.printShortHelp();
                System.exit(1);
            }

            Engine engine = new Engine(configuration);
            engine.runTests();

            System.exit(0);
        } catch (Throwable t) {
            LOG.error("Error encountered: "
                    + (t.getMessage() != null ? t.getMessage() : "Please check toughday.log for more information."));
            LogManager.getLogger(Engine.class).error("Error encountered", t);
        }

        System.exit(0);
    }
}
