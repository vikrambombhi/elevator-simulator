Run the scheduler through SchedulerSubsystem.java

SchedulerSubsystem - acts as the producer for queue items across all elevator queues. These items are created from request messages.

Scheduler - consumer for a single elevator queue. Full fills the trips listed in its queue

QueueCleaner - checks to see if a queue has been set as hard fault and moves those pick ups to other queues
