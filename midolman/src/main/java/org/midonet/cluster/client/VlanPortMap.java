package org.midonet.cluster.client;

import java.util.UUID;

public interface VlanPortMap {
    /**
     * Must return null if portId is null
     */
    public Short getVlan(UUID portId);
    /**
     * Must return null if vlanId is null
     */
    public UUID getPort(Short vlanId);
    /**
     * Tell if there are no mappings
     */
    public boolean isEmpty();
}
