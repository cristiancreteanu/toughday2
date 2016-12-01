package com.adobe.qe.toughday.tests.composite;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.CompositeTest;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.config.ConfigArg;
import com.adobe.qe.toughday.tests.sequential.users.CreateUserGroupTest;
import com.adobe.qe.toughday.tests.sequential.users.CreateUserTest;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tuicu on 30/11/16.
 */
@Description(name = "CreateGroupWithUsers",
        desc = "At every execution creates one group, five users and adds the users to that group. " +
                "Additionally, it creates one extra group at the beginning and all the users are added into that group as well")
public class CreateGroupWithUsers extends CompositeTest {
    private CreateFiveUsersTest createFiveUsersTest;
    private CreateUserGroupTest createUserGroupTest;

    public CreateGroupWithUsers() { this(true); }

    public CreateGroupWithUsers(boolean createChildren) {
        if (createChildren) {
            this.createUserGroupTest = new CreateUserGroupTest();
            this.createFiveUsersTest = new CreateFiveUsersTest();

            createUserGroupTest.setGlobalArgs(this.getGlobalArgs());
            createFiveUsersTest.setGlobalArgs(this.getGlobalArgs());

            addChild(createUserGroupTest);
            addChild(createFiveUsersTest);
        }
    }

    @Override
    public AbstractTest newInstance() {
        return new CreateGroupWithUsers(false);
    }

    @ConfigArg(required = false, desc = "Email address for created users.", defaultValue = CreateUserTest.DEFAULT_EMAIL_ADDRESS)
    public void setUserEmailAddress(String emailAddress) {
        this.createFiveUsersTest.setEmailAddress(emailAddress);
    }

    @ConfigArg(required = false, desc = "Password for the created users.", defaultValue = CreateUserTest.DEFAULT_PASSWORD)
    public void setUserPassword(String password) {
        this.createFiveUsersTest.setPassword(password);
    }

    @ConfigArg(required = false, desc = "Telephone for the created users.", defaultValue = CreateUserTest.DEFAULT_PHONE_NUMBER)
    public void setUserPhoneNumber(String phoneNumber) {
        this.createFiveUsersTest.setPhoneNumber(phoneNumber);
    }

    @ConfigArg(required = false, desc = "First name for the created users", defaultValue = CreateUserTest.DEFAULT_FIRST_NAME)
    public void setUserFirstName(String firstName) {
        this.createFiveUsersTest.setFirstName(firstName);
    }

    @ConfigArg(required = false, desc = "Last name for the created users", defaultValue = CreateUserTest.DEFAULT_LAST_NAME)
    public void setUserLastName(String lastName) {
        this.createFiveUsersTest.setLastName(lastName);
    }

    @ConfigArg(required = false, desc = "Job title for the created users", defaultValue = CreateUserTest.DEFAULT_JOB_TITLE)
    public void setUserJobTitle(String jobTitle) {
        this.createFiveUsersTest.setJobTitle(jobTitle);
    }

    @ConfigArg(required = false, desc = "Street address for the created users", defaultValue = CreateUserTest.DEFAULT_STREET)
    public void setUserStreet(String street) {
        this.createFiveUsersTest.setStreet(street);
    }

    @ConfigArg(required = false, desc = "City address for the created users", defaultValue = CreateUserTest.DEFAULT_CITY)
    public void setUserCity(String city) {
        this.createFiveUsersTest.setCity(city);
    }

    @ConfigArg(required = false, desc = "Mobile number for the created users", defaultValue = CreateUserTest.DEFAULT_MOBILE)
    public void setUserMobile(String mobile) {
        this.createFiveUsersTest.setMobile(mobile);
    }

    @ConfigArg(required = false, desc = "Postal code for the created users", defaultValue = CreateUserTest.DEFAULT_POSTAL_CODE)
    public void setUserPostalCode(String postalCode) {
        this.createFiveUsersTest.setPostalCode(postalCode);
    }

    @ConfigArg(required = false, desc = "Country for the created users", defaultValue = CreateUserTest.DEFAULT_COUNTRY)
    public void setUserCountry(String country) {
        this.createFiveUsersTest.setCountry(country);
    }

    @ConfigArg(required = false, desc = "State for the created users", defaultValue = CreateUserTest.DEFAULT_STATE)
    public void setUserState(String state) {
        this.createFiveUsersTest.setState(state);
    }

