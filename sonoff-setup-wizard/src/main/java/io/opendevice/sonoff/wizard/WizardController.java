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

import io.opendevice.sonoff.wizard.annotations.OnShow;
import io.opendevice.sonoff.wizard.annotations.Submit;
import io.opendevice.sonoff.wizard.annotations.Validate;
import com.google.inject.Inject;
import com.google.inject.Injector;
import javafx.beans.binding.When;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Callback;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WizardController {

    private final int INDICATOR_RADIUS = 10;

    private final String CONTROLLER_KEY = "controller";

    private static WizardController INSTANCE;

    @FXML
    VBox contentPanel;

    @FXML
    HBox hboxIndicators;

    @FXML
    Button btnNext, btnBack, btnCancel;

    @Inject
    Injector injector;

    @Inject
    WizardData model;

    public WizardController(){
        INSTANCE = this;
    }

    private final List<Parent> steps = new ArrayList<>();

    private final IntegerProperty currentStep = new SimpleIntegerProperty(-1);


    public static WizardController getInstance() {
        return INSTANCE;
    }

    @FXML
    public void initialize() throws Exception {

        buildSteps();

        initButtons();

        buildIndicatorCircles();

        setInitialContent();
    }

    private void initButtons() {
        btnBack.disableProperty().bind(currentStep.lessThanOrEqualTo(0));
        btnNext.disableProperty().bind(currentStep.greaterThanOrEqualTo(steps.size() - 1));

        btnCancel.textProperty().bind(
                new When(
                        currentStep.lessThan(steps.size() - 1)
                )
                        .then("Cancel")
                        .otherwise("Configure Other")
        );
    }

    private void setInitialContent() {
        currentStep.set(0);  // first element
        contentPanel.getChildren().add(steps.get(currentStep.get()));
    }

    private void buildIndicatorCircles() {
        for (int i = 0; i < steps.size(); i++) {
            hboxIndicators.getChildren().add(createIndicatorCircle(i));
        }
    }

    private void buildSteps() throws java.io.IOException {

        final JavaFXBuilderFactory bf = new JavaFXBuilderFactory();

        final Callback<Class<?>, Object> cb = (clazz) -> injector.getInstance(clazz);

        Parent stepSettings = loadStep("/wizard-fxml/StepSettings.fxml", bf, cb);
        Parent step1 = loadStep("/wizard-fxml/StepPairGuide.fxml", bf, cb);
        Parent step2 = loadStep("/wizard-fxml/StepWaitSonOff.fxml", bf, cb);
        Parent step3 = loadStep("/wizard-fxml/StepConfigureAP.fxml", bf, cb);
        Parent stepRegistration = loadStep("/wizard-fxml/StepRegistration.fxml", bf, cb);

        steps.addAll(Arrays.asList(
                stepSettings, step1, step2, step3, stepRegistration
        ));
    }

    private Parent loadStep(String resource, JavaFXBuilderFactory bf, Callback<Class<?>, Object> cb) throws IOException {
        FXMLLoader loader = new FXMLLoader(WizardController.class.getResource(resource), null, bf, cb);
        Parent step = loader.load();
        step.getProperties().put(CONTROLLER_KEY, loader.getController());
        return step;
    }

    private Circle createIndicatorCircle(int i) {

        Circle circle = new Circle(INDICATOR_RADIUS, Color.WHITE);
        circle.setStroke(Color.BLACK);

        circle.fillProperty().bind(
                new When(
                        currentStep.greaterThanOrEqualTo(i))
                        .then(Color.DODGERBLUE)
                        .otherwise(Color.WHITE));

        return circle;
    }

    @FXML
    public void next() {

        Parent p = steps.get(currentStep.get());
        Object controller = p.getProperties().get(CONTROLLER_KEY);

        // validate
        Method v = getMethod(Validate.class, controller);
        if (v != null) {
            try {
                Object retval = v.invoke(controller);
                if (retval != null && ((Boolean) retval) == false) {
                    return;
                }

            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        // submit
        Method sub = getMethod(Submit.class, controller);
        if (sub != null) {
            try {
                sub.invoke(controller);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        if (currentStep.get() < (steps.size() - 1)) {
            contentPanel.getChildren().remove(steps.get(currentStep.get()));
            currentStep.set(currentStep.get() + 1);
            contentPanel.getChildren().add(steps.get(currentStep.get()));

            // on show callback
            p = steps.get(currentStep.get());
            controller = p.getProperties().get(CONTROLLER_KEY);
            onShow(controller);

        }
    }

    @FXML
    public void back() {

        if (currentStep.get() > 0) {
            contentPanel.getChildren().remove(steps.get(currentStep.get()));
            currentStep.set(currentStep.get() - 1);
            contentPanel.getChildren().add(steps.get(currentStep.get()));
        }
    }

    private void onShow(Object controller){
        Method show = getMethod(OnShow.class, controller);
        if (show != null) {
            try {
                show.invoke(controller);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void cancel() {

        contentPanel.getChildren().remove(steps.get(currentStep.get()));
        currentStep.set(0);  // first screen
        contentPanel.getChildren().add(steps.get(currentStep.get()));

    }

    private Method getMethod(Class<? extends Annotation> an, Object obj) {

        if (an == null) {
            return null;
        }

        if (obj == null) {
            return null;
        }

        Method[] methods = obj.getClass().getMethods();
        if (methods != null && methods.length > 0) {
            for (Method m : methods) {
                if (m.isAnnotationPresent(an)) {
                    return m;
                }
            }
        }
        return null;
    }
}
