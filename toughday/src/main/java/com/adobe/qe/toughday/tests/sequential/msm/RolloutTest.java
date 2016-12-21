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
import com.adobe.qe.toughday.core.config.ConfigArg;
import com.adobe.qe.toughday.tests.sequential.SequentialTestBase;
import com.adobe.qe.toughday.tests.utils.WcmUtils;
import org.apache.logging.log4j.Logger;


@Description(name = "rollout_source", desc = "Rollout the source page/ blueprint")
public class RolloutTest extends SequentialTestBase {
    public static final Logger LOG = createLogger(RolloutTest.class);

    private String sourcePage = null;
    private String destinationPage = null;
    private String type;
    private boolean background;

    public RolloutTest() {
        this.type = "page"; // or "deep"
        this.background = false;
    }

    public RolloutTest(String sourcePage, String destinationPage, String type, boolean background) {
        this.sourcePage = sourcePage;
        this.destinationPage = destinationPage;
        this.type = type;
        this.background = background;
    }

    @Before
    private void before() throws Exception {
        this.sourcePage = getCommunication("resource", sourcePage);
        this.destinationPage = getCommunication("livecopy", destinationPage);
    }

    @Override
    public void test() throws Exception {
        WcmUtils.rolloutPage(getDefaultClient(), type, background,
                new String[]{sourcePage}, null, new String[] {destinationPage}, 200);
    }

    @Override
    public AbstractTest newInstance() {
        return new RolloutTest(sourcePage, destinationPage, type, background);
    }

    @ConfigArg(required = true, desc = "The source page to rollout")
    public AbstractTest setSourcePage(String page) {
        this.sourcePage = page;
        return this;
    }

    @ConfigArg(required = true, desc = "The destination page to rollout to")
    public AbstractTest setDestinationPage(String page) {
        this.destinationPage = page;
        return this;
    }

    @ConfigArg(required = false, desc = "page / deep", defaultValue = "page")
    public AbstractTest setType(String type) {
        this.type = type;
        return this;
    }

    @ConfigArg(required = false, desc = "true/false - Whether to rollout in the background", defaultValue = "false")
    public AbstractTest setBackground(String background) {
        this.background = Boolean.parseBoolean(background);
        return this;
    }
}
