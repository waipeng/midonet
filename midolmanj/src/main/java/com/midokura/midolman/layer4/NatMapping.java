/*
 * Copyright 2011 Midokura KK
 */

package com.midokura.midolman.layer4;

import java.util.Set;


import com.midokura.midolman.rules.NatTarget;
import com.midokura.sdn.flows.PacketMatch;


public interface NatMapping {

    // Allocates and records a Dnat mapping.
    NwTpPair allocateDnat(int nwSrc, short tpSrc, int oldNwDst, short oldTpDst,
            Set<NatTarget> nats, PacketMatch origMatch);

    NwTpPair lookupDnatFwd(int nwSrc, short tpSrc, int oldNwDst,
            short oldTpDst, PacketMatch origMatch);

    NwTpPair lookupDnatRev(
            int nwSrc, short tpSrc, int newNwDst, short newTpDst);

    // Allocates and records a Snat mapping.
    NwTpPair allocateSnat(int oldNwSrc, short oldTpSrc, int nwDst, short tpDst,
            Set<NatTarget> nats, PacketMatch origMatch);

    NwTpPair lookupSnatFwd(int oldNwSrc, short oldTpSrc, int nwDst,
            short tpDst, PacketMatch origMatch);

    NwTpPair lookupSnatRev(
            int newNwSrc, short newTpSrc, int nwDst, short tpDst);

    // The implementation of this method should reserve and clean up resources.
    void updateSnatTargets(Set<NatTarget> targets);

    void freeFlowResources(PacketMatch match);
}
