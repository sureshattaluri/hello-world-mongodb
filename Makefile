.PHONY: build
build:
	./gradlew clean build
test:
	./gradlew check
run:
	./gradlew run