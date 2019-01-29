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
	java floor.Floor

build:
	javac src/**/*.java

clean:
	rm -r bin/*
