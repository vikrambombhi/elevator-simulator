make: build

run: build
	./run

.ONESHELL:
elevator:
	cd src/
	javac elevator/*.java
	java elevator.Elevator

.ONESHELL:
scheduler:
	cd src/
	javac scheduler/*.java
	java scheduler.Scheduler

.ONESHELL:
floor:
	cd src/
	javac floor/*.java
	java floor.FloorManager

test: build
	cd src/
	java -cp ../vendor/junit-4.10.jar: org.junit.runner.JUnitCore floor.FloorUnitTests

build:
	javac -cp vendor/junit-4.10.jar src/**/*.java

clean:
	rm -r bin/*
