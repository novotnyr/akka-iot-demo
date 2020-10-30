package sk.upjs.ics.kopr.iot;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;

import java.io.Serializable;

public class Aggregator extends AbstractBehavior<Aggregator.Command> {
    interface Command extends Serializable {}

    public static class SendTemperature implements Command {
        private final double temperature;

        private final ActorRef<Sensor.Command> sensor;

        public SendTemperature(double temperature, ActorRef<Sensor.Command> sensor) {
            this.temperature = temperature;
            this.sensor = sensor;
        }

        public ActorRef<Sensor.Command> getSensor() {
            return sensor;
        }

        public double getTemperature() {
            return temperature;
        }
    }

    public static Behavior<Aggregator.Command> create() {
        return Behaviors.setup(context -> {
            context.getSystem()
                    .receptionist()
                    .tell(Receptionist.register(AGGREGATOR, context.getSelf()));

            return new Aggregator(context);
        });
    }

    public static final ServiceKey<Aggregator.Command> AGGREGATOR
            = ServiceKey.create(Aggregator.Command.class, "Aggregator");

    //--------------------------------------------

    private Aggregator(ActorContext<Command> context) {
        super(context);
    }


    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(SendTemperature.class, this::sendTemperature)
                .build();
    }

    private Behavior<Command> sendTemperature(SendTemperature command) {
        getContext().getLog().info("Received temperature {} from {}",
                command.temperature, command.sensor);
        return this;
    }
}
