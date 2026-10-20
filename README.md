# Benchmark parsing CAMT.053 : Java, Java Valhalla, Rust, C++

Même logique dans tous les langages : celle de `StaxLegacyParser` (DOM + expressions XPath). Chaque version se compile et se lance avec sa propre chaîne d'outils.

```
data/sample-camt053.xml   le fichier d'entrée
data/expected-dump.txt    contenu extrait par le Java legacy (référence)
java/                     ton projet Java complet (legacy + Valhalla + JMH), pom.xml
cpp/                      make + pugixml 1.16 (MIT, inclus dans third_party/)
```

## Java et Java Valhalla

Les sources sont celles que tu m'as envoyées, non modifiées, sauf deux ajouts : `DumpLegacy` et `DumpValhalla` (affichent le contenu extrait, pour comparer) et `pom.xml`.

Java ordinaire (JDK 21 ou plus). Attention : dans ce mode, le package `model/valhalla` ne compile pas, car `value class` n'existe pas dans un JDK ordinaire. Pour tester le legacy seul, supprime temporairement `model/valhalla`, `StaxValhallaParser`, `DumpValhalla` et la méthode Valhalla du benchmark.

Java Valhalla : télécharge le build early-access sur https://jdk.java.net/valhalla, décompresse-le, puis :

```
cd java
export JAVA_HOME=/chemin/vers/le/jdk-valhalla
mvn -Pvalhalla package          # compile avec --enable-preview, release 27
$JAVA_HOME/bin/java --enable-preview -jar target/benchmarks.jar -jvmArgs --enable-preview
```

`<java.release>` dans le profil `valhalla` doit correspondre au numéro du build (27 pour `27-jep401ea3`). Avec ce JDK, tout le projet (legacy et Valhalla) compile et le benchmark JMH compare les deux méthodes, `parseLegacy` et `parseWithValhalla`.

## C++

```
cd cpp  && make && ./camt_bench bench ../data/sample-camt053.xml
```

Le benchmark reprend les paramètres de ton JMH (3 fenêtres de warm-up de 5 s, 3 de mesure de 10 s, temps moyen par appel en ns). Pour un essai rapide :

```
bench <fichier> <fenêtres_warmup> <secondes_warmup> <fenêtres_mesure> <secondes_mesure>
```

## Vérifier que tout extrait la même chose

```
java -cp target/classes org.example.DumpLegacy   ../data/sample-camt053.xml | diff - ../data/expected-dump.txt
java --enable-preview -cp target/classes org.example.DumpValhalla ../data/sample-camt053.xml | diff - ../data/expected-dump.txt
./cpp/camt_bench dump data/sample-camt053.xml | diff - data/expected-dump.txt
```

Aucune sortie signifie que le contenu est identique. `DumpValhalla` affiche "null" pour les chaînes vides, car le modèle Valhalla stocke "" là où le legacy a null.

## Différences avec le Java

- Namespaces : `DocumentBuilderFactory` (Java) et pugixml l'ignorent. `sxd-xpath` (Rust) en tient compte, donc les expressions s'écrivent `//c:Ntry`.
- Types : `BigDecimal` devient un entier plus une échelle, `Currency` une chaîne (Rust et C++).
- Les expressions XPath sont compilées à chaque appel dans tous les langages.
- Le parseur Valhalla garde `Double.parseDouble` et `Math.round` pour les montants, donc il ne fait pas exactement le même travail que le legacy (`BigDecimal`).
