package com.adobe.qe.toughday.tests.sequential;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.annotations.Before;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.config.ConfigArg;
import com.adobe.qe.toughday.tests.utils.Constants;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.HttpStatus;
import org.apache.sling.testing.clients.util.FormEntityBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

@Description(name = "CreateUserTest", desc = "Creates users")
public class CreateUserTest extends SequentialTestBase {
    private static final String DEFAULT_PASSWORD = "toughday";
    private static final String DEFAULT_EMAIL_ADDRESS = "toughday@adobe.com";
    private static final String DEFAULT_PHONE_NUMBER = "098765654";
    private static final String DEFAULT_FIRST_NAME = "Tough";
    private static final String DEFAULT_LAST_NAME = "Day";
    private static final String DEFAULT_JOB_TITLE = "Performance Tester";
    private static final String DEFAULT_STREET = "151 South Almaden Boulevard";
    private static final String DEFAULT_CITY = "San Jose";
    private static final String DEFAULT_MOBILE = "0987654";
    private static final String DEFAULT_POSTAL_CODE = "123456";
    private static final String DEFAULT_COUNTRY = "United States";
    private static final String DEFAULT_GENDER = "male";
    private static final String DEFAULT_STATE = "California";
    private static final String DEFAULT_ABOUT_ME = "Stress testing and performance benchmarking.";

    private String id;
    private String title;
    private String password = DEFAULT_PASSWORD;
    private String emailAddress = DEFAULT_EMAIL_ADDRESS;
    private String phoneNumber = DEFAULT_PHONE_NUMBER;
    private String firstName = DEFAULT_FIRST_NAME;
    private String lastName = DEFAULT_LAST_NAME;
    private String jobTitle = DEFAULT_JOB_TITLE;
    private String street = DEFAULT_STREET;
    private String city = DEFAULT_CITY;
    private String mobile = DEFAULT_MOBILE;
    private String postalCode = DEFAULT_POSTAL_CODE;
    private String country = DEFAULT_COUNTRY;
    private String state = DEFAULT_STATE;
    private String gender = DEFAULT_GENDER;
    private String aboutMe = DEFAULT_ABOUT_ME;
    private ArrayList<String> groups = new ArrayList<>();
    private AtomicInteger increment;

    public CreateUserTest() {
        increment = new AtomicInteger(0);
    }

    @Before
    private void before() {
        title = id = RandomStringUtils.randomAlphanumeric(20);
    }

    @Override
    public void test() throws Exception {
        String firstName    = this.firstName;
        String lastName     = this.lastName;
        String phoneNumber  = this.phoneNumber;
        String mobile       = this.mobile;
        String jobTitle     = this.jobTitle;
        String country      = this.country;
        String gender       = this.gender;
        String aboutMe      = this.aboutMe;
        String emailAddress = this.emailAddress;

        if(increment != null) {
            int incrementValue = increment.getAndIncrement();
            firstName += incrementValue;
            lastName += incrementValue;
            phoneNumber += incrementValue;
            mobile += incrementValue;
            jobTitle += incrementValue;
            aboutMe += incrementValue;
            String[] tmp = emailAddress.split("@");
            gender = Constants.GENDERS[incrementValue % 2];
            country = Constants.COUNTRIES[incrementValue % Constants.COUNTRIES.length];
            emailAddress = tmp[0] + incrementValue + "@" + tmp[1];
        }

        //Create user
        FormEntityBuilder entityBuilder = FormEntityBuilder.create()
            .addParameter("authorizableId", id)
            .addParameter("./jcr:title", title)
            .addParameter("./profile/email", emailAddress)
            .addParameter("rep:password", password)
            .addParameter("./profile/givenName", firstName)
            .addParameter("./profile/familyName", lastName)
            .addParameter("./profile/phoneNumber", phoneNumber)
            .addParameter("./profile/jobTitle", jobTitle)
            .addParameter("./profile/street", street)
            .addParameter("./profile/mobile", mobile)
            .addParameter("./profile/city", city)
            .addParameter("./profile/postalCode", postalCode)
            .addParameter("./profile/country", country)
            .addParameter("./profile/state", state)
            .addParameter("./profile/gender", gender)
            .addParameter("./profile/aboutMe", aboutMe)
            .addParameter("_charset_", "utf-8")
            .addParameter("createUser", "1");

        getDefaultClient().doPost("/libs/granite/security/post/authorizables.html", entityBuilder.build(), HttpStatus.SC_CREATED);

        //Add user to the groups
        for(String group : groups) {
            addUserToGroup(group, id);
        }

        ArrayList<String> communicatedGroups = getCommunication("groups", new ArrayList<String>());
        communicatedGroups.remove(groups);
        for(String group : communicatedGroups) {
            addUserToGroup(group, id);
        }
    }

