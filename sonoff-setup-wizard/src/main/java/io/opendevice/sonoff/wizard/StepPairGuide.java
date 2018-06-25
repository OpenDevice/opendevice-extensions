/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package io.opendevice.sonoff.wizard;

import io.opendevice.sonoff.wizard.annotations.Submit;
import io.opendevice.sonoff.wizard.annotations.Validate;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StepPairGuide {

    private Logger log = LoggerFactory.getLogger(StepPairGuide.class);

    @Inject
    WizardData model;

    @FXML
    public void initialize() {


    }

    @Validate
    public boolean validate() throws Exception {
        return true;
    }

    @Submit
    public void submit() throws Exception {

            log.debug("[SUBMIT] the user has completed step 1");
    }
}
