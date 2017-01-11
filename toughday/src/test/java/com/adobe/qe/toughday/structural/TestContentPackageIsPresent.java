package com.adobe.qe.toughday.structural;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.ReflectionsContainer;
import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

/**
 * Created by tuicu on 11/01/17.
 */
public class TestContentPackageIsPresent {

    @Test
    public void test() {
        Assert.assertNotNull("Tough Day Content Package is not in jar!", ReflectionsContainer.getInstance().getToughdayContentPackage());
    }
}
