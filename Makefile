make: build

run: build
	./run

test: build
	java -cp src/:junit-4.11.jar org.junit.runner.JUnitCore floor.FloorUnitTests

clean:
	rm -r src/**/*.class

test: build
	cd src/
	java -cp ../vendor/junit-4.10.jar: org.junit.runner.JUnitCore floor.FloorUnitTests

build:
	javac -cp vendor/junit-4.10.jar src/**/*.java

elevator: build
	java elevator.ElevatorSubSystem

scheduler: build
	java scheduler.Scheduler

floor: build
	java floor.FloorManager
