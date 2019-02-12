make: build

build:
	mkdir -p build
	javac -cp src/:vendor/junit-4.10.jar src/**/*.java -d build/

run: build
	./run

test: build
	java -cp build/:vendor/junit-4.10.jar: org.junit.runner.JUnitCore floor.FloorUnitTests
	java -cp build/:vendor/junit-4.10.jar: org.junit.runner.JUnitCore elevator.ElevatorSubSystemTest

clean:
	rm -r build/**/*.class

elevator: build
	java -cp build/ elevator.ElevatorManager

scheduler: build
	java -cp build/ scheduler.Scheduler

floor: build
	java -cp build/ floor.FloorManager
