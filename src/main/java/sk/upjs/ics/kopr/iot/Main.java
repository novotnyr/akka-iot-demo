package sk.upjs.ics.kopr.iot;

import akka.actor.typed.ActorSystem;
import akka.cluster.typed.ClusterSingleton;
import akka.cluster.typed.SingletonActor;

public class Main {
    public static void main(String[] args) {
        ActorSystem<Sensor.Command> system = ActorSystem.create(Sensor.create(), "ClusterSystem");
        system.tell(new Sensor.TriggerMeasurement());

        ClusterSingleton clusterSingleton = ClusterSingleton.get(system);
        clusterSingleton.init(SingletonActor.of(Aggregator.create(), "Aggregator"));
    }
}
