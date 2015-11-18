package com.adobe.qe.toughday;


import com.adobe.qe.toughday.core.Engine;
import com.adobe.qe.toughday.core.config.CliParser;
import com.adobe.qe.toughday.core.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Main class. Creates a Configuration and an engine and runs the tests.
 */
public class Main {
    private static final Logger LOG = LogManager.getLogger(Main.class);

    public static void main (String[] args) throws Exception {
        CliParser cliParser = new CliParser();
        System.out.println();
        if (args.length == 0 || (args.length == 1 && args[0].equals("--help"))) {
            cliParser.printShortHelp();
        } else if (args.length == 1 && args[0].equals("--print_tests")) {
            cliParser.printHelp();
            System.exit(0);
        } else {
            Configuration configuration = null;
            try {
                configuration = new Configuration(args);
            } catch (IllegalArgumentException e) {
                LOG.error("Bad configuration: {}", e.getMessage());
                System.exit(1);
                cliParser.printShortHelp();
            }
            Engine engine = new Engine(configuration);
            LOG.info("Running tests for {} seconds", configuration.getGlobalArgs().getDuration());
            engine.runTests();
            LOG.info("Finished running tests", configuration.getGlobalArgs().getDuration());
            System.exit(0);
        }
    }
}
