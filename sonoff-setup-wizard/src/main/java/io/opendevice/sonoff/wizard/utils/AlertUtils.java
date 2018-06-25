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

package io.opendevice.sonoff.wizard.utils;

import javafx.scene.control.Alert;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 *         Date: 10/06/18
 */
public class AlertUtils {

    public static final void error(String title, String content){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Alert");
        alert.setHeaderText( title );
        alert.setContentText( content );
        alert.showAndWait();
    }
}
