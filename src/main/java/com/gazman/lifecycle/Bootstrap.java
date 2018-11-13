// =================================================================================================
//	Life Cycle Framework for native android
//	Copyright 2014 Ilya Gazman. All Rights Reserved.
//
//	This is not free software. You can redistribute and/or modify it
//	in accordance with the terms of the accompanying license agreement.
//  http://gazman-sdk.com/license/
// =================================================================================================
package com.gazman.lifecycle;

import com.gazman.lifecycle.signal.$SignalsTerminator;
import com.gazman.lifecycle.signal.SignalsHelper;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Ilya Gazman on 2/17/2015.
 */
public abstract class Bootstrap extends Registrar {

    private static final Object synObject = new Object();
    private SignalsHelper signalsHelper = new SignalsHelper();
    private boolean coreInitialization;


    private static AtomicBoolean registrationCompleted = new AtomicBoolean(false);

    protected static boolean killProcessOnExit = false;

    public static boolean isRegistrationComplete() {
        return registrationCompleted.get();
    }

    public void initialize() {
        if (coreInitialization) {
            throw new IllegalStateException(
                    "Initialization process has already been executed.");
        }
        coreInitialization = true;
        initRegistrars();
        for (Registrar registrar : registrars) {
            registrar.initClasses();
        }
        initClasses();
        for (Registrar registrar : registrars) {
            registrar.initSignals(signalsHelper);
        }
        initSignals(signalsHelper);

        for (Registrar registrar : registrars) {
            registrar.initSettings();
        }
        initSettings();
        registrationCompleted.set(true);
        registrars.clear();
    }

    /**
     * Will dispatch DisposableSignal and then:<br>
     * - Will unregister all the signals in the system<br>
     * - Will remove all the singletons in the system, so GC will be able to destroy them
     */
    public static void exit(final Runnable callback) {
        synchronized (synObject) {
            Registrar.buildersMap.clear();
            Registrar.classesMap.clear();
            Registrar.registrars.clear();
        }
        $SignalsTerminator.exit();
        ClassConstructor.singletons.clear();
        registrationCompleted.set(false);

        if (callback != null) {
            callback.run();
        }
        if (killProcessOnExit) {
            System.exit(1);
        }
    }
}
