package com.day.qa.toughday.core.config;

import com.day.qa.toughday.core.*;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by tuicu on 27/08/15.
 */
public class CliParser implements ConfigurationParser {
    private Options options; //used for printing help message only

    public CliParser() {

        options = new Options();
        options = new Options();
        options.addOption(Option.builder().longOpt("Duration=val")
                .desc("how long will toughday run")
                .build());
        options.addOption(Option.builder().longOpt("WaitTime=val")
                .desc("wait time between two consecutive test runs for a user in milliseconds")
                .build());
        options.addOption(Option.builder().longOpt("Concurrency=val")
                .desc("number of concurrent users")
                .build());
        options.addOption(Option.builder().longOpt("Timeout=val")
                .desc("how long can a test run before it is interrupted and marked as failed")
                .build());

        options.addOptionGroup(getOptionsForClass(Configuration.GlobalArgs.class));

        for(Class<? extends AbstractTest> testClass : ReflectionsContainer.getInstance().getTestClasses().values()) {
            options.addOption(getOptionFromTestClass(testClass));
        }

        for(Class<? extends Publisher> publisherClass : ReflectionsContainer.getInstance().getPublisherClasses().values()) {
            options.addOption(getOptionFromPublisher(publisherClass));
        }


        String desc = "add setup step for the suite. \"val\" can be:";
        for(Class<? extends SuiteSetup> suiteSetupClass : ReflectionsContainer.getInstance().getSuiteSetupClasses().values()) {
            desc += " " + suiteSetupClass.getSimpleName();
        }
        options.addOption(Option.builder().longOpt("SetupStep=val").desc(desc).required(false).build());
    }


    private String getOptionArgNameForClass(Class<?> klass) {
        ArrayList<Method> properties = new ArrayList<>();
        for(Method method : klass.getMethods()) {
            if(method.getAnnotation(ConfigArg.class) != null) {
                if(method.getParameterTypes().length != 1 || !method.getParameterTypes()[0].isAssignableFrom(String.class)) {
                    throw new IllegalStateException("Setters annotated with ConfigArg must have one parameter of type String");
                }
                properties.add(method);
            }
        }

        String argName = "";

        if(properties.size() != 0) {
            for (int i = 0; i < properties.size(); i++) {
                if (i != 0)
                    argName += "> <";
                Method current = properties.get(i);
                String currentArgName = Configuration.propertyFromMethod(current.getName()) + "=val";
                currentArgName = current.getAnnotation(ConfigArg.class).required() ? currentArgName : "[" + currentArgName + "]";
                argName += currentArgName;
            }
        }
        return argName;
    }

    private Option getOptionFromTestClass(Class<? extends AbstractTest> testClass) {
        Option.Builder builder = Option.builder()
                .longOpt(testClass.getSimpleName())
                .desc("add a " + testClass.getSimpleName() + " test to the suite");

        String argName = getOptionArgNameForClass(testClass);
        argName = "Weight=val" + (argName.length() != 0 ? "> <" + argName : argName);
        builder.hasArgs().argName(argName);

        return builder.build();
    }

    public Option getOptionFromPublisher(Class<? extends Publisher> publisher) {
        Option.Builder builder = Option.builder()
                .longOpt(publisher.getSimpleName())
                .desc("add a " + publisher.getSimpleName() + " publisher");

        String argName = getOptionArgNameForClass(publisher);
        return argName.length() == 0 ? builder.build() : builder.hasArgs().argName(argName).build();
    }

    public OptionGroup getOptionsForClass(Class klass) {
        OptionGroup group = new OptionGroup();
        for(Method m : klass.getMethods()) {
            if(m.getAnnotation(ConfigArg.class) != null) {
                ConfigArg annotation = m.getAnnotation(ConfigArg.class);
                group.addOption(Option.builder()
                        .longOpt(Configuration.propertyFromMethod(m.getName()) + "=val")
                        .build());
            }
         }
        return group;
    }


    private void parseAndAddProperty(String propertyAndValue, HashMap<String, String> args) {
        String[] optionValue = propertyAndValue.split("=");
        if(optionValue.length != 2)
            throw new IllegalArgumentException("Properties must have the following form: property=value. Found: " + propertyAndValue);
        args.put(optionValue[0], optionValue[1]);
    }

    public ConfigParams parse(String[] cmdLineArgs) {
        HashMap<String, String> globalArgs = new HashMap<>();
        ConfigParams configParams = new ConfigParams();
        for(String arg : cmdLineArgs) {
            if(arg.startsWith("--")) {
                arg = arg.substring(2);
                if (!(ReflectionsContainer.getInstance().getTestClasses().containsKey(arg)
                        || ReflectionsContainer.getInstance().getPublisherClasses().containsKey(arg))) {
                    parseAndAddProperty(arg, globalArgs);
                }
            }
        }
        configParams.setGlobalParams(globalArgs);


        for(int i = 0; i < cmdLineArgs.length; i++) {
            if(cmdLineArgs[i].startsWith("--")) {
                String option = cmdLineArgs[i].substring(2);
                if(ReflectionsContainer.getInstance().getTestClasses().containsKey(option)
                        || ReflectionsContainer.getInstance().getPublisherClasses().containsKey(option)) {
                    HashMap<String, String> args = new HashMap<>();
                    for(int j = i + 1; j < cmdLineArgs.length && !cmdLineArgs[j].startsWith("--"); j++) {
                        parseAndAddProperty(cmdLineArgs[j], args);
                        i = j;
                    }
                    if(ReflectionsContainer.getInstance().getTestClasses().containsKey(option)) {
                        configParams.addTest(option, args);
                    }
                    else if(ReflectionsContainer.getInstance().getPublisherClasses().containsKey(option)) {
                        configParams.addPublisher(option, args);
                    }
                }
            }
        }

        return configParams;
    }

    public void printHelp() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(100, "toughday","", options, "");
    }
}
