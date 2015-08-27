package com.day.qa.toughday.cli;

import com.day.qa.toughday.publishers.Publisher;
import com.day.qa.toughday.tests.AbstractTest;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by tuicu on 27/08/15.
 */
public class Cli {
    private static Option getOptionForClass(Option.Builder builder, Class<?> klass) {
        ArrayList<Method> properties = new ArrayList<>();
        for(Method method : klass.getMethods()) {
            if(method.getAnnotation(CliArg.class) != null) {
                if(method.getReturnType() == null || !method.getReturnType().isAssignableFrom(AbstractTest.class)
                        || method.getParameterTypes().length != 1) {
                    throw new IllegalStateException("Setters annotated with CliArg must respect the builder pattern." +
                            "They must receive one argument and return the \"this\" reference");
                }
                properties.add(method);
            }
        }
        if(properties.size() != 0) {
            builder.hasArgs();
            String argName = "";
            for (int i = 0; i < properties.size(); i++) {
                if (i != 0)
                    argName += "> <";
                Method current = properties.get(i);
                String currentArgName = (current.getName().startsWith("set") ? current.getName().substring(3) : current.getName()) + "=value";
                currentArgName = current.getAnnotation(CliArg.class).required() ? currentArgName : "[" + currentArgName + "]";
                argName += currentArgName;
            }
            builder.argName(argName);
        }
        return builder.build();

    }

    private static Option getOptionFromTestClass(Class<? extends AbstractTest> testClass) {
        Option.Builder builder = Option.builder()
                .longOpt(testClass.getSimpleName())
                .desc("add a " + testClass.getSimpleName() + " test to the suite");

        return getOptionForClass(builder, testClass);
    }

    public static Option getOptionFromPublisher(Class<? extends Publisher> publisher) {
        Option.Builder builder = Option.builder()
                .longOpt(publisher.getSimpleName())
                .desc("add a " + publisher.getSimpleName() + " publisher");

        return getOptionForClass(builder, publisher);
    }

    public static Options getOptions() {
        Options options = new Options();
        options.addOption(Option.builder().longOpt("duration")
                .desc("how long will toughday run")
                .required()
                .hasArg()
                .build());
        options.addOption(Option.builder().longOpt("wait_time")
                .desc("wait time between two consecutive test runs for a user")
                .required()
                .hasArg()
                .build());
        options.addOption(Option.builder().longOpt("concurrency")
                .desc("number of concurrent users")
                .required()
                .hasArg()
                .build());
        Reflections reflections = new Reflections("com.day.qa");
        for(Class<? extends AbstractTest> testClass : reflections.getSubTypesOf(AbstractTest.class)) {
            options.addOption(getOptionFromTestClass(testClass));
        }

        for(Class<? extends Publisher> publisherClass : reflections.getSubTypesOf(Publisher.class)) {
            options.addOption(getOptionFromPublisher(publisherClass));
        }

        return options;
    }
}
