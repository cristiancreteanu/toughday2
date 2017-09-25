package com.adobe.qe.toughday.core.config.parsers.yaml;

import com.adobe.qe.toughday.core.ReflectionsContainer;
import com.adobe.qe.toughday.core.config.ConfigParams;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

public class GenerateYamlConfiguration {

    private ConfigParams configParams;
    private Map<String, Class> itemsIdentifiers;
    private java.util.List<YamlDumpAction> yamlTestActions;
    private java.util.List<YamlDumpAction> yamlPublisherActions;
    private List<YamlDumpAction> yamlMetricActions;
    private List<YamlDumpAction> yamlExtensionActions;

    private static final String DEFAULT_YAML_CONFIGURATION_FILENAME = "toughday_";
    private static final String DEFAULT_YAML_EXTENSION = ".yaml";
    private static final SimpleDateFormat TIME_STAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

    public GenerateYamlConfiguration(ConfigParams configParams, Map<String, Class> items) {
        this.configParams = configParams;
        this.itemsIdentifiers = items;
        yamlTestActions = new ArrayList<>();
        yamlPublisherActions = new ArrayList<>();
        yamlMetricActions = new ArrayList<>();
        yamlExtensionActions = new ArrayList<>();
        createActionsForItems();
    }

    public Map<String, Object> getGlobals() {
        Map<String, Object> globals = configParams.getGlobalParams();
        globals.remove("configfile");
        return globals;
    }

    public Map<String, Object> getPublishmode() {
        return configParams.getPublishModeParams();
    }

    public Map<String, Object> getRunmode() {
        return configParams.getRunModeParams();
    }

    public List<YamlDumpAction> getTests() {
        return yamlTestActions;
    }

    public List<YamlDumpAction> getPublishers() {
        return yamlPublisherActions;
    }

    public List<YamlDumpAction> getMetrics() {
        return yamlMetricActions;
    }

    public List<YamlDumpAction> getExtensions() { return yamlExtensionActions; }

    // creates a list of actions for each item(tests, publishers, metrics, extensions)
    private void createActionsForItems() {
        // create add actions
        for (ConfigParams.ClassMetaObject item : configParams.getItemsToAdd()) {
            YamlDumpAddAction addAction = new YamlDumpAddAction(item.getClassName(), item.getParameters());
            if (ReflectionsContainer.getInstance().getTestClasses().containsKey(item.getClassName())) {
                yamlTestActions.add(addAction);
            } else if (ReflectionsContainer.getInstance().getPublisherClasses().containsKey(item.getClassName())) {
                yamlPublisherActions.add(addAction);
            } else if (ReflectionsContainer.getInstance().getMetricClasses().containsKey(item.getClassName())
                    || item.getClassName().equals("BASICMetrics") || item.getClassName().equals("DEFAULTMetrics")){
                yamlMetricActions.add(addAction);
            } else if (item.getClassName().endsWith(".jar")) {
                yamlExtensionActions.add(addAction);
            }
        }

        // create config actions
        for (ConfigParams.NamedMetaObject item : configParams.getItemsToConfig()) {
            YamlDumpConfigAction configAction = new YamlDumpConfigAction(item.getName(), item.getParameters());
            if (ReflectionsContainer.getInstance().getTestClasses().containsKey(itemsIdentifiers.get(item.getName()).getSimpleName())) {
                yamlTestActions.add(configAction);
            } else if (ReflectionsContainer.getInstance().getPublisherClasses().containsKey(itemsIdentifiers.get(item.getName()).getSimpleName())) {
                yamlPublisherActions.add(configAction);
            } else if (ReflectionsContainer.getInstance().getMetricClasses().containsKey(itemsIdentifiers.get(item.getName()).getSimpleName())) {
                yamlMetricActions.add(configAction);
            }
        }

        // create exclude actions
        for (String item : configParams.getItemsToExclude()) {
            YamlDumpExcludeAction excludeAction = new YamlDumpExcludeAction(item);
            if (ReflectionsContainer.getInstance().getTestClasses().containsKey(itemsIdentifiers.get(item).getSimpleName())) {
                yamlTestActions.add(excludeAction);
            } else if (ReflectionsContainer.getInstance().getPublisherClasses().containsKey(itemsIdentifiers.get(item).getSimpleName())) {
                yamlPublisherActions.add(excludeAction);
            } else if (ReflectionsContainer.getInstance().getMetricClasses().containsKey(itemsIdentifiers.get(item).getSimpleName())) {
                yamlMetricActions.add(excludeAction);
            }
        }
    }

    // Configure yaml representer to exclude class tags when dumping an object.
    private void configureYamlRepresenterToExcludeClassTags(Representer representer) {
        // Tag.MAP is by default ignored when dumping an object
        representer.addClassTag(GenerateYamlConfiguration.class, Tag.MAP);
        for (Class klass : ReflectionsContainer.getSubTypesOf(YamlDumpAction.class)) {
            representer.addClassTag(klass, Tag.MAP);
        }

    }

    /**
     * Creates a YAML configuration file.
     */
    public void createYamlConfigurationFile() {

        final String timestamp = TIME_STAMP_FORMAT.format(new Date());
        final String filename = DEFAULT_YAML_CONFIGURATION_FILENAME + timestamp + DEFAULT_YAML_EXTENSION;

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        org.yaml.snakeyaml.constructor.Constructor constructor = new org.yaml.snakeyaml.constructor.Constructor
                (GenerateYamlConfiguration.class);

        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setAllowReadOnlyProperties(true);

        // Configure the representer to ignore empty fields when dumping the object. By default, each empty filed is represented as {}.

        Representer representer = new Representer() {
            @Override
            protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue, Tag customTag) {

                Method method = null;
                try {
                    method = propertyValue.getClass().getMethod("isEmpty");
                } catch (NoSuchMethodException e) { }

                if (method == null) {
                    return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
                } else {

                    try {
                        if (Boolean.valueOf(method.invoke(propertyValue).toString())) {
                            return null;
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }

                    return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
                }
            }
        };

        configureYamlRepresenterToExcludeClassTags(representer);

        // dump configuration
        Yaml yaml = new Yaml(constructor, representer, dumperOptions);
        String yamlObjectRepresentation = yaml.dump(this);

        try {
            bufferedWriter.write(yamlObjectRepresentation);
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            fileWriter.close();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}