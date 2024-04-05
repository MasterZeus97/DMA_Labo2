# DMA - Labo 2

Loris Marzullo - Thibault Seem - Joel Matias

## Objectifs : Lister les balises et déterminer sa position

La majorité de nos changements ont eu lieu dans le fichier `MainActivity.kt`.

```kotlin

class MainActivity : AppCompatActivity() {

    //...
    
    private val beaconMap  = mutableMapOf<Int, PersistentBeacon>()

    private var beaconToNameMap : Map<Int, String> = mapOf(
        46 to "Salle de réunion",
        36 to "Salle de cours",
        23 to "Salle de TP"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        
        //...

        val beaconManager =  BeaconManager.getInstanceForApplication(this)
        val region = Region("all-beacons-region", null, null, null)

        beaconManager.beaconParsers.add(
            BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        )
        beaconManager.getRegionViewModel(region).rangedBeacons.observe(this, rangingObserver)
        beaconManager.startRangingBeacons(region)
    }

    //...
    
    /*
        Observer for ranging beacons:
        This observer is called every time beacons are detected in the region. The beacons variable contains a list of all beacons detected, sorted by distance. The closest beacon is then used to determine the place, which is displayed in the UI.
    */
    val rangingObserver = Observer<Collection<Beacon>> { beacons ->
        Log.d("TAG", "Ranged: ${beacons.count()} beacons")
        var beaconList : MutableList<PersistentBeacon> = mutableListOf()

        for (beacon: Beacon in beacons) {
            val pBeacon = PersistentBeacon.convertFromBeacon(beacon)

            if(pBeacon.minor == 46 || pBeacon.minor == 36 || pBeacon.minor == 23)
                beaconMap[pBeacon.minor] = pBeacon
        }

        beaconList = beaconMap.values.toMutableList()

        beaconList.sortBy {
            it.distance
        }

        beaconsViewModel.setNearbyBeacons(beaconList)

        beaconsViewModel.setPlaceByBeacons(beaconList.firstOrNull())

        // Remove beacons that are not detected anymore
        beaconMap.forEach { value ->

            value.value.count--
            if(value.value.count == 0)
                beaconMap.remove(value.key)
        }

        for (beacon: PersistentBeacon in beaconList) {

            Log.d("TAG", "$beacon about ${beacon.distance} meters away")
        }
    }
}
```

// TODO JOEL

Pour la partie permettant de se localiser, nous avons ajouté une map `beaconToNameMap` qui nous permet de lié les numéros des balises qui nous avaient été fournie à un nom de salle. Ainsi, il nous suffit d'afficher le text lié à la balise la plus proche de nous.



Nous avons également ajouté 2 méthodes dans `BeaconsViewModel.kt` permettant de mettre les LiveData à jour.

```kotlin
class BeaconsViewModel : ViewModel() {

    //...
    
    fun setNearbyBeacons(beacons : MutableList<PersistentBeacon>){
        _nearbyBeacons.value = beacons
    }

    fun setPlaceByBeacons(beacon: PersistentBeacon?){
        _closestBeacon.value = beacon
    }
}
```

Ces deux méthodes servent uniquement à copier la liste de beacons et le beacons le plus proches dans les LiveData utilisées pour l'affichage.

Enfin, nous avons ajouté deux choses dans `PersistantBeacon.kt`. La première est une méthode permettant de convertir un objet de type `Beacon` en un `PersistantBeacon` et un compteur de temps.

```kotlin
data class PersistentBeacon(
    var id : Long = nextId++,
    var major: Int,
    var minor: Int,
    var uuid: UUID,
    var rssi : Int,
    var txPower : Int,
    var distance : Double,
    var count : Int) {

    companion object {
        private var nextId = 0L

        fun convertFromBeacon(beacon : Beacon): PersistentBeacon {
            return PersistentBeacon(major = beacon.id2.toInt(), minor = beacon.id3.toInt(), uuid = beacon.id1.toUuid(),
                rssi = beacon.rssi, txPower = beacon.txPower, distance = beacon.distance, count = 20)
        }
    }

}
```

Le compteur `count` nous permet de savoir combien de fois l'objet a été affiché sans être rafraichi.

## 1.1. Questions d’approfondissement

### 1.1.1 Est-ce que toutes les balises à proximité sont présentes dans toutes les annonces de la librairie ? Que faut-il mettre en place pour permettre de « lisser »  les annonces et ne pas perdre momentanément certaines balises ?

