make: build

build:
	javac -cp src/:vendor/junit-4.10.jar src/**/*.java

run: build
	./run

test: build
	java -cp src/:vendor/junit-4.10.jar: org.junit.runner.JUnitCore floor.FloorUnitTests

clean:
	rm -r src/**/*.class

elevator: build
	java -cp src/ elevator.ElevatorSubSystem

scheduler: build
	java -cp src/ scheduler.Scheduler

floor: build
	java -cp src/ floor.FloorManager
