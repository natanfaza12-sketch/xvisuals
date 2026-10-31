# X Visuals (Fabric, Minecraft 1.21.4)

Klientowy mod wizualny. **Right Shift** otwiera animowane menu: Visual -> Sky | Aura | Trail | Hit Sound | Hit FX | World, oraz Settings -> Interface.

## Budowanie
Wymagane: Java 21.
1. Jeśli masz Gradle: `gradle wrapper --gradle-version 8.12` (jednorazowo), potem `./gradlew build`.
   Albo otwórz folder w IntelliJ IDEA (sam pobierze Gradle) i uruchom task `build`.
2. Gotowy plik: `build/libs/xvisuals-1.0.0.jar` -> wrzuć do `.minecraft/mods` razem z Fabric API 1.21.4.

## Własny dźwięk uderzenia
W Hit Sound wybierz "Custom". Plik `assets/xvisuals/sounds/custom_hit.ogg` możesz nadpisać zwykłym resource packiem
(mono/stereo `.ogg`, ta sama ścieżka w paczce).

## Config
`config/xvisuals.json` (zapisuje się przy zamknięciu menu).

## Build bez instalowania Gradle (GitHub)
Wrzuć cały folder do nowego repozytorium na GitHubie. Zakładka **Actions** -> "Build X Visuals" -> po zakończeniu
pobierz artefakt `xvisuals-jar` (w środku jest gotowy `.jar`).
