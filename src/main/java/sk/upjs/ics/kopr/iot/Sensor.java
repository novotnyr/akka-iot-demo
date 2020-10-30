package sk.upjs.ics.kopr.iot;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;

import java.time.Duration;

// Actor
public class Sensor extends AbstractBehavior<Sensor.Command> {

    private ActorRef<Aggregator.Command> aggregator;

    private Sensor(ActorContext<Command> context) {
        super(context);

        var messageAdapter = context.messageAdapter(
                Receptionist.Listing.class, HandleListing::new);

        Receptionist.Command message = Receptionist.subscribe(Aggregator.AGGREGATOR, messageAdapter);
        context.getSystem()
                .receptionist()
                .tell(message);
    }

    public static Behavior<Sensor.Command> create() {
        return Behaviors.setup(context -> {
            return Behaviors.withTimers(timers -> {
                timers.startTimerWithFixedDelay(new TriggerMeasurement(), Duration.ofSeconds(2));
                return new Sensor(context);
            });
        });
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(TriggerMeasurement.class, this::triggerMeasurement)
                .onMessage(HandleListing.class, this::handleListing)
                .build();
    }

    private Behavior<Command> handleListing(HandleListing command) {
        getContext().getLog().info("Aggregator list has changed: " + command);
        var instances = command.getListing().getServiceInstances(Aggregator.AGGREGATOR);
        if (instances.size() != 1) {
            getContext().getLog().error("Incorrect number of Aggregators. Found: {}", instances.size());
            return this;
        }
        aggregator = instances.iterator().next();

        return this;
    }

    private Behavior<Command> triggerMeasurement(TriggerMeasurement command) {
        double temperature = (Math.random() * 60) - 30;
        if (this.aggregator == null) {
            getContext().getLog().error("No aggregator is available");
            return this;
        }
        getContext().getLog().info("Sending temperature to the aggregator: {}", temperature);
        this.aggregator.tell(new Aggregator.SendTemperature(
                temperature,
                getContext().getSelf()));
        return this;
    }

    interface Command {

    }

    public static class TriggerMeasurement implements Command {
    }

    public static class HandleListing implements Command {
        private final Receptionist.Listing listing;

        public HandleListing(Receptionist.Listing listing) {
            this.listing = listing;
        }

        public Receptionist.Listing getListing() {
            return listing;
        }
    }
}
