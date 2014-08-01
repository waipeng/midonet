/*
 * Copyright (c) 2014 Midokura SARL, All Rights Reserved.
 */
package org.midonet.midolman

import java.util.UUID
import scala.collection.JavaConversions._

import com.google.inject.{Scopes, PrivateModule, AbstractModule, Guice, Injector}
import com.yammer.metrics.core.MetricsRegistry
import org.apache.commons.configuration.HierarchicalConfiguration
import org.openjdk.jmh.annotations.{Setup => JmhSetup, TearDown}

import org.midonet.cluster.services.MidostoreSetupService
import org.midonet.cluster.Client
import org.midonet.config.ConfigProvider
import org.midonet.midolman.services.{DatapathConnectionService, DashboardService,
        SelectLoopService, MidolmanActorsService, HostIdProviderService, MidolmanService}
import org.midonet.midolman.version.guice.VersionModule
import org.midonet.midolman.guice.{MidolmanModule, ResourceProtectionModule, MidolmanActorsModule}
import org.midonet.midolman.guice.serialization.SerializationModule
import org.midonet.midolman.guice.config.ConfigProviderModule
import org.midonet.midolman.guice.datapath.MockDatapathModule
import org.midonet.midolman.guice.zookeeper.MockZookeeperConnectionModule
import org.midonet.midolman.guice.cluster.ClusterClientModule
import org.midonet.midolman.host.scanner.InterfaceScanner
import org.midonet.midolman.util.mock.{MockMidolmanActors, MockInterfaceScanner}
import org.midonet.midolman.simulation.Chain
import org.midonet.midolman.config.MidolmanConfig

trait MidolmanBenchmark extends MockMidolmanActors {
    var injector: Injector = null

    @JmhSetup
    def midolmanBenchmarkSetup(): Unit = {
        val config = fillConfig(new HierarchicalConfiguration)
        injector = Guice.createInjector(getModules(config))
        injector.getInstance(classOf[MidostoreSetupService]).startAndWait()
        injector.getInstance(classOf[MidolmanService]).startAndWait()
    }

    @TearDown
    def midolmanBenchmarkTeardown(): Unit = {
        injector.getInstance(classOf[MidolmanService]).stopAndWait()
        injector.getInstance(classOf[MidostoreSetupService]).stopAndWait()
    }

    protected def fillConfig(config: HierarchicalConfiguration)
    : HierarchicalConfiguration = {
        config.setProperty("midolman.midolman_root_key", "/test/v3/midolman")
        config.setProperty("cassandra.servers", "localhost:9171")
        config
    }

    protected def getModules(config: HierarchicalConfiguration) = {
        List(
            new VersionModule(),
            new SerializationModule(),
            new ConfigProviderModule(config),
            new MockDatapathModule(),
            new MockZookeeperConnectionModule(),
            new AbstractModule {
                def configure() {
                    bind(classOf[HostIdProviderService])
                            .toInstance(new HostIdProviderService() {
                        val hostId = UUID.randomUUID()
                        def getHostId: UUID = hostId
                    })
                }
            },
            new ClusterClientModule(),
            new MidolmanActorsModule {
                override def configure() {
                    bind(classOf[MidolmanActorsService])
                            .toInstance(actorsService)
                    expose(classOf[MidolmanActorsService])
                }
            },
            new ResourceProtectionModule(),
            new PrivateModule {
                override def configure() {
                    bind(classOf[InterfaceScanner])
                            .to(classOf[MockInterfaceScanner]).asEagerSingleton()
                    expose(classOf[InterfaceScanner])
                }
            },
            // This is MidolmanModule, but doesn't create threads via the
            // SelectLoopService.
            new PrivateModule {
                override def configure() {
                    binder.requireExplicitBindings()
                    requireBinding(classOf[ConfigProvider])
                    requireBinding(classOf[Client])
                    requireBinding(classOf[DatapathConnectionService])
                    requireBinding(classOf[MidolmanActorsService])

                    bind(classOf[MidolmanService]).in(Scopes.SINGLETON)
                    expose(classOf[MidolmanService])

                    bind(classOf[MidolmanConfig])
                            .toProvider(classOf[MidolmanModule.MidolmanConfigProvider])
                            .in(Scopes.SINGLETON)
                    expose(classOf[MidolmanConfig])

                    bind(classOf[SelectLoopService])
                    .toInstance(new SelectLoopService {
                        override def doStart(): Unit = notifyStarted()
                        override def doStop(): Unit = notifyStopped()
                    })
                    expose(classOf[SelectLoopService])

                    bind(classOf[DashboardService])
                            .toInstance(new DashboardService {
                        override def doStart(): Unit = notifyStarted()
                        override def doStop(): Unit = notifyStopped()
                    })
                    expose(classOf[DashboardService])

                    bind(classOf[MetricsRegistry])
                            .toInstance(new MetricsRegistry)
                    expose(classOf[MetricsRegistry])

                    requestStaticInjection(classOf[Chain])
                }
            }
        )
    }
}
