Architecture
============

![Actor Setup and Messages](architecture.png)

Running
=======

Run a primary node:

	-Dakka.cluster.seed-nodes.0=akka://ClusterSystem@127.0.0.1:2551 -Dakka.remote.artery.canonical.port=2551

Run a secondary node with the following settings:

	-Dakka.cluster.seed-nodes.0=akka://ClusterSystem@127.0.0.1:2551 -Dakka.remote.artery.canonical.port=2552

Troubleshooting
===============

	 No configured serialization-bindings for class [sk.upjs.ics.kopr.akka.cluster.SensorAggregator$SendTemperature]

Enable Java serialization, which is unsafe and possibly an attack vector.
In `application.conf`, enable:

	akka.actor.allow-java-serialization = on
