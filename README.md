# elevator-simulator
Elevator Simulation for SYSC3303 Group 9

Members:
- Kirin Rastogi 101003372
- Vikram Bombhi 101007498
- Jacky Chiu 101001982
- Kavan Salehi 101046945
- Connor Poland 101013229


## Changelog

#### 1.00
The basic system has been set up with all three subsystems with communication between all. The elevator subsystem currently simulates one elevator, as it arrives at a floor it triggers the arrival sensor alerting the floor subsystem of its arrival and stopping at the floor if needed. The floor subsystem currently simulates three floors and simulates the flow of people requesting elevators and moving around. The scheduler subsystem coordinates the elevators based on the requests from the floor subsystem.

##### Contribtions
- Kirin Rastogi: Built the scheduler algorithm.
- Vikram Bombhi: Basic structure and communication.
- Jacky Chiu: Messages serialization and deserialization and elevator subsystem.
- Connor Poland: Floor subsystem.
