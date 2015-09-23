package com.day.qa.toughday.core.config;

import com.day.qa.toughday.core.AbstractTest;
import com.day.qa.toughday.core.Publisher;
import com.day.qa.toughday.core.ReflectionsContainer;
import com.day.qa.toughday.core.TestSuite;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tuicu on 18/09/15.
 */
public class Configuration {
    private GlobalArgs globalArgs;
    private TestSuite suite;


    public Configuration(String[] cmdLineArgs)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        ConfigurationParser parser = getConfigurationParser(cmdLineArgs);
        ConfigParams configParams= parser.parse(cmdLineArgs);

        HashMap<String, String> globalArgsMeta = configParams.getGlobalParams();

        this.globalArgs = createObject(GlobalArgs.class, globalArgsMeta);


        if(configParams.getPublishers().size() == 0)
            throw new IllegalStateException("No publishers added.");

        for(ConfigParams.ParametrizedObject publisherMeta : configParams.getPublishers()) {
            Publisher publisher = createObject(
                    ReflectionsContainer.getInstance().getPublisherClasses().get(publisherMeta.getClassName()),
                    publisherMeta.getParameters());
            this.globalArgs.addPublisher(publisher);
        }

        suite = createObject(TestSuite.class, globalArgsMeta);

        if(configParams.getTests().size() == 0)
            throw new IllegalStateException("No tests added to the suite.");

        for(ConfigParams.ParametrizedObject testMeta : configParams.getTests()) {
            AbstractTest test = createObject(
                    ReflectionsContainer.getInstance().getTestClasses().get(testMeta.getClassName()),
                    testMeta.getParameters());
            test.setGlobalArgs(this.globalArgs);
            if(!testMeta.getParameters().containsKey("Weight"))
                throw new IllegalArgumentException("Property Weight is required for class " + test.getClass().getSimpleName());
            suite.add(test, Integer.parseInt(testMeta.getParameters().get("Weight")));
        }
    }

    public TestSuite getTestSuite() {
        return suite;
    }

    public GlobalArgs getGlobalArgs() {
        return globalArgs;
    }

    public static String propertyFromMethod(String methodName) {
        return methodName.startsWith("set") ? methodName.substring(3) : methodName;
    }


    public static <T> T setObjectProperties(T object, HashMap<String, String> args) throws InvocationTargetException, IllegalAccessException {
        Class classObject = object.getClass();
        for(Method method : classObject.getMethods()) {
            ConfigArg annotation = method.getAnnotation(ConfigArg.class);
            if(annotation == null) {
                continue;
            }
            String property = propertyFromMethod(method.getName());
            String value = args.get(property);
            if(value == null) {
                if(annotation.required()) {
                    throw new IllegalArgumentException("Property " + property + " is required for class " + classObject.getSimpleName());
                }
                else {
                    //will use default value
                    continue;
                }
            }
            method.invoke(object, value);
        }
        return object;
    }

    public static <T> T createObject(Class<? extends T> classObject, HashMap<String, String> args)
            throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        Constructor constructor = null;
        try {
            constructor = classObject.getConstructor(null);
        } catch (NoSuchMethodException e) {
            NoSuchMethodException explicitException = new NoSuchMethodException(classObject.getSimpleName()
                    + " class must have a constructor without arguments");
            explicitException.initCause(e);
            throw explicitException;
        }

        T object = (T) constructor.newInstance();
        setObjectProperties(object, args);
        return object;
    }

    private ConfigurationParser getConfigurationParser(String[] args) {
        //Insert logic here to select from other types of parsers
        return new CliParser();
    }

    public void printHelp() {
        CliParser cliParser = new CliParser();
        cliParser.printHelp();
    }

    /**
     * Created by tuicu on 07/09/15.
     */
    public static class GlobalArgs {
        private String host;
        private int port;
        private String user;
        private String password;
        private int concurrency;
        private long waitTime;
        private long duration;
        private List<Publisher> publishers;
        private long timeout;

        public GlobalArgs() {
            publishers = new ArrayList<>();
        }

        private static long unitToSeconds(char unit) {
            long factor = 1;
            switch (unit) {
                case 'd': factor *= 24;
                case 'h': factor *= 60;
                case 'm': factor *= 60;
                case 's': factor *= 1;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown duration unit: " + unit);
            }
            return factor;
        }

        private static long parseDurationToSeconds(String duration) {
            long finalDuration = 0l;
            long intermDuration = 0;

            for(char c : duration.toCharArray()) {
                if(Character.isDigit(c)) {
                    intermDuration = intermDuration * 10 + (long) (c - '0');
                } else {
                    finalDuration += intermDuration * unitToSeconds(c);
                    intermDuration = 0;
                }
            }
            return finalDuration;
        }

        @ConfigArg
        public void setHost(String host) {
            this.host = host;
        }

        @ConfigArg
        public void setPort(String port) {
            this.port = Integer.parseInt(port);
        }

        @ConfigArg
        public void setUser(String user) {
            this.user = user;
        }

        @ConfigArg
        public void setPassword(String password) {
            this.password = password;
        }

        @ConfigArg
        public void setConcurrency(String concurrencyString) {
            this.concurrency = Integer.parseInt(concurrencyString);
        }

        @ConfigArg
        public void setDuration(String durationString) {
            this.duration = parseDurationToSeconds(durationString);
        }

        @ConfigArg
        public void setWaitTime(String waitTime) {
            this.waitTime = Integer.parseInt(waitTime);
        }

        @ConfigArg
        public void setTimeout(String timeout) {
            this.timeout = Integer.parseInt(timeout) * 1000;
        }

        public void addPublisher(Publisher publisher) {
            publishers.add(publisher);
        }

        public int getConcurrency() {
            return concurrency;
        }

        public long getWaitTime() {
            return waitTime;
        }

        public long getDuration() {
            return duration;
        }

        public List<Publisher> getPublishers() {
            return publishers;
        }

        public long getTimeout() {
            return timeout;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getUser() {
            return user;
        }

        public String getPassword() {
            return password;
        }
    }
}
