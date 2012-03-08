/*
 * Copyright 2012 Midokura Pte. Ltd.
 */

package com.midokura.midolman.agent.command;

import com.midokura.midolman.util.Sudo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MtuCommandExecutor extends CommandExecutor<Integer> {

    private final static Logger log =
            LoggerFactory.getLogger(MtuCommandExecutor.class);

    protected MtuCommandExecutor() {
        super(Integer.class);
    }

    @Override
    public void execute() {
        try {
            int returnValue = Sudo.sudoExec("ifconfig " + targetName + " mtu " + param);
            // if there was an error, log it
            if (returnValue != 0) {
                log.warn("Cannot set MTU for " + targetName + " (" + returnValue + ")");
            }
        } catch (Exception e) {
            log.warn("Cannot set MTU for " + targetName, e);
        }
    }
}
