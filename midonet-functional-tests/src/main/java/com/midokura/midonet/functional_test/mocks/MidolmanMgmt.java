/*
* Copyright 2011 Midokura Europe SARL
*/
package com.midokura.midonet.functional_test.mocks;

import java.net.URI;

import com.midokura.midonet.client.dto.*;

public interface MidolmanMgmt {

    DtoRouter addRouter(DtoTenant t, DtoRouter r);

    DtoBridge addBridge(DtoTenant t, DtoBridge b);

    void updateBridge(DtoBridge b);

    void updateRouter(DtoRouter r);

    void updatePort(DtoPort p);

    void linkRouterToPeer(DtoLogicalRouterPort peerPort);

    DtoMaterializedRouterPort addMaterializedRouterPort(DtoRouter r,
                                                        DtoMaterializedRouterPort p);

    DtoLogicalRouterPort addLogicalRouterPort(DtoRouter r,
                                              DtoLogicalRouterPort p);

    DtoBridgePort addMaterializedBridgePort(DtoBridge b,
                                            DtoBridgePort p);

    DtoLogicalBridgePort addLogicalBridgePort(DtoBridge b,
                                              DtoLogicalBridgePort p);

    DtoRoute addRoute(DtoRouter r, DtoRoute rt);

    DtoBgp addBGP(DtoMaterializedRouterPort p, DtoBgp b);

    <T> T get(String path, Class<T> clazz);

    void delete(URI uri);

    DtoAdRoute addBgpAdvertisedRoute(DtoBgp dtoBgp, DtoAdRoute dtpAdRoute);

    DtoRuleChain addRuleChain(DtoTenant tenant, DtoRuleChain chain);

    DtoRuleChain getRuleChain(DtoRouter router, String name);

    DtoRule addRule(DtoRuleChain chain, DtoRule rule);

    DtoVpn addVpn(DtoMaterializedRouterPort p, DtoVpn vpn);

    DtoDhcpSubnet addDhcpSubnet(DtoBridge dtoBridge, DtoDhcpSubnet dhcpSubnet);

    DtoDhcpHost addDhcpSubnetHost(DtoDhcpSubnet dtoSubnet, DtoDhcpHost host);

    void deleteVpn(DtoVpn vpn);

    DtoRoute[] getRoutes(DtoRouter router);

    void stop();

    DtoHost[] getHosts();

    DtoHost getHost(URI uri);

    DtoInterface[] getHostInterfaces(DtoHost host);

    DtoInterface getHostInterface(DtoInterface dtoInterface);

    void addInterface(DtoHost host, DtoInterface dtoInterface);

    void updateInterface(DtoInterface dtoInterface);

    DtoPortGroup addPortGroup(DtoTenant tenant, DtoPortGroup group);

    void addHostInterfacePortMap(DtoHost host, HostInterfacePortMap portMap);

    void deleteHostInterfacePortMap(DtoHost host, HostInterfacePortMap portMap);

    HostInterfacePortMap[] getInterfacePortMap(DtoHost host);
}
