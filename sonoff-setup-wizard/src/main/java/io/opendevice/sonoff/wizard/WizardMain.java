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

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WizardMain extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {

		final Injector injector = Guice.createInjector( new WizardModule() );

		final Parent p = FXMLLoader.load( WizardMain.class.getResource("/wizard-fxml/Wizard.fxml"),
										  null,
										  new JavaFXBuilderFactory(),
										  (clazz) -> injector.getInstance(clazz)
		);
		
		final Scene scene = new Scene(p);
		
		primaryStage.setScene( scene );
		primaryStage.setWidth( 600 );
		primaryStage.setHeight( 450 );
		primaryStage.setTitle("SonOff Setup - by: OpenDevice");
		
		primaryStage.show();
	}
	
	public static void main(String[] args) { launch(args); }

}
