package com.adobe.qe.toughday;

import com.adobe.qe.toughday.internal.core.ReflectionsContainer;
import com.adobe.qe.toughday.internal.core.config.Configuration;
import org.junit.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TestObjectWithRequiredField {
    private List<String> cmdLineArgs;

    @BeforeClass
    public static void onlyOnce() {
        ReflectionsContainer.getInstance().getTestClasses().put("MockTestRequiredField", MockTestRequiredField.class);
        ReflectionsContainer.getInstance().getTestClasses().put("MockTestTwoRequiredFields", MockTestTwoRequiredFields.class);
    }

    @Before
    public void before() {
        cmdLineArgs = new ArrayList<>(Collections.singletonList("--host=localhost"));
    }

    @Test
    public void testSimple() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        new Configuration(cmdLineArgs.toArray(new String[0]));
    }

    @Test
    public void addSimple() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InterruptedException {
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTestRequiredField", "name=RandomTestName"));
        new Configuration(cmdLineArgs.toArray(new String[0]));
    }

    @Test
    public void addSimpleTwo() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTestTwoRequiredFields", "name=RandomTestName", "mock=Something"));
        new Configuration(cmdLineArgs.toArray(new String[0]));
    }

    @Test
    public void addWithoutName() {
        try {
            cmdLineArgs.addAll(Arrays.asList("--add", "MockTestRequiredField"));
            new Configuration(cmdLineArgs.toArray(new String[0]));
            Assert.fail("Added test without the required field.");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void addWithoutNameTwo() {
        try {
            cmdLineArgs.addAll(Arrays.asList("--add", "MockTestTwoRequiredFields", "mock=Something"));
            new Configuration(cmdLineArgs.toArray(new String[0]));
            Assert.fail("Added test without all the required fields.");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void addWithoutNameConfig() {
        try {
            cmdLineArgs.addAll(Arrays.asList("--add", "MockTestRequiredField"));
            cmdLineArgs.addAll(Arrays.asList("--config", "MockTestRequiredField", "name=RandomTestName"));
            new Configuration(cmdLineArgs.toArray(new String[0]));
            Assert.fail("Adding required field from --config without first adding it with --add.");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void configFail() {
        try {
            cmdLineArgs.addAll(Arrays.asList("--add", "MockTestRequiredField", "name=RandomTestName"));
            cmdLineArgs.addAll(Arrays.asList("--config", "MockTestRequiredField", "name=RandomTestNameAgain"));
            new Configuration(cmdLineArgs.toArray(new String[0]));
            Assert.fail("Configuring test using class name after naming it at addition.");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void configPass() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTestRequiredField", "name=RandomTestName"));
        cmdLineArgs.addAll(Arrays.asList("--config", "RandomTestName", "name=RandomTestNameAgain"));
        new Configuration(cmdLineArgs.toArray(new String[0]));
    }

    @Test
    public void configPassTwo() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTestTwoRequiredFields", "name=RandomTestName", "mock=Something"));
        cmdLineArgs.addAll(Arrays.asList("--config", "RandomTestName", "name=RandomTestNameAgain"));
        new Configuration(cmdLineArgs.toArray(new String[0]));
    }

    @Test
    public void excludeFail() {
        try {
            cmdLineArgs.addAll(Arrays.asList("--add", "MockTestRequiredField", "name=RandomTestName"));
            cmdLineArgs.addAll(Arrays.asList("--exclude", "MockTestRequiredField"));
            new Configuration(cmdLineArgs.toArray(new String[0]));
            Assert.fail("Excluding test using class name after naming it at addition.");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void excludePass() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTestRequiredField", "name=SomeName"));
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTestRequiredField", "name=RandomTestName"));
        cmdLineArgs.addAll(Arrays.asList("--exclude", "RandomTestName"));
        new Configuration(cmdLineArgs.toArray(new String[0]));
    }

    @After
    public void afterEach() {
        Configuration.getRequiredFieldsForClassAdded().clear();
    }

    @AfterClass
    public static void afterAll() {
        new File("toughday_" + new SimpleDateFormat("yyyy-MM-dd'T'HH-mm").format(new Date()) + ".yaml").delete();
    }
}
