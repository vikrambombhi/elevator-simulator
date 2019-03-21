.PHONY: build

make: build

build:
	mkdir -p build/src
	cp src/input.txt build/src/input.txt
	javac -cp src/:vendor/junit-4.10.jar src/**/*.java -d build/

run: build
	./run

test: build
	java -cp build/:vendor/junit-4.10.jar: org.junit.runner.JUnitCore floor.FloorUnitTests
	java -cp build/:vendor/junit-4.10.jar: org.junit.runner.JUnitCore elevator.ElevatorManagerTest
	java -cp build/:vendor/junit-4.10.jar: org.junit.runner.JUnitCore scheduler.SchedulerTest

clean:
	rm -r build/**/*.class

elevator: build
	java -cp build/ elevator.ElevatorManager

scheduler: build
	java -cp build/ scheduler.SchedulerSubSystem

floor: build
	java -cp build/ floor.FloorManager
