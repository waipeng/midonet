/*
 * Copyright (c) 2014 Midokura Europe SARL, All Rights Reserved.
 */
package org.midonet.midolman.io

import scala.concurrent.{ExecutionContext, Future}

import akka.actor.ActorSystem

import org.midonet.midolman.config.MidolmanConfig
import org.midonet.odp.{Packet, DpPort, Datapath}
import org.midonet.util.BatchCollector

class MockUpcallDatapathConnectionManager(config: MidolmanConfig)
        extends UpcallDatapathConnectionManager(config) {

    val conn = new MockManagedDatapathConnection()

    var upcallHandler: BatchCollector[Packet] = null

    def initialize()(implicit ec: ExecutionContext, as: ActorSystem) {
        if (upcallHandler == null) {
            upcallHandler = makeUpcallHandler()
            conn.getConnection.datapathsSetNotificationHandler(upcallHandler)
        }
    }

    override def portCreated(datapath: Datapath, port: DpPort, conn: ManagedDatapathConnection) {}

    override def portDeleted(datapath: Datapath, port: DpPort) {}

    override def createAndHookDpPort(datapath: Datapath, port: DpPort)(
            implicit ec: ExecutionContext, as: ActorSystem):
            Future[(Datapath, DpPort, ManagedDatapathConnection)] = {

        initialize()

        val (createCb, createFuture) = newCallbackBackedFuture[DpPort]()
        conn.getConnection.portsCreate(datapath, port, createCb)
        createFuture map { (datapath, _, conn) }
    }

    override def deleteDpPort(datapath: Datapath, port: DpPort)(
            implicit ec: ExecutionContext, as: ActorSystem): Future[Boolean] = {

        val (delCb, delFuture) = newCallbackBackedFuture[DpPort]()
        conn.getConnection.portsDelete(port, datapath, delCb)
        delFuture map { _ => true }
    }

}
