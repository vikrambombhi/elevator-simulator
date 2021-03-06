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

3. Open src/scheduler/SchedulerSubSystem.java, click run in eclipse

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

#### 4.00

Made the scheduler composed of multiple classes.
Added timing, and statistics about timings of scheduler algorithm & message sending. Stats are printed after a termination message.

#### 5.00

Added a UI and some calculations.
The results of our system test across 2 computers with significant message traffic are reflected in the following results:
Arrival Sensor Interface: Period - 50, Response Time - 10.129ms
Elevator Buttons Interface: Period - 100, Response Time - 10.143ms
Floor Buttons Interface: Period - 200, Response Time - 10.379ms
To make a fair RMS viability analysis we can generously inflate these average response times...
Arrival Sensor Interface: Period - 50, Response Time - 20ms
Elevator Buttons Interface: Period - 100, Response Time - 20ms
Floor Buttons Interface: Period - 200, Response Time - 20ms
Using the standard RMS viability inequality for 3 processes:
(20/50) + (20/100) + (20/200) <= 0.78
0.70 < 0.78 ... therefore our system is certainly schedulable by RMS to meet its deadlines consistently

## Diagrams

The most up to date version of our diagrams can be found at..
	src/diagrams/elevator_uml.pdf
	src/diagrams/Floor_uml.pdf
	src/diagrams/scheduler_uml.pdf
	src/diagrams/elevator_state.pdf
	src/diagrams/timing_diagram.pdf

	if you cannot read the pdf diagrams for elevator_uml and scheduler_uml we
	have included png diagrams which are formatted better

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

##### Contributions - Iteration 3
- Kirin Rastogi: Scheduler soft faults and tests
- Vikram Bombhi: Elevator fault simulation and integration
- Jacky Chiu: Scheduler hard faults and tests
- Connor Poland: Refactor of queueing system and fault file simulation

##### Contributions - Iteration 4
- Kirin Rastogi: Scheduler timings & statistics
- Vikram Bombhi: Scheduler timings & statistics
- Jacky Chiu: Scheduler refactor
- Connor Poland: Diagrams & scaling up the amount of elevators

##### Contributions - Iteration 5
- Kirin Rastogi: UML
- Vikram Bombhi: UML
- Jacky Chiu: gui for floor and elevator subsystems, scheduler refactor
- Connor Poland: Calculations
