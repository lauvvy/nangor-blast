ifeq ($(OS),Windows_NT)
    # Absolute path: make on Windows may run recipes through cmd, which does not run ./x.bat.
    GRADLEW := "$(CURDIR)/gradlew.bat"
else
    GRADLEW := ./gradlew
endif

# Run from the installed distribution, not `gradlew run`: Gradle does not give
# the program a real terminal, which the game needs for raw key input.
RUN_CP := "build/install/nangor-blast/lib/*"

.PHONY: build run spike test clean

build:
	$(GRADLEW) build

run:
	$(GRADLEW) installDist
	java -cp $(RUN_CP) nangorblast.app.Main

# S-02 terminal spike: key echo and 20 FPS redraw. Every member runs it once.
spike:
	$(GRADLEW) installDist
	java -cp $(RUN_CP) nangorblast.view.cli.TerminalSpike

test:
	$(GRADLEW) test

clean:
	$(GRADLEW) clean