    private void addUserToGroup(String group, String user) throws Exception {
        String groupServlet = group + ".rw.userprops.html";

        FormEntityBuilder entityBuilder = FormEntityBuilder.create()
                .addParameter("addMembers", id)
                .addParameter("_charset_", "utf-8");

        getDefaultClient().doPost(groupServlet, entityBuilder.build(), HttpStatus.SC_OK);
    }

    @ConfigArg(required = false, desc = "Email address for created users.", defaultValue = DEFAULT_EMAIL_ADDRESS)
    public CreateUserTest setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
        return this;
    }

    @ConfigArg(required = false, desc = "Password for the created users.", defaultValue = DEFAULT_PASSWORD)
    public CreateUserTest setPassword(String password) {
        this.password = password;
        return this;
    }

    @ConfigArg(required = false, desc = "Telephone for the created users.", defaultValue = DEFAULT_PHONE_NUMBER)
    public CreateUserTest setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    @ConfigArg(required = false, desc = "First name for the created users", defaultValue = DEFAULT_FIRST_NAME)
    public CreateUserTest setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    @ConfigArg(required = false, desc = "Last name for the created users", defaultValue = DEFAULT_LAST_NAME)
    public CreateUserTest setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    @ConfigArg(required = false, desc = "Job title for the created users", defaultValue = DEFAULT_JOB_TITLE)
    public CreateUserTest setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
        return this;
    }

    @ConfigArg(required = false, desc = "Street address for the created users", defaultValue = DEFAULT_STREET)
    public CreateUserTest setStreet(String street) {
        this.street = street;
        return this;
    }

    @ConfigArg(required = false, desc = "City address for the created users", defaultValue = DEFAULT_CITY)
    public CreateUserTest setCity(String city) {
        this.city = city;
        return this;
    }

    @ConfigArg(required = false, desc = "Mobile number for the created users", defaultValue = DEFAULT_MOBILE)
    public CreateUserTest setMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    @ConfigArg(required = false, desc = "Postal code for the created users", defaultValue = DEFAULT_POSTAL_CODE)
    public CreateUserTest setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
    }

    @ConfigArg(required = false, desc = "Country for the created users", defaultValue = DEFAULT_COUNTRY)
    public CreateUserTest setCountry(String country) {
        this.country = country;
        return this;
    }

    @ConfigArg(required = false, desc = "State for the created users", defaultValue = DEFAULT_STATE)
    public CreateUserTest setState(String state) {
        this.state = state;
        return this;
    }

    @ConfigArg(required = false, desc = "Gender for the created users.", defaultValue = DEFAULT_GENDER)
    public CreateUserTest setGender(String gender) {
        this.gender = gender;
        return this;
    }

    @ConfigArg(required = false, desc = "User description", defaultValue = DEFAULT_ABOUT_ME)
    public CreateUserTest setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
        return this;
    }

    @ConfigArg(required = false, desc = "If this is true then some of user properties will be either incremented or randomised", defaultValue = "true")
    public CreateUserTest setIncrement(String value) {
        if (!Boolean.getBoolean(value))
            this.increment = null;
        return this;
    }

    private CreateUserTest setIncrement(AtomicInteger increment) {
        this.increment = increment;
        return this;
    }

    @ConfigArg(required = false, desc = "Comma separated group paths. Newly created users will be added in these groups.")
    public CreateUserTest setGroups(String values) {
        groups.addAll(Arrays.asList(values.split(",")));
        return this;
    }

    public CreateUserTest setGroups(ArrayList<String> groups) {
        this.groups = groups;
        return this;
    }

    @Override
    public AbstractTest newInstance() {
        return new CreateUserTest()
                .setGroups(groups)
                .setIncrement(increment)
                .setFirstName(firstName)
                .setLastName(lastName)
                .setPassword(password)
                .setStreet(street)
                .setCity(city)
                .setState(state)
                .setCountry(country)
                .setPostalCode(postalCode)
                .setPhoneNumber(phoneNumber)
                .setMobile(mobile)
                .setJobTitle(jobTitle)
                .setEmailAddress(emailAddress)
                .setAboutMe(aboutMe);
    }

}