```
Non, elles ne sont pas toujours toutes affichées. On peut présumer que la librairie garde en mémoire les signaux reçus pendant un temps T, et tout les temps T il nous donne la liste desdits signaux. Ce qui peut provoquer la disparition d'une des balises de la liste qui n'a pas envoyé de signal (ou que le signal n'a pas été reçu) durant cette période fixe.

Afin de régler ce problème, nous pouvons ajouter un tempon qui servira à savoir depuis combien de temps la balise n'a pas été mise à jour. Chaque fois qu'une balise sera détectée, soit son timer sera réinitialisé dans cette liste, soit elle sera ajoutée à cette liste. Lorsque le timer d'une de ces 

```



### 1.1.2 Nous souhaitons effectuer un positionnement en arrière-plan, à quel moment faut-il démarrer et éteindre le monitoring des balises ? Sans le mettre en place, que faudrait-il faire pour pouvoir continuer le monitoring alors que l’activité n’est plus active ?

```
Lors de l'appel à la fonction OnCreate(), nous pouvons démarrer le monitoring des balises et l'éteindre uniquement lors de l'appel au OnDestroy(). Et afin de le garder en place en arrière-plan, il faut déléguer l'appel aux fonctions internes du smartphone. Cela va fonctionner de la même manière que les notifications étant lancées même lorsque l'application est éteinte.

Pour le faire fonctionner, il faut mettre en place un BroadCastReceiver pour la communication avec les fonctions internes du téléphone.
```



### 1.1.3 On souhaite trier la liste des balises détectées de la plus proche à la plus éloignée, quelles sont les valeurs présentes dans les annonces reçues qui nous permettraient de le faire ? Comment sont-elles calculées et quelle est leur fiabilité ?

```
Une des valeurs envoyées dans les annonces est la distance. La distance est une estimation basée sur la force du signal bluethoot reçue du beacon au smartphone. La fiabilité est très mauvaise. Jusqu'à deux mètres, les données sont fiables de plus ou moins un mètre tandis qu'à vingt mètres, sont fiables à plus ou moins 10 à 40 mètres.
```

### 2.1.1 Comment pouvons-nous déterminer notre position ? Est-ce uniquement basé sur notion de
proximité étudiée dans la question 1.1.3, selon vous est-ce que d’autres paramètres peuvent
être pertinents ?

```
Si on a une seule balise, nous n'avons pas vraiment d'autres choix que de soit calculer la distance avec la puissance du signal, soit utiliser la valeur de distance envoyée par la balise. 
Si nous avons 3 balises ou plus, il serait possible de faire une trilatération afin de pouvoir se repérer dans un plan. Cependant, cela ne serait pas excessivement précis, puisque chaque obstacle poserait problème en modifiant la puissance du signal.
```

### 2.1.2 Les iBeacons sont conçus pour permettre du positionnement en intérieur. D’après l’expérience que vous avez acquise sur cette technologie dans ce laboratoire, quels sont les cas d’utilisation pour lesquels les iBeacons sont pleinement adaptés (minimum deux) ? Est-ce que vous voyez des limitations qui rendraient difficile leur utilisation pour certaines applications ?

```
Nous avons pensé à deux cas d'utilisations. 

Le premier serait de permettre à une personne de s'orienter dans une manifestation (festival, salon, forum) à l'aide d'une application. Des iBeacon seraient disposés, par exemple dans le cas du salon de l'auto, à chaque stands. Ainsi, l'app pourrait calculer la position de l'utilisateur dans le salon, et lui indiquer un chemin vers un autre stand.

La seconde façon de les utiliser serait dans une exposition. Un iBeacon serait placé à chaque point d'intérêt (une ouvre d'art, une voiture, etc.). Lorsqu'une personne dotée de l'app de l'exposition s'approcherait, l'app lui afficherait des informations à propos du point d'intérêt. 

La localisation à l'aide des iBeacon possède certain désaventages. Le premier est que, puisque la distance est calculée à l'aide de la force du signal, si un obstacle se tient entre nous et la balise, la mesure de distance risque d'être totalement faussée. Par exemple, si on reprend la seconde façon de les utiliser, si les oeuvres (et les iBeacon) sont enfermés dans des boites en verre par exemple, on risque de ne pas détecter l'ibeacon comme étant proche de nous. 

Un autre problème est la précision des iBeacon. Ils ne sont en effet pas très précis et ne peuvent donc pas être utilisé pour obtenir un positionnement exact.

Enfin, il nous a semblé que les smartphone ne réagissaient pas tous de manière similaire aux balises, n'obtenant pas toujours la même distance. Cela rend compliqué l'application systématique d'une librairie.
```