    @ConfigArg(required = false, desc = "Gender for the created users.", defaultValue = CreateUserTest.DEFAULT_GENDER)
    public void setUserGender(String gender) {
        this.createFiveUsersTest.setGender(gender);
    }

    @ConfigArg(required = false, desc = "User description", defaultValue = CreateUserTest.DEFAULT_ABOUT_ME)
    public void setUserAboutMe(String aboutMe) {
        this.createFiveUsersTest.setGender(aboutMe);
    }

    @ConfigArg(required = false, desc = "Group Name", defaultValue = CreateUserGroupTest.DEFAULT_GROUP_NAME)
    public void setGroupName(String groupName) {
        this.createUserGroupTest.setGroupName(groupName);
    }

    @ConfigArg(required = false, desc = "Group Description", defaultValue = CreateUserGroupTest.DEFAULT_GROUP_DESCRIPTION)
    public void setGroupDescription(String description) {
        this.createUserGroupTest.setDescription(description);
    }

    @ConfigArg(required = false, desc = "If this is true then some of user properties will be either incremented or randomised", defaultValue = "true")
    public void setIncrement(String value) {
        createUserGroupTest.setIncrement(value);
        createFiveUsersTest.setIncrement(value);
    }

    private static class CreateFiveUsersTest extends CompositeTest {

        public CreateFiveUsersTest() { this(true); }

        public CreateFiveUsersTest(boolean createChildren) {
            if (createChildren) {
                AtomicInteger increment = new AtomicInteger(0);
                for(int i = 0; i < 5; i++) {
                    addChild(new CreateUserTest()
                            .setIncrement(increment)
                            .setGlobalArgs(this.getGlobalArgs()));
                }
            }
        }


        @Override
        public AbstractTest newInstance() {
            return new CreateFiveUsersTest(false);
        }

        @Override
        public boolean includeChildren() {
            return false;
        }

        public void setEmailAddress(final String emailAddress) {
            for(AbstractTest userTest : this.getChildren()) {
                ((CreateUserTest)userTest).setEmailAddress(emailAddress);
            }
        }

        public void setPassword(String password) {
            for(AbstractTest userTest : this.getChildren()) {
                ((CreateUserTest)userTest).setPassword(password);
            }
        }

        public void setPhoneNumber(String phoneNumber) {
            for(AbstractTest userTest : this.getChildren()) {
                ((CreateUserTest)userTest).setPhoneNumber(phoneNumber);
            }
        }

        public void setFirstName(String firstName) {
            for(AbstractTest userTest : this.getChildren()) {
                ((CreateUserTest)userTest).setFirstName(firstName);
            }
        }

        public void setLastName(String lastName) {
            for(AbstractTest userTest : this.getChildren()) {
                ((CreateUserTest)userTest).setLastName(lastName);
            }
        }

        public void setJobTitle(String jobTitle) {
            for(AbstractTest userTest : this.getChildren()) {
                ((CreateUserTest)userTest).setJobTitle(jobTitle);
            }
        }

        public void setStreet(String street) {
            for(AbstractTest userTest : this.getChildren()) {
                ((CreateUserTest)userTest).setStreet(street);
            }
        }

        public void setCity(String city) {
            for(AbstractTest userTest : this.getChildren()) {
                ((CreateUserTest)userTest).setCity(city);
            }
        }

        public void setMobile(String mobile) {
            for(AbstractTest userTest : this.getChildren()) {
                ((CreateUserTest)userTest).setMobile(mobile);
            }
        }

        public void setPostalCode(String postalCode) {
            for(AbstractTest userTest : this.getChildren()) {
                ((CreateUserTest)userTest).setPostalCode(postalCode);
            }
        }

        public void setCountry(String country) {
            for(AbstractTest userTest : this.getChildren()) {
                ((CreateUserTest)userTest).setCountry(country);
            }
        }

        public void setState(String state) {
            for(AbstractTest userTest : this.getChildren()) {
                ((CreateUserTest)userTest).setState(state);
            }
        }

        public void setGender(String gender) {
            for(AbstractTest userTest : this.getChildren()) {
                ((CreateUserTest)userTest).setGender(gender);
            }
        }

        public void setIncrement(String increment) {
            for(AbstractTest userTest : this.getChildren()) {
                ((CreateUserTest)userTest).setIncrement(increment);
            }
        }
    }
}
