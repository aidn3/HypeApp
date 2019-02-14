package com.aidn5.hypeapp.notifiers;

import com.aidn5.hypeapp.ServicesProvider;


public class AppEventsNotifier extends NotifierFactory {
    public AppEventsNotifier(ServicesProvider servicesProvider) {
        super(servicesProvider);
    }

    @Override
    public void doLoop() {

    }
}
