# Gym Membership Management System

A console application in Java 17 that manages gym plans, members, trainers, attendance and payments.

## Requirements
- JDK 17 or newer
- Maven 3.8+ (bundled with IntelliJ, Eclipse and VS Code Java extensions)
- Internet access the first time, so Maven can download Jackson

## Open in an IDE
- **IntelliJ IDEA**: File > Open > select the `gym-membership-system` folder (the one containing `pom.xml`). Wait for Maven to import, then right-click `Main.java` > Run.
- **Eclipse**: File > Import > Maven > Existing Maven Projects.
- **VS Code**: install "Extension Pack for Java", open the folder, then run `Main.java`.

## Run from a terminal
```
mvn compile exec:java                                  # interactive menu
mvn compile exec:java -Dexec.args="--demo"             # automatic walkthrough of every feature
mvn package && java -jar target/gym-membership-system.jar
```

## Project structure
```
src/main/java/com/gym
├── Main.java                      entry point (menu or --demo)
├── model/                         CO1 - OOP
│   ├── Person.java                abstract class: encapsulation, equals/hashCode/toString, abstract getRole()
│   ├── Member.java                extends Person, Comparable<Member>, overrides getRole()/toString()
│   ├── Trainer.java               extends Person, overrides getRole()/toString()
│   ├── MembershipPlan.java        Comparable by fee
│   ├── Attendance.java, Payment.java, PaymentStatus.java, Identifiable.java
├── repo/Repository.java           generic HashMap-backed store: search(Predicate), sorted(Comparator)
├── service/GymService.java        business logic, search, sorting (Comparable + Comparator)
├── concurrent/                    CO3 - concurrency
│   ├── ConcurrentProcessor.java   ExecutorService, Callable/Future, Runnable on a Thread
│   └── BatchResult.java
├── storage/                       File I/O + Jackson JSON
│   ├── JsonStorage.java           save/load with Jackson (java.time supported)
│   ├── GymData.java               the object written to JSON
│   └── ReportWriter.java          plain-text report via java.nio
└── ui/
    ├── MenuController.java        main menu / workflow integrating all modules
    ├── SampleData.java            demo records
    └── Demo.java                  non-interactive walkthrough
```

## Where each requirement lives
| Requirement | Location |
|---|---|
| Classes, constructors, encapsulation | `model/*` |
| Inheritance / abstract class / overriding | `Person` -> `Member`, `Trainer` |
| equals / hashCode / toString | `Person`, `MembershipPlan`, `Attendance`, `Payment` |
| HashMap / List storage, generics | `repo/Repository.java` |
| Search | `GymService.searchMembers` |
| Comparable / Comparator sorting | `Member.compareTo`, `GymService.getMembersBy*` |
| Thread, Runnable, Callable, ExecutorService | `ConcurrentProcessor` |
| Main menu / controller | `ui/MenuController.java` |
| File I/O + Jackson JSON save/load | `storage/*`, menu options 14-16 |

## Data files
Saved to the `data/` folder (created automatically): `gym-data.json` is auto-loaded on start if it exists.

## Suggested Git commits (for GitHub evidence)
1. `Add OOP model classes (Person, Member, Trainer, MembershipPlan, Attendance, Payment)`
2. `Add generic Repository and GymService with search and sorting`
3. `Add ConcurrentProcessor using ExecutorService`
4. `Add Jackson JSON storage and text report`
5. `Add console MenuController, sample data and demo mode`
