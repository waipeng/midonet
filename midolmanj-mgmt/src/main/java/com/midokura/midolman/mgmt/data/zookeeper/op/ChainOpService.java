/*
 * @(#)ChainOpService        1.6 11/12/26
 *
 * Copyright 2011 Midokura KK
 */
package com.midokura.midolman.mgmt.data.zookeeper.op;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.zookeeper.Op;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.midokura.midolman.mgmt.data.dao.zookeeper.ChainZkDao;
import com.midokura.midolman.mgmt.data.dto.config.ChainMgmtConfig;
import com.midokura.midolman.mgmt.data.dto.config.ChainNameMgmtConfig;
import com.midokura.midolman.mgmt.rest_api.core.ChainTable;
import com.midokura.midolman.state.ChainZkManager.ChainConfig;
import com.midokura.midolman.state.StateAccessException;

/**
 * Chain Op service.
 *
 * @version 1.6 26 Dec 2011
 * @author Ryu Ishimoto
 */
public class ChainOpService {

    private final static Logger log = LoggerFactory
            .getLogger(ChainOpService.class);
    private final ChainOpBuilder opBuilder;
    private final ChainZkDao zkDao;

    /**
     * Constructor
     *
     * @param zkDao
     *            Chain DAO.
     * @param opBuilder
     *            Chain DAO opBuilder.
     */
    public ChainOpService(ChainOpBuilder opBuilder, ChainZkDao zkDao) {
        this.opBuilder = opBuilder;
        this.zkDao = zkDao;
    }

    /**
     * Build list of Op objects to create a chain
     *
     * @param id
     *            ID of the chain
     * @param config
     *            ChainConfig object
     * @param mgmtConfig
     *            ChainMgmtConfig object
     * @param nameConfig
     *            ChainNameMgmtConfig object
     * @return List of Op objects.
     * @throws StateAccessException
     */
    public List<Op> buildCreate(UUID id, ChainConfig config,
            ChainMgmtConfig mgmtConfig, ChainNameMgmtConfig nameConfig)
            throws StateAccessException {
        log.debug("ChainOpService.buildCreate entered: id={}", id);
        List<Op> ops = new ArrayList<Op>();

        // Root
        ops.add(opBuilder.getChainCreateOp(id, mgmtConfig));

        // Router/Chain ID
        ops.add(opBuilder.getRouterTableChainCreateOp(config.routerId,
                mgmtConfig.table, id));

        // Router/Chain name
        ops.add(opBuilder.getRouterTableChainNameCreateOp(config.routerId,
                mgmtConfig.table, config.name, nameConfig));

        // Cascade
        ops.addAll(opBuilder.getChainCreateOps(id, config));

        log.debug("ChainOpService.buildCreate exiting: ops count={}",
                ops.size());
        return ops;
    }

    /**
     * Build list of Op objects to delete a chain
     *
     * @param id
     *            ID of the chain
     * @param cascade
     *            True to update Midolman side
     * @return List of Op objects
     */
    public List<Op> buildDelete(UUID id, boolean cascade)
            throws StateAccessException {
        log.debug("ChainOpService.buildDelete entered: id=" + id + ", cascade="
                + cascade);

        ChainConfig config = zkDao.getData(id);
        ChainMgmtConfig mgmtConfig = zkDao.getMgmtData(id);

        List<Op> ops = new ArrayList<Op>();

        // Cascade
        if (cascade) {
            ops.addAll(opBuilder.getChainDeleteOps(id));
        }

        // Router/Chain name
        ops.add(opBuilder.getRouterTableChainNameDeleteOp(config.routerId,
                mgmtConfig.table, config.name));

        // Router/Chain ID
        ops.add(opBuilder.getRouterTableChainDeleteOp(config.routerId,
                mgmtConfig.table, id));

        // Root
        ops.add(opBuilder.getChainDeleteOp(id));

        log.debug("ChainOpService.buildDelete exiting: ops count={}",
                ops.size());
        return ops;
    }

    /**
     * Ops to delete all the router chains.
     *
     * @param routerId
     *            ID of the router
     * @param table
     *            Chain table
     * @return List of Op to delete
     * @throws StateAccessException
     *             Data error
     */
    public List<Op> buildDeleteRouterChains(UUID routerId, ChainTable table)
            throws StateAccessException {
        log.debug("ChainOpService.buildDeleteRouterChains entered: routerId="
                + routerId + ",table=" + table);

        List<Op> ops = new ArrayList<Op>();
        Set<String> ids = zkDao.getIds(routerId, table);
        for (String id : ids) {
            ops.addAll(buildDelete(UUID.fromString(id), true));
        }

        log.debug(
                "ChainOpService.buildDeleteRouterChains exiting: ops count={}",
                ops.size());
        return ops;
    }

    /**
     * Get Ops to create built-in chains for a router.
     *
     * @param routerId
     *            ID of the router
     * @param table
     *            ChainTable object
     * @return List of Op
     * @throws StateAccessException
     *             Data error
     */
    public List<Op> buildBuiltInChains(UUID routerId, ChainTable table)
            throws StateAccessException {
        log.debug("ChainOpService.buildBuiltInChains entered: routerId="
                + routerId + ",table=" + table);

        List<Op> ops = new ArrayList<Op>();
        String[] builtInChains = ChainTable.getBuiltInChainNames(table);
        for (String name : builtInChains) {
            UUID id = UUID.randomUUID();
            ChainConfig chainConfig = zkDao
                    .constructChainConfig(name, routerId);
            ChainMgmtConfig chainMgmtConfig = zkDao
                    .constructChainMgmtConfig(table);
            ChainNameMgmtConfig chainNameConfig = zkDao
                    .constructChainNameMgmtConfig(id);
            ops.addAll(buildCreate(id, chainConfig, chainMgmtConfig,
                    chainNameConfig));
        }

        log.debug("ChainOpService.buildBuiltInChains exiting: ops count={}",
                ops.size());
        return ops;
    }
}
