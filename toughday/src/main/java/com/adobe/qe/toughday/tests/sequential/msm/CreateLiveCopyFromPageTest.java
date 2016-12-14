/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package com.adobe.qe.toughday.tests.sequential.msm;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.annotations.Before;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.annotations.FactorySetup;
import com.adobe.qe.toughday.core.config.ConfigArg;
import com.adobe.qe.toughday.tests.sequential.SequentialTestBase;
import com.adobe.qe.toughday.tests.utils.TreePhaser;
import com.adobe.qe.toughday.tests.utils.WcmUtils;

import java.util.UUID;

@Description(name = "Create LC from Page", desc = "Creates live copies from pages")
public class CreateLiveCopyFromPageTest extends SequentialTestBase {
    public static final String SOURCE_PAGE_NAME = "CreateLiveCopySourcePage";
    public static final String DESTINATION_PAGE_NAME = "CreateLiveCopyDestPage";
    public static final String DEFAULT_SOURCE_PAGE = ROOT_NODE_PATH + "/" + SOURCE_PAGE_NAME;
    public static final String DEFAULT_DESTINATION_PAGE = ROOT_NODE_PATH + "/" + DESTINATION_PAGE_NAME;
    public static final String LC_PREFIX = "lc";

    private final TreePhaser phaser;
    private String template = WcmUtils.DEFAULT_TEMPLATE;

    private String title = LC_PREFIX;
    private String sourcePage;
    private String destinationPage;


    public CreateLiveCopyFromPageTest() {
        this.phaser = new TreePhaser();
    }

    public CreateLiveCopyFromPageTest(TreePhaser phaser, String title) {
        this.phaser = phaser;
        this.title = title;
    }

    @Before
    private void before() throws Exception {
        this.sourcePage = getCommunication("sourcePage", DEFAULT_SOURCE_PAGE);
        this.destinationPage = getCommunication("destinationPage", DEFAULT_DESTINATION_PAGE);
    }

    @Override
    public void test() throws Exception {
        WcmUtils.createLiveCopy(getDefaultClient(), title, title, destinationPage, sourcePage, false, null, null, false, 200);
    }

    @Override
    public AbstractTest newInstance() {
        return new CreateLiveCopyFromPageTest(phaser, title);
    }
}
