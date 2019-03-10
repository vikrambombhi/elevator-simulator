# elevator-simulator
Elevator Simulation for SYSC3303 Group 9

Members:
- Kirin Rastogi 101003372
- Vikram Bombhi 101007498
- Jacky Chiu 101001982
- Connor Poland 101013229

## HOW TO RUN

1. Import the project into eclipse

2. Open src/elevator/ElevatorManager.java, click run in eclipse

3. Open src/scheduler/Scheduler.java, click run in eclipse

4. Open src/floor/FloorManager.java, click run in eclipse

5. Run (Optional) the following as junit test. Ex:
```
	java -cp build/:vendor/junit-4.10.jar: org.junit.runner.JUnitCore floor.FloorUnitTests
	java -cp build/:vendor/junit-4.10.jar: org.junit.runner.JUnitCore elevator.ElevatorManagerTest
	java -cp build/:vendor/junit-4.10.jar: org.junit.runner.JUnitCore scheduler.SchedulerTest
```

## Changelog

#### 1.00
The basic system has been set up with all three subsystems with communication between all. The elevator subsystem currently simulates one elevator, as it arrives at a floor it triggers the arrival sensor alerting the floor subsystem of its arrival and stopping at the floor if needed. The floor subsystem currently simulates three floors and simulates the flow of people requesting elevators and moving around. The scheduler subsystem coordinates the elevators based on the requests from the floor subsystem. For a more detailed explanation of each subsystem read their respective readme.

We feel that the scheduler should not be modeled using a state machine because it essentially only has two states, 'servicing' and 'idle'. The scheduler is essentially stateless and all the state is held in the messages it sends/receives.

#### 2.00
The system has been adapted to service multiple elevators, each of which is completely independent and running in its own thread. The scheduler has been updated to utilize this increased service potential by dynamically designating elevators as mainly 'up traveling' or 'down traveling' based on the frequency of requests and the position of each elevator relative to those requests.

#### 3.00
The system has been updated to now include error handling and detection.
The floor subsystem now reads and sends faults to the elevator subsystem.
The elevator subsystem receives these fault messages and simulates them.
The scheduler handles soft fault by resending the last message.
Hard fault are handled by removing the faulty elevator and redistributing the queue.

## Diagrams

The most up to date version of our diagrams can be found at..
	src/diagrams/elevator_uml.pdf
	src/diagrams/Floor_uml.pdf
	src/diagrams/scheduler_uml.pdf
	src/diagrams/elevator_state.pdf
	src/diagrams/timing_diagram.pdf

##### Contributions - Iteration 1
- Kirin Rastogi: Built the scheduler algorithm.
- Vikram Bombhi: Basic structure and communication.
- Jacky Chiu: Messages serialization and de serialization and elevator subsystem.
- Connor Poland: Floor subsystem.

##### Contributions - Iteration 2
- Kirin Rastogi: elevator refactor, scheduler contributions.
- Vikram Bombhi: Junit automation + testing, UML updates, scheduler + input file contributions.
- Jacky Chiu: Refactored elevator for multiple elevators, scheduler contributions, elevator junit
- Connor Poland: Utilized previous assets to implement input file requests, timing diagram.

##### Contributions - Iteration 2
- Kirin Rastogi: Scheduler soft faults and tests
- Vikram Bombhi: Elevator fault simulation and integration
- Jacky Chiu: Scheduler hard faults and tests
- Connor Poland: Refactor of queueing system and fault file simulation
