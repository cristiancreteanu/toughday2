package com.adobe.qe.toughday.core.config.parsers.yaml;

import com.adobe.qe.toughday.core.config.ConfigParams;
import com.adobe.qe.toughday.core.config.ConfigurationParser;
import com.adobe.qe.toughday.core.config.ParserArgHelp;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.resolver.Resolver;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Created by tuicu on 26/12/16.
 */
public class YamlParser implements ConfigurationParser {
    public static final String CONFIG_FILE_ARG_NAME = "configfile";
    public static final String CONFIG_FILE_DESCRIPTION = "Config file in yaml format.";


    public static final ParserArgHelp configFile = new ParserArgHelp() {
        @Override
        public String name() {
            return CONFIG_FILE_ARG_NAME;
        }

        @Override
        public String defaultValue() {
            return "";
        }

        @Override
        public String description() {
            return CONFIG_FILE_DESCRIPTION;
        }
    };

    @Override
    public ConfigParams parse(String[] cmdLineArgs) {
        String configFilePath = null;

        for(String arg : cmdLineArgs) {
            if (arg.startsWith("--" + CONFIG_FILE_ARG_NAME +  "=")) {
                configFilePath = arg.split("=")[1];
            }
        }

        if(configFilePath != null) {
            try {
                Resolver resolver;
                Constructor constructor = new Constructor(YamlConfiguration.class);
                TypeDescription yamlParserDesc = new TypeDescription(YamlConfiguration.class);
                yamlParserDesc.putListPropertyType("tests", YamlAction.class);
                yamlParserDesc.putListPropertyType("publishers", YamlRestrictedAction.class);

                constructor.addTypeDescription(yamlParserDesc);
                Yaml yaml = new Yaml(constructor);
                YamlConfiguration yamlConfig = (YamlConfiguration) yaml.load(new FileInputStream(configFilePath));

                return yamlConfig.getConfigParams();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        return new ConfigParams();
    }
}
